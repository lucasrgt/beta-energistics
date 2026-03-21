package betaenergistics.storage;

import java.util.Map;

/**
 * Aggregates multiple gas storages (BE_IGasStorage) into a unified view.
 * Implements BE_IGasStorage so it can be used as a storage itself.
 */
public class BE_CompositeGasStorage extends BE_CompositeStorageBase implements BE_IGasStorage {

    public void addStorage(BE_IGasStorage storage) {
        addStorageImpl(storage);
    }

    public void removeStorage(BE_IGasStorage storage) {
        removeStorageImpl(storage);
    }

    @Override
    protected int getStoragePriority(Object storage) {
        return ((BE_IGasStorage) storage).getPriority();
    }

    @Override
    protected int doInsert(Object storage, Object key, int amount, boolean simulate) {
        return ((BE_IGasStorage) storage).insertGas((BE_GasKey) key, amount, simulate);
    }

    @Override
    protected int doExtract(Object storage, Object key, int amount, boolean simulate) {
        return ((BE_IGasStorage) storage).extractGas((BE_GasKey) key, amount, simulate);
    }

    @Override
    protected int doGetCount(Object storage, Object key) {
        return ((BE_IGasStorage) storage).getGasAmount((BE_GasKey) key);
    }

    @Override
    protected Map doGetAll(Object storage) {
        return ((BE_IGasStorage) storage).getAllGases();
    }

    @Override
    protected int doGetStored(Object storage) {
        return 0; // IGasStorage doesn't have getStored
    }

    @Override
    protected int doGetCapacity(Object storage) {
        return 0; // IGasStorage doesn't have getCapacity
    }

    // BE_IGasStorage interface implementation

    public int insertGas(BE_GasKey key, int amountMB, boolean simulate) {
        return insertAll(key, amountMB, simulate);
    }

    public int extractGas(BE_GasKey key, int amountMB, boolean simulate) {
        return extractAll(key, amountMB, simulate);
    }

    public Map<BE_GasKey, Integer> getAllGases() {
        return getAllMerged();
    }

    public int getGasAmount(BE_GasKey key) {
        return getCountAll(key);
    }

    public int getPriority() { return 0; }
    public BE_AccessMode getAccessMode() { return BE_AccessMode.INSERT_EXTRACT; }
}
