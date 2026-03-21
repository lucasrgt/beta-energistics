package betaenergistics.storage;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class BE_GasDiskRegistry {
    private static final Map<Integer, BE_GasDiskStorage> registry = new HashMap<Integer, BE_GasDiskStorage>();
    private static int nextId = 10;

    public static final int[] TIER_CAPACITIES = {8000, 32000, 128000, 512000};
    public static final String[] TIER_NAMES = {"8K Gas Disk", "32K Gas Disk", "128K Gas Disk", "512K Gas Disk"};

    public static int assignId(int tier) {
        int id = nextId++;
        int capacity = tier < TIER_CAPACITIES.length ? TIER_CAPACITIES[tier] : TIER_CAPACITIES[0];
        registry.put(id, new BE_GasDiskStorage(capacity));
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
        String name = tierName + " (" + storage.getStored() + "/" + storage.getCapacity()
            + " mB, " + storage.getAllGases().size() + "/4 types)";
        ModLoader.AddLocalization("beGasDisk" + diskId + ".name", name);
    }

    public static void updateAllDiskNames() {
        for (java.util.Map.Entry<Integer, BE_GasDiskStorage> entry : registry.entrySet()) {
            updateDiskName(entry.getKey());
        }
    }

    public static void save(World world) {
        try {
            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("nextId", nextId);
            NBTTagList list = new NBTTagList();
            for (Map.Entry<Integer, BE_GasDiskStorage> e : registry.entrySet()) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("diskId", e.getKey());
                e.getValue().writeToNBT(entry);
                list.setTag(entry);
            }
            root.setTag("disks", list);
            File file = world.saveHandler.func_28113_a("be_gas_data");
            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeGzippedCompoundToOutputStream(root, fos);
            fos.close();
        } catch (Exception e) {}
    }

    public static void load(World world) {
        try {
            File file = world.saveHandler.func_28113_a("be_gas_data");
            if (!file.exists()) return;
            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound root = CompressedStreamTools.func_1138_a(fis);
            fis.close();
            nextId = root.getInteger("nextId");
            if (nextId < 10) nextId = 10;
            registry.clear();
            NBTTagList list = root.getTagList("disks");
            if (list != null) {
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound entry = (NBTTagCompound) list.tagAt(i);
                    int diskId = entry.getInteger("diskId");
                    int capacity = entry.getInteger("capacity");
                    BE_GasDiskStorage storage = new BE_GasDiskStorage(capacity);
                    storage.readFromNBT(entry);
                    registry.put(diskId, storage);
                }
            }
        } catch (Exception e) {}
    }
}
