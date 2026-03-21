package betaenergistics.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

/**
 * Generic abstract base for all disk storage types (item, fluid, gas).
 * Eliminates duplicated insert/extract/NBT logic across the 3 concrete classes.
 *
 * Subclasses only need to implement key serialization methods.
 */
public abstract class BE_DiskStorageBase {
    protected final Map contents;
    protected final int capacity;
    protected int stored = 0;
    protected BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;
    protected int priority = 0;

    public BE_DiskStorageBase(int capacity) {
        this.contents = new HashMap();
        this.capacity = capacity;
    }

    /**
     * Returns the NBT tag name for the content list ("items", "fluids", or "gases").
     */
    protected abstract String getContentTag();

    /**
     * Read a key from an NBT compound (type-specific deserialization).
     */
    protected abstract Object readKey(NBTTagCompound tag);

    /**
     * Write a key to an NBT compound (type-specific serialization).
     */
    protected abstract void writeKey(NBTTagCompound tag, Object key);

    /**
     * Maximum number of distinct types this storage can hold.
     * Override in item disk to return 63. Default is unlimited.
     */
    protected int getMaxTypes() {
        return Integer.MAX_VALUE;
    }

    /**
     * Returns the NBT key for the amount field in each entry.
     * Items use "count", fluids/gases use "amount".
     */
    protected String getAmountTag() {
        return "count";
    }

    public int insertImpl(Object key, int amount, boolean simulate) {
        if (!accessMode.allowsInsert() || amount <= 0) return 0;

        // Check type limit
        if (!contents.containsKey(key) && contents.size() >= getMaxTypes()) return 0;

        int space = capacity - stored;
        int toInsert = Math.min(amount, space);
        if (toInsert <= 0) return 0;

        if (!simulate) {
            Integer current = (Integer) contents.get(key);
            contents.put(key, Integer.valueOf((current != null ? current.intValue() : 0) + toInsert));
            stored += toInsert;
        }
        return toInsert;
    }

    public int extractImpl(Object key, int amount, boolean simulate) {
        if (!accessMode.allowsExtract() || amount <= 0) return 0;

        Integer current = (Integer) contents.get(key);
        if (current == null || current.intValue() <= 0) return 0;

        int toExtract = Math.min(amount, current.intValue());

        if (!simulate) {
            int remaining = current.intValue() - toExtract;
            if (remaining <= 0) {
                contents.remove(key);
            } else {
                contents.put(key, Integer.valueOf(remaining));
            }
            stored -= toExtract;
        }
        return toExtract;
    }

    public int getCountImpl(Object key) {
        Integer count = (Integer) contents.get(key);
        return count != null ? count.intValue() : 0;
    }

    public Map getAllImpl() {
        return Collections.unmodifiableMap(contents);
    }

    public int getStored() { return stored; }
    public int getCapacity() { return capacity; }
    public int getTypeCount() { return contents.size(); }

    public BE_AccessMode getAccessMode() { return accessMode; }
    public void setAccessMode(BE_AccessMode mode) { this.accessMode = mode; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public BE_StorageState getState() {
        if (stored <= 0) return BE_StorageState.EMPTY;
        if (stored >= capacity) return BE_StorageState.FULL;
        if ((float) stored / capacity > 0.75f) return BE_StorageState.NEAR_CAPACITY;
        return BE_StorageState.NORMAL;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("capacity", capacity);
        tag.setInteger("priority", priority);
        tag.setInteger("accessMode", accessMode.ordinal());

        NBTTagList list = new NBTTagList();
        java.util.Iterator it = contents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            NBTTagCompound entryTag = new NBTTagCompound();
            writeKey(entryTag, entry.getKey());
            entryTag.setInteger(getAmountTag(), ((Integer) entry.getValue()).intValue());
            list.setTag(entryTag);
        }
        tag.setTag(getContentTag(), list);
    }

    public void readFromNBT(NBTTagCompound tag) {
        priority = tag.getInteger("priority");
        int modeOrd = tag.getInteger("accessMode");
        if (modeOrd >= 0 && modeOrd < BE_AccessMode.values().length) {
            accessMode = BE_AccessMode.values()[modeOrd];
        }

        contents.clear();
        stored = 0;
        NBTTagList list = tag.getTagList(getContentTag());
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entryTag = (NBTTagCompound) list.tagAt(i);
                Object key = readKey(entryTag);
                int amount = entryTag.getInteger(getAmountTag());
                contents.put(key, Integer.valueOf(amount));
                stored += amount;
            }
        }
    }
}
