package betaenergistics.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

/**
 * Fluid storage implementation for a single fluid disk.
 * Stores fluids as Map<BE_FluidKey, Integer> (mB) with capacity limit.
 * Unlike item disks, fluid disks have no type limit (fluids are few types, large amounts).
 */
public class BE_FluidDiskStorage implements BE_IFluidStorage {
    private final Map<BE_FluidKey, Integer> fluids = new HashMap<BE_FluidKey, Integer>();
    private final int capacity; // in mB
    private int stored = 0;
    private BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;
    private int priority = 0;

    public BE_FluidDiskStorage(int capacity) {
        this.capacity = capacity;
    }

    public int insertFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        if (!accessMode.allowsInsert() || amountMB <= 0) return 0;

        int space = capacity - stored;
        int toInsert = Math.min(amountMB, space);
        if (toInsert <= 0) return 0;

        if (!simulate) {
            Integer current = fluids.get(key);
            fluids.put(key, (current != null ? current : 0) + toInsert);
            stored += toInsert;
        }
        return toInsert;
    }

    public int extractFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        if (!accessMode.allowsExtract() || amountMB <= 0) return 0;

        Integer current = fluids.get(key);
        if (current == null || current <= 0) return 0;

        int toExtract = Math.min(amountMB, current);

        if (!simulate) {
            int remaining = current - toExtract;
            if (remaining <= 0) {
                fluids.remove(key);
            } else {
                fluids.put(key, remaining);
            }
            stored -= toExtract;
        }
        return toExtract;
    }

    public int getFluidCount(BE_FluidKey key) {
        Integer count = fluids.get(key);
        return count != null ? count : 0;
    }

    public Map<BE_FluidKey, Integer> getAllFluids() {
        return Collections.unmodifiableMap(fluids);
    }

    public int getStored() { return stored; }
    public int getCapacity() { return capacity; }
    public int getTypeCount() { return fluids.size(); }

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
        for (Map.Entry<BE_FluidKey, Integer> entry : fluids.entrySet()) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setInteger("type", entry.getKey().fluidType);
            fluidTag.setInteger("amount", entry.getValue());
            list.setTag(fluidTag);
        }
        tag.setTag("fluids", list);
    }

    public void readFromNBT(NBTTagCompound tag) {
        priority = tag.getInteger("priority");
        int modeOrd = tag.getInteger("accessMode");
        if (modeOrd >= 0 && modeOrd < BE_AccessMode.values().length) {
            accessMode = BE_AccessMode.values()[modeOrd];
        }

        fluids.clear();
        stored = 0;
        NBTTagList list = tag.getTagList("fluids");
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound fluidTag = (NBTTagCompound) list.tagAt(i);
                BE_FluidKey key = new BE_FluidKey(fluidTag.getInteger("type"));
                int amount = fluidTag.getInteger("amount");
                fluids.put(key, amount);
                stored += amount;
            }
        }
    }
}
