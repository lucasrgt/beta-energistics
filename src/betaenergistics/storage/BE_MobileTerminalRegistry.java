package betaenergistics.storage;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry that maps mobile terminal IDs (item damage values) to linked Grid Terminal coordinates.
 * Persisted to be_mobile_data.dat via Controller.
 *
 * damage 0 = unlinked terminal
 * damage >= 1 = linked terminal with coordinates stored here
 */
public class BE_MobileTerminalRegistry {
    private static Map<Integer, int[]> links = new HashMap<Integer, int[]>();
    private static int nextId = 1;
    private static boolean dirty = false;

    /** Link a mobile terminal to a Grid Terminal at the given coordinates. Returns the assigned damage value. */
    public static int linkTerminal(int x, int y, int z) {
        int id = nextId++;
        links.put(id, new int[]{x, y, z});
        dirty = true;
        return id;
    }

    /** Get the linked coordinates for a terminal ID. Returns null if not linked. */
    public static int[] getLinkedCoords(int id) {
        return links.get(id);
    }

    /** Check if a terminal ID is linked. */
    public static boolean isLinked(int id) {
        return id > 0 && links.containsKey(id);
    }

    /** Save registry to world save directory. */
    public static void save(World world) {
        if (!dirty) return;
        try {
            File file = world.saveHandler.func_28113_a("be_mobile_data");
            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("nextId", nextId);

            NBTTagList list = new NBTTagList();
            for (Map.Entry<Integer, int[]> entry : links.entrySet()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("id", entry.getKey());
                int[] coords = entry.getValue();
                tag.setInteger("x", coords[0]);
                tag.setInteger("y", coords[1]);
                tag.setInteger("z", coords[2]);
                list.setTag(tag);
            }
            root.setTag("links", list);

            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeGzippedCompoundToOutputStream(root, fos);
            fos.close();
            dirty = false;
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to save mobile terminal registry: " + e.getMessage());
        }
    }

    /** Load registry from world save directory. */
    public static void load(World world) {
        try {
            File file = world.saveHandler.func_28113_a("be_mobile_data");
            if (!file.exists()) return;

            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound root = CompressedStreamTools.func_1138_a(fis);
            fis.close();

            links.clear();
            nextId = root.getInteger("nextId");
            if (nextId < 1) nextId = 1;

            NBTTagList list = root.getTagList("links");
            if (list != null) {
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = (NBTTagCompound) list.tagAt(i);
                    int id = tag.getInteger("id");
                    int x = tag.getInteger("x");
                    int y = tag.getInteger("y");
                    int z = tag.getInteger("z");
                    links.put(id, new int[]{x, y, z});
                }
            }
            dirty = false;
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to load mobile terminal registry: " + e.getMessage());
        }
    }
}
