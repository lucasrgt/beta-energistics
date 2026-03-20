package betaenergistics.crafting;

import betaenergistics.storage.BE_ItemKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of a crafting calculation. Describes what items to take from network,
 * what sub-crafts are needed, and what items are missing.
 */
public class BE_CraftingPlan {
    /** Items to extract from network storage. */
    public final Map<BE_ItemKey, Integer> itemsToTake = new HashMap<BE_ItemKey, Integer>();

    /** Items that need to be crafted (intermediate products). Key = output, value = quantity. */
    public final Map<BE_ItemKey, Integer> itemsToCraft = new HashMap<BE_ItemKey, Integer>();

    /** Items that are missing (not in storage and no pattern available). */
    public final Map<BE_ItemKey, Integer> missing = new HashMap<BE_ItemKey, Integer>();

    /** Whether the plan can be fully executed (no missing items). */
    public boolean isComplete() {
        return missing.isEmpty();
    }

    public void addToTake(BE_ItemKey key, int amount) {
        Integer current = itemsToTake.get(key);
        itemsToTake.put(key, (current != null ? current : 0) + amount);
    }

    public void addToCraft(BE_ItemKey key, int amount) {
        Integer current = itemsToCraft.get(key);
        itemsToCraft.put(key, (current != null ? current : 0) + amount);
    }

    public void addMissing(BE_ItemKey key, int amount) {
        Integer current = missing.get(key);
        missing.put(key, (current != null ? current : 0) + amount);
    }

    /** Merge another plan's results into this one. */
    public void merge(BE_CraftingPlan other) {
        for (Map.Entry<BE_ItemKey, Integer> entry : other.itemsToTake.entrySet()) {
            addToTake(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<BE_ItemKey, Integer> entry : other.itemsToCraft.entrySet()) {
            addToCraft(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<BE_ItemKey, Integer> entry : other.missing.entrySet()) {
            addMissing(entry.getKey(), entry.getValue());
        }
    }
}
