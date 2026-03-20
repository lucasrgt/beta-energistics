package betaenergistics;

import betaenergistics.block.*;
import betaenergistics.container.*;
import betaenergistics.gui.*;
import betaenergistics.item.*;
import betaenergistics.tile.*;

import net.minecraft.src.*;

public class mod_BetaEnergistics extends BaseMod {
    // Block IDs (240-249 range)
    public static final int ID_CONTROLLER = 240;
    public static final int ID_CABLE = 241;
    public static final int ID_DISK_DRIVE = 242;
    public static final int ID_GRID = 243;
    public static final int ID_CRAFTING_TERMINAL = 244;
    public static final int ID_IMPORTER = 245;
    public static final int ID_EXPORTER = 246;
    public static final int ID_AUTOCRAFTER = 247;

    // Item IDs (700-719 range)
    public static final int ID_STORAGE_DISK = 700;
    public static final int ID_PATTERN = 701;

    // Block instances
    public static Block blockController;
    public static Block blockCable;
    public static Block blockDiskDrive;
    public static Block blockGrid;
    public static Block blockCraftingTerminal;
    public static Block blockImporter;
    public static Block blockExporter;
    public static Block blockAutocrafter;

    // Item instances
    public static Item itemStorageDisk;
    public static Item itemPattern;

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public void load() {
        // Register blocks
        blockController = new BE_BlockController(ID_CONTROLLER);
        blockCable = new BE_BlockCable(ID_CABLE);
        blockDiskDrive = new BE_BlockDiskDrive(ID_DISK_DRIVE);
        blockGrid = new BE_BlockGrid(ID_GRID);
        blockCraftingTerminal = new BE_BlockCraftingTerminal(ID_CRAFTING_TERMINAL);
        blockImporter = new BE_BlockImporter(ID_IMPORTER);
        blockExporter = new BE_BlockExporter(ID_EXPORTER);
        blockAutocrafter = new BE_BlockAutocrafter(ID_AUTOCRAFTER);

        ModLoader.registerBlock(blockController);
        ModLoader.registerBlock(blockCable);
        ModLoader.registerBlock(blockDiskDrive);
        ModLoader.registerBlock(blockGrid);
        ModLoader.registerBlock(blockCraftingTerminal);
        ModLoader.registerBlock(blockImporter);
        ModLoader.registerBlock(blockExporter);
        ModLoader.registerBlock(blockAutocrafter);

        // Register tile entities
        ModLoader.registerTileEntity(BE_TileController.class, "BE_Controller");
        ModLoader.registerTileEntity(BE_TileCable.class, "BE_Cable");
        ModLoader.registerTileEntity(BE_TileDiskDrive.class, "BE_DiskDrive");
        ModLoader.registerTileEntity(BE_TileGrid.class, "BE_Grid");
        ModLoader.registerTileEntity(BE_TileCraftingTerminal.class, "BE_CraftingTerminal");
        ModLoader.registerTileEntity(BE_TileImporter.class, "BE_Importer");
        ModLoader.registerTileEntity(BE_TileExporter.class, "BE_Exporter");
        ModLoader.registerTileEntity(BE_TileAutocrafter.class, "BE_Autocrafter");

        // Register items
        itemStorageDisk = new BE_ItemStorageDisk(ID_STORAGE_DISK);
        itemPattern = new BE_ItemPattern(ID_PATTERN);

        // Block names
        ModLoader.addName(blockController, "ME Controller");
        ModLoader.addName(blockCable, "ME Cable");
        ModLoader.addName(blockDiskDrive, "ME Disk Drive");
        ModLoader.addName(blockGrid, "ME Grid Terminal");
        ModLoader.addName(blockCraftingTerminal, "ME Crafting Terminal");
        ModLoader.addName(blockImporter, "ME Import Bus");
        ModLoader.addName(blockExporter, "ME Export Bus");
        ModLoader.addName(blockAutocrafter, "ME Autocrafter");

        // Item names
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 0), "1K Storage Disk");
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 1), "4K Storage Disk");
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 2), "16K Storage Disk");
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 3), "64K Storage Disk");
        ModLoader.addName(itemPattern, "Blank Pattern");

        // Register recipes
        BE_Recipes.registerAll();

        System.out.println("[Beta Energistics] Loaded v" + getVersion());
    }

    /**
     * Handle GUI opening for blocks.
     * Called from blockActivated in each block class.
     */
    public static void openGui(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileGrid) {
            ModLoader.openGUI(player, new BE_GuiGrid(player.inventory, (BE_TileGrid) te));
        } else if (te instanceof BE_TileCraftingTerminal) {
            // Crafting terminal uses the grid GUI for now (TODO: dedicated GUI with crafting grid)
            ModLoader.openGUI(player, new BE_GuiGrid(player.inventory, (BE_TileCraftingTerminal) te));
        } else if (te instanceof BE_TileDiskDrive) {
            ModLoader.openGUI(player, new BE_GuiDiskDrive(player.inventory, (BE_TileDiskDrive) te));
        }
        // TODO: Exporter GUI, Autocrafter GUI
    }
}
