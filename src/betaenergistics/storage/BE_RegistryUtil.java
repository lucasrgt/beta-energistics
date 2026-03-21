package betaenergistics.storage;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * Shared utility methods for disk registries (item, fluid, gas).
 * Eliminates duplicated save/load/updateName logic across the 3 registry classes.
 */
public class BE_RegistryUtil {

    /**
     * Callback interface for writing type-specific disk data during save.
     */
    public interface DiskWriter {
        void writeDisk(NBTTagCompound diskTag, int diskId);
    }

    /**
     * Callback interface for reading type-specific disk data during load.
     */
    public interface DiskReader {
        void readDisk(NBTTagCompound diskTag, int diskId);
    }

    /**
     * Save a disk registry to a world file.
     *
     * @param world      the world (provides save handler)
     * @param fileName   the file name (e.g. "be_data", "be_fluid_data")
     * @param diskIds    iterable of disk IDs to save
     * @param nextId     the next available ID
     * @param writer     callback to write each disk's data
     * @param logPrefix  prefix for error messages
     */
    public static void saveRegistry(World world, String fileName, Iterable diskIds,
                                    int nextId, DiskWriter writer, String logPrefix) {
        try {
            File file = world.saveHandler.func_28113_a(fileName);
            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("nextId", nextId);

            NBTTagList list = new NBTTagList();
            java.util.Iterator it = diskIds.iterator();
            while (it.hasNext()) {
                Integer diskId = (Integer) it.next();
                NBTTagCompound diskTag = new NBTTagCompound();
                diskTag.setInteger("diskId", diskId.intValue());
                writer.writeDisk(diskTag, diskId.intValue());
                list.setTag(diskTag);
            }
            root.setTag("disks", list);

            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeGzippedCompoundToOutputStream(root, fos);
            fos.close();
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to save " + logPrefix + ": " + e.getMessage());
        }
    }

    /**
     * Load a disk registry from a world file.
     *
     * @param world      the world (provides save handler)
     * @param fileName   the file name (e.g. "be_data", "be_fluid_data")
     * @param reader     callback to read each disk's data
     * @param logPrefix  prefix for error messages
     * @return the loaded nextId, or -1 if file didn't exist
     */
    public static int loadRegistry(World world, String fileName, DiskReader reader, String logPrefix) {
        try {
            File file = world.saveHandler.func_28113_a(fileName);
            if (!file.exists()) return -1;

            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound root = CompressedStreamTools.func_1138_a(fis);
            fis.close();

            int nextId = root.getInteger("nextId");
            if (nextId < 10) nextId = 10;

            NBTTagList list = root.getTagList("disks");
            if (list != null) {
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound diskTag = (NBTTagCompound) list.tagAt(i);
                    int diskId = diskTag.getInteger("diskId");
                    reader.readDisk(diskTag, diskId);
                }
            }
            return nextId;
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to load " + logPrefix + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Update the localization name for a disk.
     *
     * @param diskId      the unique disk ID
     * @param locPrefix   the localization prefix (e.g. "beDisk", "beFluidDisk", "beGasDisk")
     * @param tierName    the tier display name (e.g. "16K", "32K")
     * @param diskLabel   the disk type label (e.g. "Disk", "Fluid Disk", "Gas Disk")
     * @param stored      current stored amount
     * @param capacity    total capacity
     * @param types       current type count
     * @param maxTypes    maximum type count
     * @param unit        unit label (e.g. "items", "mB")
     */
    public static void updateDiskName(int diskId, String locPrefix, String tierName, String diskLabel,
                                       int stored, int capacity, int types, int maxTypes, String unit) {
        String name = tierName + " " + diskLabel + " (" + stored + "/" + capacity
            + " " + unit + ", " + types + "/" + maxTypes + " types)";
        ModLoader.AddLocalization(locPrefix + diskId + ".name", name);
    }
}
