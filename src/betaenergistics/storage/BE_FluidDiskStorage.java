package betaenergistics.storage;

import java.util.Map;

import net.minecraft.src.NBTTagCompound;

/**
 * Fluid disk storage. Stores fluids as Map<BE_FluidKey, Integer> (mB) with capacity limit.
 * Unlike item disks, fluid disks have no type limit.
 * Extends BE_DiskStorageBase for shared insert/extract/NBT logic.
 */
public class BE_FluidDiskStorage extends BE_DiskStorageBase implements BE_IFluidStorage {

    public BE_FluidDiskStorage(int capacity) {
        super(capacity);
    }

    @Override
    protected String getContentTag() {
        return "fluids";
    }

    @Override
    protected String getAmountTag() {
        return "amount";
    }

    @Override
    protected Object readKey(NBTTagCompound tag) {
        return new BE_FluidKey(tag.getInteger("type"));
    }

    @Override
    protected void writeKey(NBTTagCompound tag, Object key) {
        BE_FluidKey k = (BE_FluidKey) key;
        tag.setInteger("type", k.fluidType);
    }

    // BE_IFluidStorage interface delegation

    public int insertFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        return insertImpl(key, amountMB, simulate);
    }

    public int extractFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        return extractImpl(key, amountMB, simulate);
    }

    public int getFluidCount(BE_FluidKey key) {
        return getCountImpl(key);
    }

    public Map<BE_FluidKey, Integer> getAllFluids() {
        return getAllImpl();
    }

    // Explicit overrides to satisfy BE_IFluidStorage interface (raw type erasure workaround)
    @Override
    public int getStored() { return super.getStored(); }

    @Override
    public int getCapacity() { return super.getCapacity(); }

    @Override
    public int getPriority() { return super.getPriority(); }
}
