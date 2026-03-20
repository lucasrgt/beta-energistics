package betaenergistics.storage;

import betaenergistics.item.BE_ItemStorageDisk;

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
 * Global registry mapping disk IDs to their DiskStorage data.
 * Persisted as a separate file (be_data.dat) in the world save directory.
 *
 * Disk items use their damage value as unique ID:
 * - damage 0-3 = blank disks (tier 1K/4K/16K/64K, not yet registered)
 * - damage >= 10 = registered disk with data in this registry
 *
 * This survives breaking controllers, drives, or any tile entity.
 * Data lives with the world save, not with any block.
 */
public class BE_DiskRegistry {
    private static final Map<Integer, BE_DiskStorage> disks = new HashMap<Integer, BE_DiskStorage>();
    private static final Map<Integer, Integer> diskTiers = new HashMap<Integer, Integer>();
    private static int nextId = 10;
    private static boolean dirty = false;

    /**
     * Register a new disk. Returns the unique disk ID (to be set as item damage).
     */
    public static int createDisk(int tier, int capacity) {
        int id = nextId++;
        disks.put(id, new BE_DiskStorage(capacity));
        diskTiers.put(id, tier);
        dirty = true;
        updateDiskName(id);
        return id;
    }

    public static BE_DiskStorage getDisk(int id) {
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

    /**
     * Update the localization for a disk so the tooltip shows current storage info.
     * Called after insert/extract operations.
     */
    public static void updateDiskName(int diskId) {
        BE_DiskStorage storage = disks.get(diskId);
        Integer tier = diskTiers.get(diskId);
        if (storage == null || tier == null) return;
        String tierName = BE_ItemStorageDisk.getTierName(tier);
        String name = tierName + " Disk (" + storage.getStored() + "/" + storage.getCapacity()
            + " items, " + storage.getTypeCount() + "/" + BE_DiskStorage.MAX_TYPES + " types)";
        ModLoader.AddLocalization("beDisk" + diskId + ".name", name);
    }

    /**
     * Update localization for ALL registered disks.
     * Called on load and periodically.
     */
    public static void updateAllDiskNames() {
        for (Map.Entry<Integer, BE_DiskStorage> entry : disks.entrySet()) {
            updateDiskName(entry.getKey());
        }
    }

    /**
     * Save registry to world save directory.
     * Called from Controller tick (periodically) and world save.
     */
    public static void save(World world) {
        if (!dirty) return;
        try {
            File file = world.saveHandler.func_28113_a("be_data");
            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("nextId", nextId);

            NBTTagList list = new NBTTagList();
            for (Map.Entry<Integer, BE_DiskStorage> entry : disks.entrySet()) {
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
            System.err.println("[Beta Energistics] Failed to save disk registry: " + e.getMessage());
        }
    }

    /**
     * Load registry from world save directory.
     * Called when the Controller first loads.
     */
    public static void load(World world) {
        try {
            File file = world.saveHandler.func_28113_a("be_data");
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
                    BE_DiskStorage storage = new BE_DiskStorage(capacity);
                    storage.readFromNBT(diskTag);
                    disks.put(id, storage);
                    diskTiers.put(id, tier);
                }
            }
            dirty = false;
            updateAllDiskNames();
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to load disk registry: " + e.getMessage());
        }
    }
}
