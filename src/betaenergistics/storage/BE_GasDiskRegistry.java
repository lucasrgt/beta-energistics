package betaenergistics.storage;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

import java.util.HashMap;
import java.util.Map;

public class BE_GasDiskRegistry {
    private static final Map<Integer, BE_GasDiskStorage> registry = new HashMap<Integer, BE_GasDiskStorage>();
    private static int nextId = 10;

    public static final int[] TIER_CAPACITIES = {8000, 32000, 128000, 512000};
    public static final String[] TIER_NAMES = {"8K", "32K", "128K", "512K"};

    public static int assignId(int tier) {
        int id = nextId++;
        int capacity = tier < TIER_CAPACITIES.length ? TIER_CAPACITIES[tier] : TIER_CAPACITIES[0];
        registry.put(id, new BE_GasDiskStorage(capacity));
        updateDiskName(id);
        return id;
    }

    public static BE_GasDiskStorage getStorage(int diskId) {
        return registry.get(diskId);
    }

    public static boolean isRegistered(int diskId) {
        return registry.containsKey(diskId);
    }

    public static int getTier(int diskId) {
        BE_GasDiskStorage s = registry.get(diskId);
        if (s == null) return 0;
        int cap = s.getCapacity();
        if (cap <= 8000) return 0;
        if (cap <= 32000) return 1;
        if (cap <= 128000) return 2;
        return 3;
    }

    public static void updateDiskName(int diskId) {
        BE_GasDiskStorage storage = registry.get(diskId);
        if (storage == null) return;
        int tier = getTier(diskId);
        String tierName = TIER_NAMES[tier];
        BE_RegistryUtil.updateDiskName(diskId, "beGasDisk", tierName, "Gas Disk",
            storage.getStored(), storage.getCapacity(),
            storage.getAllGases().size(), 4, "mB");
    }

    public static void updateAllDiskNames() {
        for (Map.Entry<Integer, BE_GasDiskStorage> entry : registry.entrySet()) {
            updateDiskName(entry.getKey());
        }
    }

    public static void save(World world) {
        BE_RegistryUtil.saveRegistry(world, "be_gas_data", registry.keySet(), nextId,
            new BE_RegistryUtil.DiskWriter() {
                public void writeDisk(NBTTagCompound diskTag, int diskId) {
                    BE_GasDiskStorage storage = registry.get(diskId);
                    if (storage != null) storage.writeToNBT(diskTag);
                }
            }, "gas disk registry");
    }

    public static void load(World world) {
        registry.clear();
        int loaded = BE_RegistryUtil.loadRegistry(world, "be_gas_data",
            new BE_RegistryUtil.DiskReader() {
                public void readDisk(NBTTagCompound diskTag, int diskId) {
                    int capacity = diskTag.getInteger("capacity");
                    BE_GasDiskStorage storage = new BE_GasDiskStorage(capacity);
                    storage.readFromNBT(diskTag);
                    registry.put(diskId, storage);
                }
            }, "gas disk registry");
        if (loaded >= 0) {
            nextId = loaded;
        }
        updateAllDiskNames();
    }
}
