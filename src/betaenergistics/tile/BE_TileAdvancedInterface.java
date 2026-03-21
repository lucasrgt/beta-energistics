package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.*;

/**
 * Advanced Interface — bridges ME network with external machines for processing patterns.
 *
 * When the Autocrafter starts a processing craft, it pushes input items here.
 * The interface places them into the adjacent machine (furnace, crusher, etc.)
 * and watches for expected output items to appear, pulling them back into the network.
 *
 * Buffer: 9 slots for holding items in transit.
 * Each tick:
 *   1. Push buffered items into adjacent machine input slots
 *   2. Pull matching output items from adjacent machine back to network
 */
public class BE_TileAdvancedInterface extends TileEntity implements BE_INetworkNode, IInventory {
    private static final int ENERGY_USAGE = 3;
    private static final int BUFFER_SLOTS = 9;
    private static final int TICK_INTERVAL = 10;

    private BE_StorageNetwork network;
    private ItemStack[] buffer = new ItemStack[BUFFER_SLOTS];
    private int tickCounter = 0;

    // Expected output from current processing job
    private int expectedOutputId = 0;
    private int expectedOutputDmg = 0;
    private int expectedOutputCount = 0;

    private IInventory cachedAdjacentInv = null;
    private boolean adjacentDirty = true;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld || network == null || !network.isActive()) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        IInventory adjacent = getAdjacentInventory();
        if (adjacent == null) return;

        // 1. Push buffered items into adjacent machine
        pushBufferToMachine(adjacent);

        // 2. Pull expected output from adjacent machine back to network
        if (expectedOutputId > 0) {
            pullOutputFromMachine(adjacent);
        }
    }

    /**
     * Called by Autocrafter to start a processing job.
     * Accepts input items into the buffer and sets expected output.
     */
    public boolean acceptProcessingJob(ItemStack[] inputs, ItemStack output) {
        if (inputs == null || output == null) return false;

        // Check if buffer has enough empty slots
        int slotsNeeded = 0;
        for (ItemStack input : inputs) {
            if (input != null) slotsNeeded++;
        }

        int emptySlots = 0;
        for (int i = 0; i < BUFFER_SLOTS; i++) {
            if (buffer[i] == null) emptySlots++;
        }
        if (emptySlots < slotsNeeded) return false;

        // Place inputs into buffer
        int bufIdx = 0;
        for (ItemStack input : inputs) {
            if (input == null) continue;
            while (bufIdx < BUFFER_SLOTS && buffer[bufIdx] != null) bufIdx++;
            if (bufIdx >= BUFFER_SLOTS) return false;
            buffer[bufIdx] = input.copy();
            bufIdx++;
        }

        // Set expected output
        expectedOutputId = output.itemID;
        expectedOutputDmg = output.getItemDamage();
        expectedOutputCount = output.stackSize;

        return true;
    }

    public boolean hasActiveJob() {
        // Active if we have buffered items or are expecting output
        if (expectedOutputId > 0) return true;
        for (int i = 0; i < BUFFER_SLOTS; i++) {
            if (buffer[i] != null) return true;
        }
        return false;
    }

    private void pushBufferToMachine(IInventory machine) {
        for (int bufSlot = 0; bufSlot < BUFFER_SLOTS; bufSlot++) {
            if (buffer[bufSlot] == null) continue;

            ItemStack toPlace = buffer[bufSlot];
            // Try to place into machine slots
            for (int mSlot = 0; mSlot < machine.getSizeInventory(); mSlot++) {
                ItemStack existing = machine.getStackInSlot(mSlot);
                if (existing == null) {
                    machine.setInventorySlotContents(mSlot, toPlace.copy());
                    buffer[bufSlot] = null;
                    machine.onInventoryChanged();
                    break;
                } else if (existing.itemID == toPlace.itemID
                        && existing.getItemDamage() == toPlace.getItemDamage()
                        && existing.stackSize + toPlace.stackSize <= existing.getMaxStackSize()) {
                    existing.stackSize += toPlace.stackSize;
                    buffer[bufSlot] = null;
                    machine.onInventoryChanged();
                    break;
                }
            }
        }
    }

    private void pullOutputFromMachine(IInventory machine) {
        BE_ItemKey outputKey = new BE_ItemKey(expectedOutputId, expectedOutputDmg);
        int remaining = expectedOutputCount;

        for (int mSlot = 0; mSlot < machine.getSizeInventory() && remaining > 0; mSlot++) {
            ItemStack stack = machine.getStackInSlot(mSlot);
            if (stack == null) continue;
            if (stack.itemID != expectedOutputId || stack.getItemDamage() != expectedOutputDmg) continue;

            int toTake = Math.min(stack.stackSize, remaining);
            int inserted = network.getRootStorage().insert(outputKey, toTake, false);
            if (inserted > 0) {
                stack.stackSize -= inserted;
                if (stack.stackSize <= 0) {
                    machine.setInventorySlotContents(mSlot, null);
                }
                machine.onInventoryChanged();
                remaining -= inserted;
            }
        }

        if (remaining <= 0) {
            // Job complete
            expectedOutputId = 0;
            expectedOutputDmg = 0;
            expectedOutputCount = 0;
        }
    }

    public void onAdjacentChanged() {
        adjacentDirty = true;
        cachedAdjacentInv = null;
    }

    private IInventory getAdjacentInventory() {
        if (!adjacentDirty && cachedAdjacentInv != null) return cachedAdjacentInv;
        adjacentDirty = false;
        cachedAdjacentInv = findAdjacentInventory();
        return cachedAdjacentInv;
    }

    private IInventory findAdjacentInventory() {
        int[][] offsets = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
        for (int[] off : offsets) {
            TileEntity te = worldObj.getBlockTileEntity(xCoord + off[0], yCoord + off[1], zCoord + off[2]);
            if (te instanceof IInventory && !(te instanceof BE_INetworkNode)) {
                return (IInventory) te;
            }
        }
        return null;
    }

    // IInventory — buffer slots
    @Override
    public int getSizeInventory() { return BUFFER_SLOTS; }
    @Override
    public ItemStack getStackInSlot(int slot) {
        return (slot >= 0 && slot < BUFFER_SLOTS) ? buffer[slot] : null;
    }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= BUFFER_SLOTS || buffer[slot] == null) return null;
        if (amount >= buffer[slot].stackSize) {
            ItemStack old = buffer[slot];
            buffer[slot] = null;
            return old;
        }
        ItemStack split = buffer[slot].splitStack(amount);
        if (buffer[slot].stackSize <= 0) buffer[slot] = null;
        return split;
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot >= 0 && slot < BUFFER_SLOTS) buffer[slot] = stack;
    }
    @Override
    public String getInvName() { return "BE Advanced Interface"; }
    @Override
    public int getInventoryStackLimit() { return 64; }
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }
    @Override
    public void onInventoryChanged() { super.onInventoryChanged(); }

    // Network
    @Override
    public int getEnergyUsage() { return ENERGY_USAGE; }
    @Override
    public void onNetworkJoin(BE_StorageNetwork network) { this.network = network; }
    @Override
    public void onNetworkLeave() { this.network = null; }
    @Override
    public BE_StorageNetwork getNetwork() { return network; }
    @Override
    public TileEntity getTileEntity() { return this; }
    @Override
    public boolean canConnectOnSide(int side) { return true; }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        NBTTagList bufList = tag.getTagList("Buffer");
        for (int i = 0; i < bufList.tagCount() && i < BUFFER_SLOTS; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) bufList.tagAt(i);
            if (slotTag.hasKey("id")) {
                buffer[i] = new ItemStack(slotTag);
            }
        }

        expectedOutputId = tag.getInteger("ExpOutId");
        expectedOutputDmg = tag.getInteger("ExpOutDmg");
        expectedOutputCount = tag.getInteger("ExpOutCnt");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagList bufList = new NBTTagList();
        for (int i = 0; i < BUFFER_SLOTS; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (buffer[i] != null) buffer[i].writeToNBT(slotTag);
            bufList.setTag(slotTag);
        }
        tag.setTag("Buffer", bufList);

        tag.setInteger("ExpOutId", expectedOutputId);
        tag.setInteger("ExpOutDmg", expectedOutputDmg);
        tag.setInteger("ExpOutCnt", expectedOutputCount);
    }
}
