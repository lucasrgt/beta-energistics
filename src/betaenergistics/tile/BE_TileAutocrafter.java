package betaenergistics.tile;

import betaenergistics.crafting.BE_CraftingCalculator;
import betaenergistics.crafting.BE_CraftingPlan;
import betaenergistics.item.BE_ItemPattern;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.storage.BE_PatternRegistry;

import net.minecraft.src.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Autocrafter — stores encoded pattern items and executes crafting jobs.
 * Has 9 pattern slots. Each tick, tries to execute the active craft.
 * Pulls ingredients from network, pushes results back.
 *
 * Pattern items carry a damage value that maps to BE_PatternRegistry.
 * When an encoded pattern is placed in a slot, recipe data is loaded
 * from the registry into local arrays for fast access.
 */
public class BE_TileAutocrafter extends TileEntity implements BE_INetworkNode, IInventory {
    private static final int ENERGY_USAGE = 4;
    public static final int PATTERN_SLOTS = 9;
    public static final int PATTERN_START = 9; // pattern storage slots start at inventory index 9
    public static final int TOTAL_SLOTS = 19; // 9 crafting + 9 storage + 1 output
    private static final int CRAFT_TICKS = 20; // 1 second per craft
    private static final int TRIGGER_INTERVAL = 40; // check every 2 seconds

    private ItemStack[] patternSlots = new ItemStack[TOTAL_SLOTS];
    private BE_StorageNetwork network;

    // Per-slot recipe data (cached from PatternRegistry)
    private int[][] recipeInputIds = new int[PATTERN_SLOTS][9];
    private int[][] recipeInputDmg = new int[PATTERN_SLOTS][9];
    private int[][] recipeInputCount = new int[PATTERN_SLOTS][9];
    private int[] recipeOutputId = new int[PATTERN_SLOTS];
    private int[] recipeOutputDmg = new int[PATTERN_SLOTS];
    private int[] recipeOutputCount = new int[PATTERN_SLOTS];
    private boolean[] isProcessingPattern = new boolean[PATTERN_SLOTS];

    // Active craft state
    private int activeCraftIndex = -1;
    private int craftProgress = 0;
    private int triggerTimer = 0;

