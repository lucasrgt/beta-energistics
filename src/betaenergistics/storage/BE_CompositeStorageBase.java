package betaenergistics.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic base for composite storages that aggregate multiple sub-storages.
 * Insert goes to highest-priority storage with space.
 * Extract pulls from first storage that has the resource.
 * getAllMerged() merges all storages into one combined view.
 *
 * Subclasses provide type-specific insert/extract/getAll delegation.
 */
public abstract class BE_CompositeStorageBase {
    protected final List storages = new ArrayList();
    protected boolean needsSort = false;

    public void addStorageImpl(Object storage) {
        storages.add(storage);
        needsSort = true;
    }

    public void removeStorageImpl(Object storage) {
        storages.remove(storage);
    }

    public void clear() {
        storages.clear();
    }

    public void markDirty() {
        needsSort = true;
    }

    public int getStorageCount() {
        return storages.size();
    }

    /** Get the priority of a storage element. */
    protected abstract int getStoragePriority(Object storage);

    /** Insert into a single storage. Returns amount inserted. */
    protected abstract int doInsert(Object storage, Object key, int amount, boolean simulate);

    /** Extract from a single storage. Returns amount extracted. */
    protected abstract int doExtract(Object storage, Object key, int amount, boolean simulate);

    /** Get count of a key in a single storage. */
    protected abstract int doGetCount(Object storage, Object key);

    /** Get all entries from a single storage as a Map. */
    protected abstract Map doGetAll(Object storage);

    /** Get stored amount from a single storage. */
    protected abstract int doGetStored(Object storage);

    /** Get capacity from a single storage. */
    protected abstract int doGetCapacity(Object storage);

    protected void ensureSorted() {
        if (needsSort) {
            Collections.sort(storages, new Comparator() {
                public int compare(Object a, Object b) {
                    return getStoragePriority(b) - getStoragePriority(a); // descending
                }
            });
            needsSort = false;
        }
    }

    protected int insertAll(Object key, int amount, boolean simulate) {
        ensureSorted();
        int remaining = amount;
        for (int i = 0; i < storages.size(); i++) {
            if (remaining <= 0) break;
            int inserted = doInsert(storages.get(i), key, remaining, simulate);
            remaining -= inserted;
        }
        return amount - remaining;
    }

    protected int extractAll(Object key, int amount, boolean simulate) {
        ensureSorted();
        int remaining = amount;
        for (int i = 0; i < storages.size(); i++) {
            if (remaining <= 0) break;
            int extracted = doExtract(storages.get(i), key, remaining, simulate);
            remaining -= extracted;
        }
        return amount - remaining;
    }

    protected int getCountAll(Object key) {
        int total = 0;
        for (int i = 0; i < storages.size(); i++) {
            total += doGetCount(storages.get(i), key);
        }
        return total;
    }

    protected Map getAllMerged() {
        Map merged = new HashMap();
        for (int i = 0; i < storages.size(); i++) {
            Map subMap = doGetAll(storages.get(i));
            java.util.Iterator it = subMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Object key = entry.getKey();
                Integer existing = (Integer) merged.get(key);
                int val = ((Integer) entry.getValue()).intValue();
                merged.put(key, Integer.valueOf((existing != null ? existing.intValue() : 0) + val));
            }
        }
        return merged;
    }

    public int getTotalStored() {
        int total = 0;
        for (int i = 0; i < storages.size(); i++) {
            total += doGetStored(storages.get(i));
        }
        return total;
    }

    public int getTotalCapacity() {
        int total = 0;
        for (int i = 0; i < storages.size(); i++) {
            total += doGetCapacity(storages.get(i));
        }
        return total;
    }
}
