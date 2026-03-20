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
 *
 * In Beta 1.7.3, ItemStack has no NBT tags. Recipe data (inputs + output)
 * is stored directly in this tile's NBT per pattern slot. A pattern item
 * is just a blank marker; the Autocrafter assigns the recipe to its slot.
 */
public class BE_TileAutocrafter extends TileEntity implements BE_INetworkNode, IInventory {
    private static final int ENERGY_USAGE = 4;
    private static final int PATTERN_SLOTS = 9;
    private static final int CRAFT_TICKS = 20; // 1 second per craft

    private ItemStack[] patternSlots = new ItemStack[PATTERN_SLOTS];
    private BE_StorageNetwork network;

    // Per-slot recipe data (stored in tile NBT, not on items)
    private int[][] recipeInputIds = new int[PATTERN_SLOTS][9];
    private int[][] recipeInputDmg = new int[PATTERN_SLOTS][9];
    private int[][] recipeInputCount = new int[PATTERN_SLOTS][9];
    private int[] recipeOutputId = new int[PATTERN_SLOTS];
    private int[] recipeOutputDmg = new int[PATTERN_SLOTS];
    private int[] recipeOutputCount = new int[PATTERN_SLOTS];

    // Active craft state
    private int activeCraftIndex = -1;  // which pattern is crafting
    private int craftProgress = 0;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld || network == null || !network.isActive()) return;

        if (activeCraftIndex >= 0) {
            craftProgress++;
            if (craftProgress >= CRAFT_TICKS) {
                completeCraft();
                activeCraftIndex = -1;
                craftProgress = 0;
            }
        }
    }

    /** Check if a pattern slot has an encoded recipe. */
    public boolean isPatternEncoded(int slot) {
        if (slot < 0 || slot >= PATTERN_SLOTS) return false;
        if (patternSlots[slot] == null) return false;
        return recipeOutputId[slot] > 0;
    }

    /** Encode a recipe into a pattern slot. The slot must contain a pattern item. */
    public void encodePattern(int slot, ItemStack[] inputs, ItemStack output) {
        if (slot < 0 || slot >= PATTERN_SLOTS) return;
        if (patternSlots[slot] == null) return;

        // Clear previous data
        for (int i = 0; i < 9; i++) {
            recipeInputIds[slot][i] = 0;
            recipeInputDmg[slot][i] = 0;
            recipeInputCount[slot][i] = 0;
        }

        // Store inputs
        for (int i = 0; i < 9 && i < inputs.length; i++) {
            if (inputs[i] != null) {
                recipeInputIds[slot][i] = inputs[i].itemID;
                recipeInputDmg[slot][i] = inputs[i].getItemDamage();
                recipeInputCount[slot][i] = inputs[i].stackSize;
            }
        }

        // Store output
        if (output != null) {
            recipeOutputId[slot] = output.itemID;
            recipeOutputDmg[slot] = output.getItemDamage();
            recipeOutputCount[slot] = output.stackSize;
        }
    }

    /** Get the output item for a pattern slot. */
    public ItemStack getPatternOutput(int slot) {
        if (!isPatternEncoded(slot)) return null;
        return new ItemStack(recipeOutputId[slot], recipeOutputCount[slot], recipeOutputDmg[slot]);
    }

    /** Get the input items for a pattern slot (9 slots, some may be null). */
    public ItemStack[] getPatternInputs(int slot) {
        ItemStack[] inputs = new ItemStack[9];
        if (!isPatternEncoded(slot)) return inputs;
        for (int i = 0; i < 9; i++) {
            if (recipeInputIds[slot][i] > 0) {
                inputs[i] = new ItemStack(recipeInputIds[slot][i], recipeInputCount[slot][i], recipeInputDmg[slot][i]);
            }
        }
        return inputs;
    }

    /** Clear recipe data for a slot (e.g. when pattern item is removed). */
    private void clearRecipe(int slot) {
        for (int i = 0; i < 9; i++) {
            recipeInputIds[slot][i] = 0;
            recipeInputDmg[slot][i] = 0;
            recipeInputCount[slot][i] = 0;
        }
        recipeOutputId[slot] = 0;
        recipeOutputDmg[slot] = 0;
        recipeOutputCount[slot] = 0;
    }

    /**
     * Request a craft of a specific pattern index.
     * Called by the network when auto-crafting is triggered.
     * Returns true if the craft was started (ingredients consumed).
     */
    public boolean startCraft(int patternIndex) {
        if (activeCraftIndex >= 0) return false; // already crafting
        if (patternIndex < 0 || patternIndex >= PATTERN_SLOTS) return false;
        if (!isPatternEncoded(patternIndex)) return false;

        ItemStack[] inputs = getPatternInputs(patternIndex);

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
        if (activeCraftIndex < 0 || !isPatternEncoded(activeCraftIndex)) return;

        ItemStack output = getPatternOutput(activeCraftIndex);
        if (output == null) return;

        BE_ItemKey key = new BE_ItemKey(output.itemID, output.getItemDamage());
        network.getRootStorage().insert(key, output.stackSize, false);
    }

    /** Find a pattern that produces the given item. Returns pattern index or -1. */
    public int findPattern(BE_ItemKey outputKey) {
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (!isPatternEncoded(i)) continue;
            if (recipeOutputId[i] == outputKey.itemId && recipeOutputDmg[i] == outputKey.damageValue) {
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
        clearRecipe(slot);
        return stack;
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        patternSlots[slot] = stack;
        if (stack == null) {
            clearRecipe(slot);
        }
    }
    @Override
    public String getInvName() { return "BE Autocrafter"; }
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

        // Load pattern items
        NBTTagList patList = tag.getTagList("Patterns");
        for (int i = 0; i < patList.tagCount() && i < PATTERN_SLOTS; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) patList.tagAt(i);
            if (slotTag.hasKey("id")) {
                patternSlots[i] = new ItemStack(slotTag);
            }
        }

        // Load per-slot recipe data
        NBTTagList recipeList = tag.getTagList("Recipes");
        for (int i = 0; i < recipeList.tagCount() && i < PATTERN_SLOTS; i++) {
            NBTTagCompound recTag = (NBTTagCompound) recipeList.tagAt(i);
            for (int j = 0; j < 9; j++) {
                recipeInputIds[i][j] = recTag.getInteger("inId" + j);
                recipeInputDmg[i][j] = recTag.getInteger("inDm" + j);
                recipeInputCount[i][j] = recTag.getInteger("inCn" + j);
            }
            recipeOutputId[i] = recTag.getInteger("outId");
            recipeOutputDmg[i] = recTag.getInteger("outDmg");
            recipeOutputCount[i] = recTag.getInteger("outCnt");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        // Save pattern items
        NBTTagList patList = new NBTTagList();
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (patternSlots[i] != null) patternSlots[i].writeToNBT(slotTag);
            patList.setTag(slotTag);
        }
        tag.setTag("Patterns", patList);

        // Save per-slot recipe data
        NBTTagList recipeList = new NBTTagList();
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            NBTTagCompound recTag = new NBTTagCompound();
            for (int j = 0; j < 9; j++) {
                recTag.setInteger("inId" + j, recipeInputIds[i][j]);
                recTag.setInteger("inDm" + j, recipeInputDmg[i][j]);
                recTag.setInteger("inCn" + j, recipeInputCount[i][j]);
            }
            recTag.setInteger("outId", recipeOutputId[i]);
            recTag.setInteger("outDmg", recipeOutputDmg[i]);
            recTag.setInteger("outCnt", recipeOutputCount[i]);
            recipeList.setTag(recTag);
        }
        tag.setTag("Recipes", recipeList);
    }
}
