package betaenergistics.storage;

import betaenergistics.item.BE_ItemFluidDisk;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Global registry mapping fluid disk IDs to their FluidDiskStorage data.
 * Persisted as be_fluid_data.dat in the world save directory.
 *
 * Fluid disk items use their damage value as unique ID:
 * - damage 0-3 = blank fluid disks (tier 8K/32K/128K/512K mB)
 * - damage >= 10 = registered disk with data in this registry
 */
public class BE_FluidDiskRegistry {
    private static final Map<Integer, BE_FluidDiskStorage> disks = new HashMap<Integer, BE_FluidDiskStorage>();
    private static final Map<Integer, Integer> diskTiers = new HashMap<Integer, Integer>();
    private static int nextId = 10;
    private static boolean dirty = false;

    public static int createDisk(int tier, int capacity) {
        int id = nextId++;
        disks.put(id, new BE_FluidDiskStorage(capacity));
        diskTiers.put(id, tier);
        dirty = true;
        updateDiskName(id);
        return id;
    }

    public static BE_FluidDiskStorage getDisk(int id) {
        return disks.get(id);
    }

    public static int getTier(int id) {
        Integer tier = diskTiers.get(id);
        return tier != null ? tier : -1;
    }

    public static boolean isRegistered(int id) {
        return id >= 10 && disks.containsKey(id);
    }

    public static boolean isBlank(int damageValue) {
        return damageValue >= 0 && damageValue <= 3;
    }

    public static void markDirty() {
        dirty = true;
    }

    public static void updateDiskName(int diskId) {
        BE_FluidDiskStorage storage = disks.get(diskId);
        Integer tier = diskTiers.get(diskId);
        if (storage == null || tier == null) return;
        String tierName = BE_ItemFluidDisk.getTierName(tier);
        BE_RegistryUtil.updateDiskName(diskId, "beFluidDisk", tierName, "Fluid Disk",
            storage.getStored(), storage.getCapacity(),
            storage.getTypeCount(), 4, "mB");
    }

    public static void updateAllDiskNames() {
        for (Map.Entry<Integer, BE_FluidDiskStorage> entry : disks.entrySet()) {
            updateDiskName(entry.getKey());
        }
    }

    public static void save(World world) {
        if (!dirty) return;
        BE_RegistryUtil.saveRegistry(world, "be_fluid_data", disks.keySet(), nextId,
            new BE_RegistryUtil.DiskWriter() {
                public void writeDisk(NBTTagCompound diskTag, int diskId) {
                    Integer tier = diskTiers.get(diskId);
                    diskTag.setInteger("tier", tier != null ? tier : 0);
                    BE_FluidDiskStorage storage = disks.get(diskId);
                    if (storage != null) storage.writeToNBT(diskTag);
                }
            }, "fluid disk registry");
        dirty = false;
    }

    public static void load(World world) {
        disks.clear();
        diskTiers.clear();
        int loaded = BE_RegistryUtil.loadRegistry(world, "be_fluid_data",
            new BE_RegistryUtil.DiskReader() {
                public void readDisk(NBTTagCompound diskTag, int diskId) {
                    int tier = diskTag.getInteger("tier");
                    int capacity = diskTag.getInteger("capacity");
                    BE_FluidDiskStorage storage = new BE_FluidDiskStorage(capacity);
                    storage.readFromNBT(diskTag);
                    disks.put(diskId, storage);
                    diskTiers.put(diskId, tier);
                }
            }, "fluid disk registry");
        if (loaded >= 0) {
            nextId = loaded;
        }
        dirty = false;
        updateAllDiskNames();
    }
}
