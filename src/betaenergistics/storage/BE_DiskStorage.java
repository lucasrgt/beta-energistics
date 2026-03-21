package betaenergistics.storage;

import java.util.Map;

import net.minecraft.src.NBTTagCompound;

/**
 * Item disk storage. Stores items as Map<ItemKey, Integer> with capacity and type limits.
 * Extends BE_DiskStorageBase for shared insert/extract/NBT logic.
 */
public class BE_DiskStorage extends BE_DiskStorageBase implements BE_IStorage {
    public static final int MAX_TYPES = 63;

    public BE_DiskStorage(int capacity) {
        super(capacity);
    }

    @Override
    protected int getMaxTypes() {
        return MAX_TYPES;
    }

    @Override
    protected String getContentTag() {
        return "items";
    }

    @Override
    protected String getAmountTag() {
        return "count";
    }

    @Override
    protected Object readKey(NBTTagCompound tag) {
        return new BE_ItemKey(tag.getInteger("id"), tag.getInteger("dmg"));
    }

    @Override
    protected void writeKey(NBTTagCompound tag, Object key) {
        BE_ItemKey k = (BE_ItemKey) key;
        tag.setInteger("id", k.itemId);
        tag.setInteger("dmg", k.damageValue);
    }

    // BE_IStorage interface delegation

    public int insert(BE_ItemKey key, int amount, boolean simulate) {
        return insertImpl(key, amount, simulate);
    }

    public int extract(BE_ItemKey key, int amount, boolean simulate) {
        return extractImpl(key, amount, simulate);
    }

    public int getCount(BE_ItemKey key) {
        return getCountImpl(key);
    }

    public Map<BE_ItemKey, Integer> getAll() {
        return getAllImpl();
    }

    // Explicit overrides to satisfy BE_IStorage interface (raw type erasure workaround)
    @Override
    public int getStored() { return super.getStored(); }

    @Override
    public int getCapacity() { return super.getCapacity(); }

    @Override
    public int getPriority() { return super.getPriority(); }
}
