package betaenergistics.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates multiple DiskStorages into a unified view.
 * Insert operations go to the highest-priority storage with space.
 * Extract operations pull from the first storage that has the item.
 * getAll() merges all storages into one combined view.
 *
 * This is the RS2 CompositeStorage pattern.
 */
public class BE_CompositeStorage {
    private final List<BE_IStorage> storages = new ArrayList<BE_IStorage>();
    private boolean needsSort = false;

    public void addStorage(BE_IStorage storage) {
        storages.add(storage);
        needsSort = true;
    }

    public void removeStorage(BE_IStorage storage) {
        storages.remove(storage);
    }

    public void clear() {
        storages.clear();
    }

    private void ensureSorted() {
        if (needsSort) {
            Collections.sort(storages, new Comparator<BE_IStorage>() {
                public int compare(BE_IStorage a, BE_IStorage b) {
                    return b.getPriority() - a.getPriority(); // descending
                }
            });
            needsSort = false;
        }
    }

    public void markDirty() {
        needsSort = true;
    }

    /**
     * Insert into highest-priority storage that has space.
     * Spills into next storage if first one fills up.
     */
    public int insert(BE_ItemKey key, int amount, boolean simulate) {
        ensureSorted();
        int remaining = amount;
        for (BE_IStorage storage : storages) {
            if (remaining <= 0) break;
            int inserted = storage.insert(key, remaining, simulate);
            remaining -= inserted;
        }
        return amount - remaining;
    }

    /**
     * Extract from first storage that has the item.
     * Pulls from multiple storages if needed.
     */
    public int extract(BE_ItemKey key, int amount, boolean simulate) {
        ensureSorted();
        int remaining = amount;
        for (BE_IStorage storage : storages) {
            if (remaining <= 0) break;
            int extracted = storage.extract(key, remaining, simulate);
            remaining -= extracted;
        }
        return amount - remaining;
    }

    /**
     * Get total count of a specific item across all storages.
     */
    public int getCount(BE_ItemKey key) {
        int total = 0;
        for (BE_IStorage storage : storages) {
            total += storage.getCount(key);
        }
        return total;
    }

    /**
     * Merge all storages into one combined view.
     * Used by the Grid Terminal to show all items.
     */
    public Map<BE_ItemKey, Integer> getAll() {
        Map<BE_ItemKey, Integer> merged = new HashMap<BE_ItemKey, Integer>();
        for (BE_IStorage storage : storages) {
            for (Map.Entry<BE_ItemKey, Integer> entry : storage.getAll().entrySet()) {
                BE_ItemKey key = entry.getKey();
                Integer existing = merged.get(key);
                merged.put(key, (existing != null ? existing : 0) + entry.getValue());
            }
        }
        return merged;
    }

    public int getTotalStored() {
        int total = 0;
        for (BE_IStorage s : storages) total += s.getStored();
        return total;
    }

    public int getTotalCapacity() {
        int total = 0;
        for (BE_IStorage s : storages) total += s.getCapacity();
        return total;
    }

    public int getStorageCount() {
        return storages.size();
    }
}
