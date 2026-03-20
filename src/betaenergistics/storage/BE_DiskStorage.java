package betaenergistics.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

/**
 * Core storage implementation for a single storage disk.
 * Stores items as a Map<ItemKey, Integer> with capacity and type limits.
 *
 * Inspired by RS2's StorageImpl + LimitedStorageImpl pattern.
 */
public class BE_DiskStorage {
    public static final int MAX_TYPES = 63;

    private final Map<BE_ItemKey, Integer> items = new HashMap<BE_ItemKey, Integer>();
    private final int capacity;
    private int stored = 0;
    private BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;
    private int priority = 0;

    public BE_DiskStorage(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Try to insert items. Returns how many were actually inserted.
     * If simulate is true, doesn't modify state.
     */
    public int insert(BE_ItemKey key, int amount, boolean simulate) {
        if (!accessMode.allowsInsert() || amount <= 0) return 0;

        // Check type limit
        if (!items.containsKey(key) && items.size() >= MAX_TYPES) return 0;

        int space = capacity - stored;
        int toInsert = Math.min(amount, space);
        if (toInsert <= 0) return 0;

        if (!simulate) {
            Integer current = items.get(key);
            items.put(key, (current != null ? current : 0) + toInsert);
            stored += toInsert;
        }
        return toInsert;
    }

    /**
     * Try to extract items. Returns how many were actually extracted.
     * If simulate is true, doesn't modify state.
     */
    public int extract(BE_ItemKey key, int amount, boolean simulate) {
        if (!accessMode.allowsExtract() || amount <= 0) return 0;

        Integer current = items.get(key);
        if (current == null || current <= 0) return 0;

        int toExtract = Math.min(amount, current);

        if (!simulate) {
            int remaining = current - toExtract;
            if (remaining <= 0) {
                items.remove(key);
            } else {
                items.put(key, remaining);
            }
            stored -= toExtract;
        }
        return toExtract;
    }

    public int getCount(BE_ItemKey key) {
        Integer count = items.get(key);
        return count != null ? count : 0;
    }

    public Map<BE_ItemKey, Integer> getAll() {
        return Collections.unmodifiableMap(items);
    }

    public int getStored() { return stored; }
    public int getCapacity() { return capacity; }
    public int getTypeCount() { return items.size(); }

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
        for (Map.Entry<BE_ItemKey, Integer> entry : items.entrySet()) {
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setInteger("id", entry.getKey().itemId);
            itemTag.setInteger("dmg", entry.getKey().damageValue);
            itemTag.setInteger("count", entry.getValue());
            list.setTag(itemTag);
        }
        tag.setTag("items", list);
    }

    public void readFromNBT(NBTTagCompound tag) {
        priority = tag.getInteger("priority");
        int modeOrd = tag.getInteger("accessMode");
        if (modeOrd >= 0 && modeOrd < BE_AccessMode.values().length) {
            accessMode = BE_AccessMode.values()[modeOrd];
        }

        items.clear();
        stored = 0;
        NBTTagList list = tag.getTagList("items");
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(i);
                BE_ItemKey key = new BE_ItemKey(itemTag.getInteger("id"), itemTag.getInteger("dmg"));
                int count = itemTag.getInteger("count");
                items.put(key, count);
                stored += count;
            }
        }
    }
}
