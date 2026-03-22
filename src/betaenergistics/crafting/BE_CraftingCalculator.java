package betaenergistics.crafting;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_CompositeStorage;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileAutocrafter;

import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves crafting dependencies recursively.
 * Given a desired output and quantity, figures out which items to take from storage,
 * which sub-crafts are needed, and which items are missing.
 *
 * Pure logic class — no TileEntity, no world interaction beyond reading network state.
 */
public class BE_CraftingCalculator {

    private static final int MAX_RECURSION_DEPTH = 64;

    private final BE_StorageNetwork network;

    public BE_CraftingCalculator(BE_StorageNetwork network) {
        this.network = network;
    }

    /**
     * Calculate a crafting plan for the given output item and quantity.
     *
     * @param output   The desired item
     * @param quantity How many to craft
     * @return A CraftingPlan describing what's needed
     */
    public BE_CraftingPlan calculate(BE_ItemKey output, int quantity) {
        // Track available items (simulated extraction from storage)
        Map<BE_ItemKey, Integer> available = new HashMap<BE_ItemKey, Integer>();
        Map<BE_ItemKey, Integer> allItems = network.getRootStorage().getAll();
        for (Map.Entry<BE_ItemKey, Integer> entry : allItems.entrySet()) {
            available.put(entry.getKey(), entry.getValue());
        }

        BE_CraftingPlan plan = new BE_CraftingPlan();
        Set<BE_ItemKey> inProgress = new HashSet<BE_ItemKey>();
        resolve(output, quantity, available, plan, inProgress, 0);
        return plan;
    }

    /**
     * Recursively resolve an item requirement.
     *
     * @param key        Item needed
     * @param amount     Quantity needed
     * @param available  Simulated available items (decremented as we "take" them)
     * @param plan       Plan being built
     * @param inProgress Items currently being resolved (cycle detection)
     * @param depth      Current recursion depth
     */
    private void resolve(BE_ItemKey key, int amount, Map<BE_ItemKey, Integer> available,
                         BE_CraftingPlan plan, Set<BE_ItemKey> inProgress, int depth) {
        if (amount <= 0) return;

        // Check recursion depth
        if (depth > MAX_RECURSION_DEPTH) {
            plan.addMissing(key, amount);
            return;
        }

        // 1. Try to take from storage first
        int inStorage = available.containsKey(key) ? available.get(key) : 0;
        if (inStorage > 0) {
            int take = Math.min(inStorage, amount);
            plan.addToTake(key, take);
            available.put(key, inStorage - take);
            amount -= take;
        }

        if (amount <= 0) return;

        // 2. Check for infinite recursion (item is already being resolved up the call stack)
        if (inProgress.contains(key)) {
            plan.addMissing(key, amount);
            return;
        }

        // 3. Find a pattern that produces this item
        PatternMatch match = findPatternForOutput(key);
        if (match == null) {
            // No pattern available — item is missing
            plan.addMissing(key, amount);
            return;
        }

        // 4. Calculate how many crafts we need
        int outputPerCraft = match.outputCount;
        int craftsNeeded = (amount + outputPerCraft - 1) / outputPerCraft; // ceil division
        int totalProduced = craftsNeeded * outputPerCraft;
        int excess = totalProduced - amount;

        // Record this item as being crafted
        plan.addToCraft(key, totalProduced);

        // If there's excess, add it back to available pool for sibling dependencies
        if (excess > 0) {
            Integer prev = available.get(key);
            available.put(key, (prev != null ? prev : 0) + excess);
        }

        // 5. Mark as in-progress to detect cycles
        inProgress.add(key);

        // 6. Aggregate all inputs needed across all crafts
        Map<BE_ItemKey, Integer> totalInputs = new HashMap<BE_ItemKey, Integer>();
        for (int i = 0; i < 9; i++) {
            if (match.inputIds[i] > 0 && match.inputCounts[i] > 0) {
                BE_ItemKey inputKey = new BE_ItemKey(match.inputIds[i], match.inputDmg[i]);
                Integer current = totalInputs.get(inputKey);
                totalInputs.put(inputKey, (current != null ? current : 0) + match.inputCounts[i] * craftsNeeded);
            }
        }

        // 7. Recursively resolve each input
        for (Map.Entry<BE_ItemKey, Integer> input : totalInputs.entrySet()) {
            resolve(input.getKey(), input.getValue(), available, plan, inProgress, depth + 1);
        }

        // 8. Remove from in-progress
        inProgress.remove(key);
    }

    /**
     * Search all autocrafters in the network for a pattern that produces the given item.
     * Returns the first match found.
     */
    private PatternMatch findPatternForOutput(BE_ItemKey outputKey) {
        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (!(te instanceof BE_TileAutocrafter)) continue;

            BE_TileAutocrafter crafter = (BE_TileAutocrafter) te;
            int slot = crafter.findPattern(outputKey);
            if (slot < 0) continue;

            ItemStack[] inputs = crafter.getPatternInputs(slot);
            ItemStack output = crafter.getPatternOutput(slot);
            if (output == null) continue;

            PatternMatch match = new PatternMatch();
            match.crafter = crafter;
            match.slotIndex = slot;
            match.outputCount = output.stackSize;
            for (int i = 0; i < 9; i++) {
                if (inputs[i] != null) {
                    match.inputIds[i] = inputs[i].itemID;
                    match.inputDmg[i] = inputs[i].getItemDamage();
                    match.inputCounts[i] = inputs[i].stackSize;
                }
            }
            return match;
        }
        return null;
    }

    /**
     * Get all craftable items from all autocrafters in the network.
     * Returns a map of output ItemKey → list of autocrafters that can craft it.
     */
    public Map<BE_ItemKey, List<BE_TileAutocrafter>> getCraftableItems() {
        Map<BE_ItemKey, List<BE_TileAutocrafter>> craftable = new HashMap<BE_ItemKey, List<BE_TileAutocrafter>>();
        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (!(te instanceof BE_TileAutocrafter)) continue;

            BE_TileAutocrafter crafter = (BE_TileAutocrafter) te;
            for (int slot = 0; slot < BE_TileAutocrafter.PATTERN_SLOTS; slot++) {
                if (!crafter.isPatternEncoded(slot)) continue;
                ItemStack output = crafter.getPatternOutput(slot);
                if (output == null) continue;

                BE_ItemKey key = new BE_ItemKey(output.itemID, output.getItemDamage());
                List<BE_TileAutocrafter> list = craftable.get(key);
                if (list == null) {
                    list = new ArrayList<BE_TileAutocrafter>();
                    craftable.put(key, list);
                }
                if (!list.contains(crafter)) {
                    list.add(crafter);
                }
            }
        }
        return craftable;
    }

    /** Internal holder for a matched pattern. */
    private static class PatternMatch {
        BE_TileAutocrafter crafter;
        int slotIndex;
        int outputCount;
        int[] inputIds = new int[9];
        int[] inputDmg = new int[9];
        int[] inputCounts = new int[9];
    }
}