    // Crafting queue — per-slot remaining craft count
    private int[] craftQueue = new int[PATTERN_SLOTS];

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld || network == null || !network.isActive()) return;

        // Progress active craft
        if (activeCraftIndex >= 0) {
            craftProgress++;
            if (craftProgress >= CRAFT_TICKS) {
                completeCraft();
                activeCraftIndex = -1;
                craftProgress = 0;
            }
            return;
        }

        // Process queue: find next slot with pending crafts
        triggerTimer++;
        if (triggerTimer >= 4) { // check every 4 ticks
            triggerTimer = 0;
            for (int i = 0; i < PATTERN_SLOTS; i++) {
                if (craftQueue[i] <= 0) continue;
                if (startCraft(i)) {
                    craftQueue[i]--;
                    return;
                }
            }
        }
    }

    /**
     * Called when a pattern item is placed in a slot.
     * Loads recipe data from PatternRegistry if the pattern is encoded.
     */
    /**
     * Convert inventory slot index to recipe index.
     * Inventory slots 9-17 map to recipe indices 0-8.
     * Also accepts direct recipe indices 0-8 for internal use.
     */
    private int toRecipeIndex(int invSlot) {
        if (invSlot >= PATTERN_START && invSlot < PATTERN_START + PATTERN_SLOTS) {
            return invSlot - PATTERN_START;
        }
        if (invSlot >= 0 && invSlot < PATTERN_SLOTS) {
            return invSlot; // direct recipe index
        }
        return -1;
    }

    private void loadPatternFromRegistry(int invSlot) {
        int ri = toRecipeIndex(invSlot);
        if (ri < 0) return;
        clearRecipe(ri);
        ItemStack stack = patternSlots[PATTERN_START + ri];
        if (stack == null) return;
        if (!(stack.getItem() instanceof BE_ItemPattern)) return;

        int patternId = stack.getItemDamage();
        if (BE_PatternRegistry.isBlank(patternId)) return;
        if (!BE_PatternRegistry.isRegistered(patternId)) return;

        BE_PatternRegistry.PatternData data = BE_PatternRegistry.getPattern(patternId);
        if (data == null || data.output == null) return;

        for (int i = 0; i < 9; i++) {
            recipeInputIds[ri][i] = data.inputIds[i];
            recipeInputDmg[ri][i] = data.inputDmg[i];
            recipeInputCount[ri][i] = data.inputCount[i];
        }

        recipeOutputId[ri] = data.output.itemID;
        recipeOutputDmg[ri] = data.output.getItemDamage();
        recipeOutputCount[ri] = data.output.stackSize;
        isProcessingPattern[ri] = data.isProcessing();
    }

    /** Check if a pattern slot has an encoded recipe. Uses recipe index 0-8. */
    public boolean isPatternEncoded(int recipeIdx) {
        if (recipeIdx < 0 || recipeIdx >= PATTERN_SLOTS) return false;
        if (patternSlots[PATTERN_START + recipeIdx] == null) return false;
        return recipeOutputId[recipeIdx] > 0;
    }

    /** Encode a recipe into a pattern slot (legacy direct-encode path). */
    public void encodePattern(int slot, ItemStack[] inputs, ItemStack output) {
        if (slot < 0 || slot >= PATTERN_SLOTS) return;
        if (patternSlots[slot] == null) return;

        clearRecipe(slot);
        for (int i = 0; i < 9 && i < inputs.length; i++) {
            if (inputs[i] != null) {
                recipeInputIds[slot][i] = inputs[i].itemID;
                recipeInputDmg[slot][i] = inputs[i].getItemDamage();
                recipeInputCount[slot][i] = inputs[i].stackSize;
            }
        }
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

    /** Clear recipe data for a slot. */
    private void clearRecipe(int slot) {
        if (slot < 0 || slot >= PATTERN_SLOTS) return;
        for (int i = 0; i < 9; i++) {
            recipeInputIds[slot][i] = 0;
            recipeInputDmg[slot][i] = 0;
            recipeInputCount[slot][i] = 0;
        }
        recipeOutputId[slot] = 0;
        recipeOutputDmg[slot] = 0;
        recipeOutputCount[slot] = 0;
        isProcessingPattern[slot] = false;
    }

    /** Check if a pattern slot holds a processing pattern. */
    public boolean isProcessing(int slot) {
        if (slot < 0 || slot >= PATTERN_SLOTS) return false;
        return isProcessingPattern[slot];
    }

    /**
     * Request a craft of a specific pattern index.
     * Pulls ingredients from network storage and starts crafting.
     * For processing patterns, delegates to an Advanced Interface.
     * Returns true if the craft was started.
     */
    public boolean startCraft(int patternIndex) {
        if (activeCraftIndex >= 0) return false;
        if (patternIndex < 0 || patternIndex >= PATTERN_SLOTS) return false;
        if (!isPatternEncoded(patternIndex)) return false;
        if (network == null || !network.isActive()) return false;

        // Check network-wide concurrent craft limit (1 + coprocessors)
        if (network.getActiveCraftCount() >= network.getMaxConcurrentCrafts()) return false;

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

        // For processing patterns, find an available Advanced Interface
        if (isProcessingPattern[patternIndex]) {
            BE_TileAdvancedInterface iface = findAvailableInterface();
            if (iface == null) return false;

            // Consume ingredients
            for (Map.Entry<BE_ItemKey, Integer> entry : needed.entrySet()) {
                network.getRootStorage().extract(entry.getKey(), entry.getValue(), false);
            }

            // Build input stacks for the interface
            ItemStack[] jobInputs = getPatternInputs(patternIndex);
            ItemStack output = getPatternOutput(patternIndex);
            if (!iface.acceptProcessingJob(jobInputs, output)) {
                // Failed to accept — return ingredients to network
                for (Map.Entry<BE_ItemKey, Integer> entry : needed.entrySet()) {
                    network.getRootStorage().insert(entry.getKey(), entry.getValue(), false);
                }
                return false;
            }

            // Processing patterns don't use the internal craft timer — interface handles completion
            // Mark as briefly active so concurrent craft count is updated
            activeCraftIndex = patternIndex;
            craftProgress = CRAFT_TICKS - 1; // complete next tick
            return true;
        }

        // Standard crafting pattern: consume and craft internally
        for (Map.Entry<BE_ItemKey, Integer> entry : needed.entrySet()) {
            network.getRootStorage().extract(entry.getKey(), entry.getValue(), false);
        }

        activeCraftIndex = patternIndex;
        craftProgress = 0;
        return true;
    }

    /**
     * Find an Advanced Interface in the network that is not busy.
     */
    private BE_TileAdvancedInterface findAvailableInterface() {
        if (network == null) return null;
        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (te instanceof BE_TileAdvancedInterface) {
                BE_TileAdvancedInterface iface = (BE_TileAdvancedInterface) te;
                if (!iface.hasActiveJob()) return iface;
            }
        }
        return null;
    }

    /**
     * Queue craft requests from an external source (e.g., Grid Terminal).
     * @param patternIndex recipe index 0-8
     * @param quantity number of crafts to queue
     */
    public void requestCraft(int patternIndex, int quantity) {
        if (patternIndex >= 0 && patternIndex < PATTERN_SLOTS && isPatternEncoded(patternIndex)) {
            craftQueue[patternIndex] += Math.max(1, quantity);
        }
    }

    /** Queue a single craft (backwards compat). */
    public void requestCraft(int patternIndex) {
        requestCraft(patternIndex, 1);
    }

    /** Get total pending crafts across all slots. */
    public int getTotalPendingCrafts() {
        int total = 0;
        for (int i = 0; i < PATTERN_SLOTS; i++) total += craftQueue[i];
        if (activeCraftIndex >= 0) total++;
        return total;
    }

    /** Get pending crafts for a specific slot. */
    public int getPendingCrafts(int slot) {
        if (slot < 0 || slot >= PATTERN_SLOTS) return 0;
        return craftQueue[slot];
    }

    /** Cancel pending crafts for a specific slot. */
    public void cancelSlotCrafts(int slot) {
        if (slot >= 0 && slot < PATTERN_SLOTS) craftQueue[slot] = 0;
    }

    /** Cancel all pending crafts. */
    public void cancelAllCrafts() {
        for (int i = 0; i < PATTERN_SLOTS; i++) craftQueue[i] = 0;
    }

    private void completeCraft() {
        if (activeCraftIndex < 0 || !isPatternEncoded(activeCraftIndex)) return;

        // Processing patterns: output is handled by Advanced Interface, skip insertion
        if (isProcessingPattern[activeCraftIndex]) return;

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
    public int getActiveCraftIndex() { return activeCraftIndex; }
    public int getCraftProgress() { return craftProgress; }
    public int getCraftProgressScaled(int scale) { return craftProgress * scale / CRAFT_TICKS; }

    // IInventory
    @Override
    public int getSizeInventory() { return TOTAL_SLOTS; }
    @Override
    public ItemStack getStackInSlot(int slot) { return patternSlots[slot]; }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (patternSlots[slot] == null) return null;
        ItemStack stack = patternSlots[slot];
        patternSlots[slot] = null;
        int ri = toRecipeIndex(slot);
        if (ri >= 0) clearRecipe(ri);
        return stack;
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        patternSlots[slot] = stack;
        int ri = toRecipeIndex(slot);
        if (ri >= 0) {
            if (stack == null) {
                clearRecipe(ri);
            } else {
                loadPatternFromRegistry(slot);
            }
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

        // Load all inventory slots (patterns + crafting grid + output)
        NBTTagList patList = tag.getTagList("Patterns");
        for (int i = 0; i < patList.tagCount() && i < TOTAL_SLOTS; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) patList.tagAt(i);
            if (slotTag.hasKey("id")) {
                patternSlots[i] = new ItemStack(slotTag);
            }
        }

        // Load per-slot recipe data (fallback for patterns encoded before PatternRegistry)
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

        // Reload recipe data from PatternRegistry for pattern storage slots (9-17)
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            ItemStack stack = patternSlots[PATTERN_START + i];
            if (stack != null && stack.getItem() instanceof BE_ItemPattern) {
                int dmg = stack.getItemDamage();
                if (!BE_PatternRegistry.isBlank(dmg) && BE_PatternRegistry.isRegistered(dmg)) {
                    loadPatternFromRegistry(PATTERN_START + i);
                }
            }
        }

        // Load craft queue
        if (tag.hasKey("CraftQueue")) {
            NBTTagList queueList = tag.getTagList("CraftQueue");
            for (int i = 0; i < queueList.tagCount() && i < PATTERN_SLOTS; i++) {
                NBTTagCompound qTag = (NBTTagCompound) queueList.tagAt(i);
                craftQueue[i] = qTag.getInteger("q");
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        // Save all inventory slots
        NBTTagList patList = new NBTTagList();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
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

        // Save craft queue
        NBTTagList queueList = new NBTTagList();
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            NBTTagCompound qTag = new NBTTagCompound();
            qTag.setInteger("q", craftQueue[i]);
            queueList.setTag(qTag);
        }
        tag.setTag("CraftQueue", queueList);
    }
}
