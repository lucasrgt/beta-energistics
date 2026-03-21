package betaenergistics.storage;

import java.util.Map;

/**
 * Aggregates multiple fluid storages (BE_IFluidStorage) into a unified view.
 * Insert goes to highest-priority storage with space.
 * Extract pulls from first storage that has the fluid.
 */
public class BE_CompositeFluidStorage extends BE_CompositeStorageBase {

    public void addStorage(BE_IFluidStorage storage) {
        addStorageImpl(storage);
    }

    public void removeStorage(BE_IFluidStorage storage) {
        removeStorageImpl(storage);
    }

    @Override
    protected int getStoragePriority(Object storage) {
        return ((BE_IFluidStorage) storage).getPriority();
    }

    @Override
    protected int doInsert(Object storage, Object key, int amount, boolean simulate) {
        return ((BE_IFluidStorage) storage).insertFluid((BE_FluidKey) key, amount, simulate);
    }

    @Override
    protected int doExtract(Object storage, Object key, int amount, boolean simulate) {
        return ((BE_IFluidStorage) storage).extractFluid((BE_FluidKey) key, amount, simulate);
    }

    @Override
    protected int doGetCount(Object storage, Object key) {
        return ((BE_IFluidStorage) storage).getFluidCount((BE_FluidKey) key);
    }

    @Override
    protected Map doGetAll(Object storage) {
        return ((BE_IFluidStorage) storage).getAllFluids();
    }

    @Override
    protected int doGetStored(Object storage) {
        return ((BE_IFluidStorage) storage).getStored();
    }

    @Override
    protected int doGetCapacity(Object storage) {
        return ((BE_IFluidStorage) storage).getCapacity();
    }

    // Public typed API

    public int insertFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        return insertAll(key, amountMB, simulate);
    }

    public int extractFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        return extractAll(key, amountMB, simulate);
    }

    public int getFluidCount(BE_FluidKey key) {
        return getCountAll(key);
    }

    public Map<BE_FluidKey, Integer> getAllFluids() {
        return getAllMerged();
    }
}
