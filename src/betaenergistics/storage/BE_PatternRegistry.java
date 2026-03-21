package betaenergistics.storage;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Global registry mapping pattern IDs to their recipe data.
 * Persisted as a separate file (be_patterns.dat) in the world save directory.
 *
 * Pattern items use their damage value as unique ID:
 * - damage 0 = blank pattern (not registered)
 * - damage >= 1 = encoded pattern with recipe data in this registry
 */
public class BE_PatternRegistry {
    private static final Map<Integer, PatternData> patterns = new HashMap<Integer, PatternData>();
    private static int nextId = 1;
    private static boolean dirty = false;

    /**
     * Register a new crafting pattern. Returns the unique pattern ID (to be set as item damage).
     */
    public static int createPattern(ItemStack[] inputs, ItemStack output) {
        return createPattern(inputs, output, TYPE_CRAFTING);
    }

    /**
     * Register a new pattern with explicit type. Returns the unique pattern ID.
     */
    public static int createPattern(ItemStack[] inputs, ItemStack output, int patternType) {
        int id = nextId++;
        patterns.put(id, new PatternData(inputs, output, patternType));
        dirty = true;
        updatePatternName(id);
        return id;
    }

    public static PatternData getPattern(int id) {
        return patterns.get(id);
    }

    public static boolean isRegistered(int id) {
        return id >= 1 && patterns.containsKey(id);
    }

    public static boolean isBlank(int damageValue) {
        return damageValue == 0;
    }

    public static void markDirty() {
        dirty = true;
    }

    /**
     * Update the localization for a pattern so the tooltip shows the output item name.
     */
    public static void updatePatternName(int patternId) {
        PatternData data = patterns.get(patternId);
        if (data == null || data.output == null) return;
        String outputName = StringTranslate.getInstance().translateNamedKey(data.output.getItem().getItemName());
        String prefix = data.isProcessing() ? "Processing: " : "Pattern: ";
        String name = prefix + outputName + " x" + data.output.stackSize;
        ModLoader.AddLocalization("bePattern" + patternId + ".name", name);
    }

    /**
     * Update localization for ALL registered patterns.
     */
    public static void updateAllPatternNames() {
        for (Map.Entry<Integer, PatternData> entry : patterns.entrySet()) {
            updatePatternName(entry.getKey());
        }
    }

    /**
     * Save registry to world save directory.
     */
    public static void save(World world) {
        if (!dirty) return;
        try {
            File file = world.saveHandler.func_28113_a("be_patterns");
            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("nextId", nextId);

            NBTTagList list = new NBTTagList();
            for (Map.Entry<Integer, PatternData> entry : patterns.entrySet()) {
                NBTTagCompound patTag = new NBTTagCompound();
                patTag.setInteger("patId", entry.getKey());
                entry.getValue().writeToNBT(patTag);
                list.setTag(patTag);
            }
            root.setTag("patterns", list);

            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeGzippedCompoundToOutputStream(root, fos);
            fos.close();
            dirty = false;
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to save pattern registry: " + e.getMessage());
        }
    }

    /**
     * Load registry from world save directory.
     */
    public static void load(World world) {
        try {
            File file = world.saveHandler.func_28113_a("be_patterns");
            if (!file.exists()) return;

            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound root = CompressedStreamTools.func_1138_a(fis);
            fis.close();

            patterns.clear();
            nextId = root.getInteger("nextId");
            if (nextId < 1) nextId = 1;

            NBTTagList list = root.getTagList("patterns");
            if (list != null) {
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound patTag = (NBTTagCompound) list.tagAt(i);
                    int id = patTag.getInteger("patId");
                    PatternData data = new PatternData();
                    data.readFromNBT(patTag);
                    patterns.put(id, data);
                }
            }
            dirty = false;
            updateAllPatternNames();
        } catch (Exception e) {
            System.err.println("[Beta Energistics] Failed to load pattern registry: " + e.getMessage());
        }
    }

    /** Pattern type constants. */
    public static final int TYPE_CRAFTING = 0;
    public static final int TYPE_PROCESSING = 1;

    /**
     * Holds recipe data for one encoded pattern.
     */
    public static class PatternData {
        public int[] inputIds = new int[9];
        public int[] inputDmg = new int[9];
        public int[] inputCount = new int[9];
        public ItemStack output;
        public int patternType = TYPE_CRAFTING;

        public PatternData() {}

        public PatternData(ItemStack[] inputs, ItemStack output) {
            this(inputs, output, TYPE_CRAFTING);
        }

        public PatternData(ItemStack[] inputs, ItemStack output, int patternType) {
            for (int i = 0; i < 9; i++) {
                if (i < inputs.length && inputs[i] != null) {
                    inputIds[i] = inputs[i].itemID;
                    inputDmg[i] = inputs[i].getItemDamage();
                    inputCount[i] = inputs[i].stackSize;
                }
            }
            this.output = output != null ? output.copy() : null;
            this.patternType = patternType;
        }

        public boolean isProcessing() {
            return patternType == TYPE_PROCESSING;
        }

        public ItemStack[] getInputs() {
            ItemStack[] result = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                if (inputIds[i] > 0) {
                    result[i] = new ItemStack(inputIds[i], inputCount[i], inputDmg[i]);
                }
            }
            return result;
        }

        public void writeToNBT(NBTTagCompound tag) {
            tag.setInteger("patType", patternType);
            for (int i = 0; i < 9; i++) {
                tag.setInteger("inId" + i, inputIds[i]);
                tag.setInteger("inDm" + i, inputDmg[i]);
                tag.setInteger("inCn" + i, inputCount[i]);
            }
            if (output != null) {
                tag.setInteger("outId", output.itemID);
                tag.setInteger("outDmg", output.getItemDamage());
                tag.setInteger("outCnt", output.stackSize);
            }
        }

        public void readFromNBT(NBTTagCompound tag) {
            patternType = tag.getInteger("patType");
            for (int i = 0; i < 9; i++) {
                inputIds[i] = tag.getInteger("inId" + i);
                inputDmg[i] = tag.getInteger("inDm" + i);
                inputCount[i] = tag.getInteger("inCn" + i);
            }
            int outId = tag.getInteger("outId");
            if (outId > 0) {
                output = new ItemStack(outId, tag.getInteger("outCnt"), tag.getInteger("outDmg"));
            }
        }
    }
}
