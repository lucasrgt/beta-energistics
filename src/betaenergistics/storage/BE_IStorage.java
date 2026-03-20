package betaenergistics.storage;

import java.util.Map;

/**
 * Common interface for all storage types (DiskStorage, ExternalStorage).
 * Used by CompositeStorage to aggregate different storage backends.
 */
public interface BE_IStorage {
    int insert(BE_ItemKey key, int amount, boolean simulate);
    int extract(BE_ItemKey key, int amount, boolean simulate);
    int getCount(BE_ItemKey key);
    Map<BE_ItemKey, Integer> getAll();
    int getStored();
    int getCapacity();
    int getPriority();
}
