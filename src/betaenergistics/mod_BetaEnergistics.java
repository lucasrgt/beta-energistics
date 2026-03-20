package betaenergistics;

import betaenergistics.block.*;
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

    public mod_BetaEnergistics() {
        // Register blocks
        blockController = new BE_BlockController(ID_CONTROLLER);
        blockCable = new BE_BlockCable(ID_CABLE);
        blockDiskDrive = new BE_BlockDiskDrive(ID_DISK_DRIVE);
        blockGrid = new BE_BlockGrid(ID_GRID);
        blockCraftingTerminal = new BE_BlockCraftingTerminal(ID_CRAFTING_TERMINAL);
        blockImporter = new BE_BlockImporter(ID_IMPORTER);
        blockExporter = new BE_BlockExporter(ID_EXPORTER);
        blockAutocrafter = new BE_BlockAutocrafter(ID_AUTOCRAFTER);

        ModLoader.RegisterBlock(blockController);
        ModLoader.RegisterBlock(blockCable);
        ModLoader.RegisterBlock(blockDiskDrive);
        ModLoader.RegisterBlock(blockGrid);
        ModLoader.RegisterBlock(blockCraftingTerminal);
        ModLoader.RegisterBlock(blockImporter);
        ModLoader.RegisterBlock(blockExporter);
        ModLoader.RegisterBlock(blockAutocrafter);

        // Register tile entities
        ModLoader.RegisterTileEntity(BE_TileController.class, "BE_Controller");
        ModLoader.RegisterTileEntity(BE_TileCable.class, "BE_Cable");
        ModLoader.RegisterTileEntity(BE_TileDiskDrive.class, "BE_DiskDrive");
        ModLoader.RegisterTileEntity(BE_TileGrid.class, "BE_Grid");
        ModLoader.RegisterTileEntity(BE_TileCraftingTerminal.class, "BE_CraftingTerminal");
        ModLoader.RegisterTileEntity(BE_TileImporter.class, "BE_Importer");
        ModLoader.RegisterTileEntity(BE_TileExporter.class, "BE_Exporter");
        ModLoader.RegisterTileEntity(BE_TileAutocrafter.class, "BE_Autocrafter");

        // Register items
        itemStorageDisk = new BE_ItemStorageDisk(ID_STORAGE_DISK);
        itemPattern = new BE_ItemPattern(ID_PATTERN);

        // Block names
        ModLoader.AddName(blockController, "ME Controller");
        ModLoader.AddName(blockCable, "ME Cable");
        ModLoader.AddName(blockDiskDrive, "ME Disk Drive");
        ModLoader.AddName(blockGrid, "ME Grid Terminal");
        ModLoader.AddName(blockCraftingTerminal, "ME Crafting Terminal");
        ModLoader.AddName(blockImporter, "ME Import Bus");
        ModLoader.AddName(blockExporter, "ME Export Bus");
        ModLoader.AddName(blockAutocrafter, "ME Autocrafter");

        // Item names
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 0), "1K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 1), "4K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 2), "16K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 3), "64K Storage Disk");
        ModLoader.AddName(itemPattern, "Blank Pattern");

        // Register recipes
        BE_Recipes.registerAll();

        System.out.println("[Beta Energistics] Loaded v" + Version());
    }

    @Override
    public String Version() {
        return "0.1.0";
    }

    /**
     * Handle GUI opening for blocks.
     */
    public static void openGui(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileGrid) {
            ModLoader.OpenGUI(player, new BE_GuiGrid(player.inventory, (BE_TileGrid) te));
        } else if (te instanceof BE_TileDiskDrive) {
            ModLoader.OpenGUI(player, new BE_GuiDiskDrive(player.inventory, (BE_TileDiskDrive) te));
        }
    }
}
