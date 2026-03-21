package betaenergistics.storage;

import java.util.Map;

import net.minecraft.src.NBTTagCompound;

/**
 * Gas disk storage. Stores gases as Map<BE_GasKey, Integer> (mB) with capacity limit.
 * Extends BE_DiskStorageBase for shared insert/extract/NBT logic.
 */
public class BE_GasDiskStorage extends BE_DiskStorageBase implements BE_IGasStorage {

    public BE_GasDiskStorage(int capacity) {
        super(capacity);
    }

    @Override
    protected String getContentTag() {
        return "gases";
    }

    @Override
    protected String getAmountTag() {
        return "amount";
    }

    @Override
    protected Object readKey(NBTTagCompound tag) {
        return new BE_GasKey(tag.getInteger("type"));
    }

    @Override
    protected void writeKey(NBTTagCompound tag, Object key) {
        BE_GasKey k = (BE_GasKey) key;
        tag.setInteger("type", k.gasType);
    }

    // BE_IGasStorage interface delegation

    public int insertGas(BE_GasKey key, int amountMB, boolean simulate) {
        return insertImpl(key, amountMB, simulate);
    }

    public int extractGas(BE_GasKey key, int amountMB, boolean simulate) {
        return extractImpl(key, amountMB, simulate);
    }

    public Map<BE_GasKey, Integer> getAllGases() {
        return getAllImpl();
    }

    public int getGasAmount(BE_GasKey key) {
        return getCountImpl(key);
    }

    // Explicit override to satisfy BE_IGasStorage interface
    @Override
    public BE_AccessMode getAccessMode() { return super.getAccessMode(); }

    @Override
    public int getPriority() { return super.getPriority(); }
}
