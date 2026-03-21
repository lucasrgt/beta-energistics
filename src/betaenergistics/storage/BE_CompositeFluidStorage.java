package betaenergistics.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates multiple fluid storages into a unified view.
 * Insert goes to highest-priority storage with space.
 * Extract pulls from first storage that has the fluid.
 * getAllFluids() merges all storages into one combined view.
 */
public class BE_CompositeFluidStorage {
    private final List<BE_IFluidStorage> storages = new ArrayList<BE_IFluidStorage>();
    private boolean needsSort = false;

    public void addStorage(BE_IFluidStorage storage) {
        storages.add(storage);
        needsSort = true;
    }

    public void removeStorage(BE_IFluidStorage storage) {
        storages.remove(storage);
    }

    public void clear() {
        storages.clear();
    }

    private void ensureSorted() {
        if (needsSort) {
            Collections.sort(storages, new Comparator<BE_IFluidStorage>() {
                public int compare(BE_IFluidStorage a, BE_IFluidStorage b) {
                    return b.getPriority() - a.getPriority();
                }
            });
            needsSort = false;
        }
    }

    public void markDirty() {
        needsSort = true;
    }

    public int insertFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        ensureSorted();
        int remaining = amountMB;
        for (BE_IFluidStorage storage : storages) {
            if (remaining <= 0) break;
            int inserted = storage.insertFluid(key, remaining, simulate);
            remaining -= inserted;
        }
        return amountMB - remaining;
    }

    public int extractFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        ensureSorted();
        int remaining = amountMB;
        for (BE_IFluidStorage storage : storages) {
            if (remaining <= 0) break;
            int extracted = storage.extractFluid(key, remaining, simulate);
            remaining -= extracted;
        }
        return amountMB - remaining;
    }

    public int getFluidCount(BE_FluidKey key) {
        int total = 0;
        for (BE_IFluidStorage storage : storages) {
            total += storage.getFluidCount(key);
        }
        return total;
    }

    public Map<BE_FluidKey, Integer> getAllFluids() {
        Map<BE_FluidKey, Integer> merged = new HashMap<BE_FluidKey, Integer>();
        for (BE_IFluidStorage storage : storages) {
            for (Map.Entry<BE_FluidKey, Integer> entry : storage.getAllFluids().entrySet()) {
                BE_FluidKey key = entry.getKey();
                Integer existing = merged.get(key);
                merged.put(key, (existing != null ? existing : 0) + entry.getValue());
            }
        }
        return merged;
    }

    public int getTotalStored() {
        int total = 0;
        for (BE_IFluidStorage s : storages) total += s.getStored();
        return total;
    }

    public int getTotalCapacity() {
        int total = 0;
        for (BE_IFluidStorage s : storages) total += s.getCapacity();
        return total;
    }

    public int getStorageCount() {
        return storages.size();
    }
}
