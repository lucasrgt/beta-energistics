package betaenergistics.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

public class BE_GasDiskStorage implements BE_IGasStorage {
    private final Map<BE_GasKey, Integer> gases = new HashMap<BE_GasKey, Integer>();
    private final int capacity;
    private int stored = 0;
    private BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;
    private int priority = 0;

    public BE_GasDiskStorage(int capacity) {
        this.capacity = capacity;
    }

    public int insertGas(BE_GasKey key, int amountMB, boolean simulate) {
        if (!accessMode.allowsInsert() || amountMB <= 0) return 0;
        int space = capacity - stored;
        int toInsert = Math.min(amountMB, space);
        if (toInsert <= 0) return 0;
        if (!simulate) {
            Integer current = gases.get(key);
            gases.put(key, (current != null ? current : 0) + toInsert);
            stored += toInsert;
        }
        return toInsert;
    }

    public int extractGas(BE_GasKey key, int amountMB, boolean simulate) {
        if (!accessMode.allowsExtract() || amountMB <= 0) return 0;
        Integer current = gases.get(key);
        if (current == null || current <= 0) return 0;
        int toExtract = Math.min(amountMB, current);
        if (!simulate) {
            int remaining = current - toExtract;
            if (remaining <= 0) gases.remove(key);
            else gases.put(key, remaining);
            stored -= toExtract;
        }
        return toExtract;
    }

    public Map<BE_GasKey, Integer> getAllGases() {
        return Collections.unmodifiableMap(gases);
    }

    public int getGasAmount(BE_GasKey key) {
        Integer amt = gases.get(key);
        return amt != null ? amt : 0;
    }

    public int getCapacity() { return capacity; }
    public int getStored() { return stored; }
    public int getPriority() { return priority; }
    public void setPriority(int p) { this.priority = p; }
    public BE_AccessMode getAccessMode() { return accessMode; }
    public void setAccessMode(BE_AccessMode mode) { this.accessMode = mode; }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("capacity", capacity);
        tag.setInteger("priority", priority);
        tag.setInteger("accessMode", accessMode.ordinal());
        NBTTagList list = new NBTTagList();
        for (Map.Entry<BE_GasKey, Integer> e : gases.entrySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("type", e.getKey().gasType);
            entry.setInteger("amount", e.getValue());
            list.setTag(entry);
        }
        tag.setTag("gases", list);
        tag.setInteger("stored", stored);
    }

    public void readFromNBT(NBTTagCompound tag) {
        gases.clear();
        stored = 0;
        NBTTagList list = tag.getTagList("gases");
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = (NBTTagCompound) list.tagAt(i);
                int type = entry.getInteger("type");
                int amount = entry.getInteger("amount");
                gases.put(new BE_GasKey(type), amount);
                stored += amount;
            }
        }
    }
}
