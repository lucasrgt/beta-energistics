package betaenergistics.tile;

import betaenergistics.item.BE_ItemPattern;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Autocrafter — stores patterns and executes crafting jobs.
 * Has 9 pattern slots. Each tick, tries to execute patterns if requested.
 * Pulls ingredients from network, pushes results back.
 */
public class BE_TileAutocrafter extends TileEntity implements BE_INetworkNode, IInventory {
    private static final int ENERGY_USAGE = 4;
    private static final int PATTERN_SLOTS = 9;
    private static final int CRAFT_TICKS = 20; // 1 second per craft

    private ItemStack[] patternSlots = new ItemStack[PATTERN_SLOTS];
    private BE_StorageNetwork network;

    // Active craft state
    private int activeCraftIndex = -1;  // which pattern is crafting
    private int craftProgress = 0;

    @Override
    public void updateEntity() {
        if (worldObj.isRemote || network == null || !network.isActive()) return;

        if (activeCraftIndex >= 0) {
            craftProgress++;
            if (craftProgress >= CRAFT_TICKS) {
                completeCraft();
                activeCraftIndex = -1;
                craftProgress = 0;
            }
        }
    }

    /**
     * Request a craft of a specific pattern index.
     * Called by the network when auto-crafting is triggered.
     * Returns true if the craft was started (ingredients consumed).
     */
    public boolean startCraft(int patternIndex) {
        if (activeCraftIndex >= 0) return false; // already crafting
        if (patternIndex < 0 || patternIndex >= PATTERN_SLOTS) return false;
        if (!BE_ItemPattern.isEncoded(patternSlots[patternIndex])) return false;

        ItemStack[] inputs = BE_ItemPattern.getInputs(patternSlots[patternIndex]);

        // Aggregate ingredients needed
        Map<BE_ItemKey, Integer> needed = new HashMap<BE_ItemKey, Integer>();
        for (ItemStack input : inputs) {
            if (input == null) continue;
            BE_ItemKey key = new BE_ItemKey(input.itemID, input.getItemDamage());
            Integer current = needed.get(key);
            needed.put(key, (current != null ? current : 0) + input.stackSize);
        }

        // Check if network has all ingredients (simulate)
        for (Map.Entry<BE_ItemKey, Integer> entry : needed.entrySet()) {
            int available = network.getRootStorage().extract(entry.getKey(), entry.getValue(), true);
            if (available < entry.getValue()) return false;
        }

        // Consume ingredients
        for (Map.Entry<BE_ItemKey, Integer> entry : needed.entrySet()) {
            network.getRootStorage().extract(entry.getKey(), entry.getValue(), false);
        }

        activeCraftIndex = patternIndex;
        craftProgress = 0;
        return true;
    }

    private void completeCraft() {
        if (activeCraftIndex < 0 || !BE_ItemPattern.isEncoded(patternSlots[activeCraftIndex])) return;

        ItemStack output = BE_ItemPattern.getOutput(patternSlots[activeCraftIndex]);
        if (output == null) return;

        BE_ItemKey key = new BE_ItemKey(output.itemID, output.getItemDamage());
        network.getRootStorage().insert(key, output.stackSize, false);
    }

    /** Find a pattern that produces the given item. Returns pattern index or -1. */
    public int findPattern(BE_ItemKey outputKey) {
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (!BE_ItemPattern.isEncoded(patternSlots[i])) continue;
            ItemStack output = BE_ItemPattern.getOutput(patternSlots[i]);
            if (output != null && output.itemID == outputKey.itemId && output.getItemDamage() == outputKey.damageValue) {
                return i;
            }
        }
        return -1;
    }

    public boolean isCrafting() { return activeCraftIndex >= 0; }
    public int getCraftProgress() { return craftProgress; }
    public int getCraftProgressScaled(int scale) { return craftProgress * scale / CRAFT_TICKS; }

    // IInventory
    @Override
    public int getSizeInventory() { return PATTERN_SLOTS; }
    @Override
    public ItemStack getStackInSlot(int slot) { return patternSlots[slot]; }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (patternSlots[slot] == null) return null;
        ItemStack stack = patternSlots[slot];
        patternSlots[slot] = null;
        return stack;
    }
    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = patternSlots[slot];
        patternSlots[slot] = null;
        return stack;
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) { patternSlots[slot] = stack; }
    @Override
    public String getInvName() { return "BE Autocrafter"; }
    @Override
    public int getInventoryStackLimit() { return 1; }
    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }
    @Override
    public void openChest() {}
    @Override
    public void closeChest() {}

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
        NBTTagList list = tag.getTagList("Patterns");
        for (int i = 0; i < list.tagCount() && i < PATTERN_SLOTS; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) list.tagAt(i);
            if (slotTag.hasKey("id")) {
                patternSlots[i] = ItemStack.loadItemStackFromNBT(slotTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (patternSlots[i] != null) patternSlots[i].writeToNBT(slotTag);
            list.tagList.add(slotTag);
        }
        tag.setTag("Patterns", list);
    }
}
