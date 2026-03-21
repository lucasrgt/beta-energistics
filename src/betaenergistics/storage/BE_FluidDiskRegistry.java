package betaenergistics.storage;

import betaenergistics.item.BE_ItemFluidDisk;

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
        String name = tierName + " Fluid Disk (" + storage.getStored() + "/" + storage.getCapacity()
            + " mB, " + storage.getTypeCount() + " types)";
        ModLoader.AddLocalization("beFluidDisk" + diskId + ".name", name);
    }

    public static void updateAllDiskNames() {
        for (Map.Entry<Integer, BE_FluidDiskStorage> entry : disks.entrySet()) {
            updateDiskName(entry.getKey());
        }
    }

    public static void save(World world) {
        if (!dirty) return;
        try {
            File file = world.saveHandler.func_28113_a("be_fluid_data");
            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("nextId", nextId);

            NBTTagList list = new NBTTagList();
            for (Map.Entry<Integer, BE_FluidDiskStorage> entry : disks.entrySet()) {
                NBTTagCompound diskTag = new NBTTagCompound();
                diskTag.setInteger("diskId", entry.getKey());
                Integer tier = diskTiers.get(entry.getKey());
                diskTag.setInteger("tier", tier != null ? tier : 0);
                entry.getValue().writeToNBT(diskTag);
                list.setTag(diskTag);
            }
            root.setTag("disks", list);

            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeGzippedCompoundToOutputStream(root, fos);
            fos.close();
            dirty = false;
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to save fluid disk registry: " + e.getMessage());
        }
    }

    public static void load(World world) {
        try {
            File file = world.saveHandler.func_28113_a("be_fluid_data");
            if (!file.exists()) return;

            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound root = CompressedStreamTools.func_1138_a(fis);
            fis.close();

            disks.clear();
            diskTiers.clear();
            nextId = root.getInteger("nextId");
            if (nextId < 10) nextId = 10;

            NBTTagList list = root.getTagList("disks");
            if (list != null) {
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound diskTag = (NBTTagCompound) list.tagAt(i);
                    int id = diskTag.getInteger("diskId");
                    int tier = diskTag.getInteger("tier");
                    int capacity = diskTag.getInteger("capacity");
                    BE_FluidDiskStorage storage = new BE_FluidDiskStorage(capacity);
                    storage.readFromNBT(diskTag);
                    disks.put(id, storage);
                    diskTiers.put(id, tier);
                }
            }
            dirty = false;
            updateAllDiskNames();
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to load fluid disk registry: " + e.getMessage());
        }
    }
}
