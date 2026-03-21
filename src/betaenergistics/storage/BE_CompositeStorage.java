package betaenergistics.storage;

import java.util.Map;

/**
 * Aggregates multiple item storages (BE_IStorage) into a unified view.
 * Insert goes to highest-priority storage with space.
 * Extract pulls from first storage that has the item.
 */
public class BE_CompositeStorage extends BE_CompositeStorageBase {

    public void addStorage(BE_IStorage storage) {
        addStorageImpl(storage);
    }

    public void removeStorage(BE_IStorage storage) {
        removeStorageImpl(storage);
    }

    @Override
    protected int getStoragePriority(Object storage) {
        return ((BE_IStorage) storage).getPriority();
    }

    @Override
    protected int doInsert(Object storage, Object key, int amount, boolean simulate) {
        return ((BE_IStorage) storage).insert((BE_ItemKey) key, amount, simulate);
    }

    @Override
    protected int doExtract(Object storage, Object key, int amount, boolean simulate) {
        return ((BE_IStorage) storage).extract((BE_ItemKey) key, amount, simulate);
    }

    @Override
    protected int doGetCount(Object storage, Object key) {
        return ((BE_IStorage) storage).getCount((BE_ItemKey) key);
    }

    @Override
    protected Map doGetAll(Object storage) {
        return ((BE_IStorage) storage).getAll();
    }

    @Override
    protected int doGetStored(Object storage) {
        return ((BE_IStorage) storage).getStored();
    }

    @Override
    protected int doGetCapacity(Object storage) {
        return ((BE_IStorage) storage).getCapacity();
    }

    // Public typed API

    public int insert(BE_ItemKey key, int amount, boolean simulate) {
        return insertAll(key, amount, simulate);
    }

    public int extract(BE_ItemKey key, int amount, boolean simulate) {
        return extractAll(key, amount, simulate);
    }

    public int getCount(BE_ItemKey key) {
        return getCountAll(key);
    }

    public Map<BE_ItemKey, Integer> getAll() {
        return getAllMerged();
    }
}
