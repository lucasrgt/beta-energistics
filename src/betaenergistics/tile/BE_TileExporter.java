package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.*;

/**
 * Export Bus — pushes configured items from the network into adjacent inventory.
 * Has 9 filter slots to configure which items to export.
 */
public class BE_TileExporter extends TileEntity implements BE_INetworkNode, IInventory {
    private static final int ENERGY_USAGE = 2;
    private static final int ITEMS_PER_TICK = 4;
    private static final int TICK_INTERVAL = 10;

    private BE_StorageNetwork network;
    private int tickCounter = 0;
    private ItemStack[] filterSlots = new ItemStack[9]; // ghost slots (only used for filtering)

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld || network == null || !network.isActive()) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        IInventory adjacent = findAdjacentInventory();
        if (adjacent == null) return;

        int exported = 0;
        for (int f = 0; f < filterSlots.length && exported < ITEMS_PER_TICK; f++) {
            if (filterSlots[f] == null) continue;

            BE_ItemKey key = new BE_ItemKey(filterSlots[f].itemID, filterSlots[f].getItemDamage());
            int toExport = ITEMS_PER_TICK - exported;

            // Check if network has this item
            int available = network.getRootStorage().extract(key, toExport, true);
            if (available <= 0) continue;

            // Try to place into adjacent inventory
            int placed = placeIntoInventory(adjacent, key, available);
            if (placed > 0) {
                network.getRootStorage().extract(key, placed, false);
                exported += placed;
            }
        }
    }

    private int placeIntoInventory(IInventory inv, BE_ItemKey key, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inv.getSizeInventory() && remaining > 0; slot++) {
            ItemStack existing = inv.getStackInSlot(slot);
            if (existing == null) {
                int toPlace = Math.min(remaining, 64);
                inv.setInventorySlotContents(slot, new ItemStack(key.itemId, toPlace, key.damageValue));
                remaining -= toPlace;
            } else if (existing.itemID == key.itemId && existing.getItemDamage() == key.damageValue) {
                int space = existing.getMaxStackSize() - existing.stackSize;
                int toPlace = Math.min(remaining, space);
                if (toPlace > 0) {
                    existing.stackSize += toPlace;
                    remaining -= toPlace;
                }
            }
        }
        inv.onInventoryChanged();
        return amount - remaining;
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

    // IInventory (filter slots — ghost items, not real)
    @Override
    public int getSizeInventory() { return 9; }
    @Override
    public ItemStack getStackInSlot(int slot) { return filterSlots[slot]; }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        filterSlots[slot] = null;
        return null;
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (stack != null) {
            filterSlots[slot] = new ItemStack(stack.itemID, 1, stack.getItemDamage());
        } else {
            filterSlots[slot] = null;
        }
    }
    @Override
    public String getInvName() { return "BE Exporter"; }
    @Override
    public int getInventoryStackLimit() { return 1; }
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }
    @Override
    public void onInventoryChanged() {
        super.onInventoryChanged();
    }

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
        NBTTagList list = tag.getTagList("Filters");
        for (int i = 0; i < list.tagCount() && i < 9; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) list.tagAt(i);
            if (slotTag.hasKey("id")) {
                filterSlots[i] = new ItemStack(slotTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 9; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (filterSlots[i] != null) filterSlots[i].writeToNBT(slotTag);
            list.setTag(slotTag);
        }
        tag.setTag("Filters", list);
    }
}
