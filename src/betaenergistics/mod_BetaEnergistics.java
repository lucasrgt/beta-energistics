package betaenergistics;

import betaenergistics.block.BE_BlockCable;
import betaenergistics.block.BE_BlockController;
import betaenergistics.block.BE_BlockDiskDrive;
import betaenergistics.block.BE_BlockGrid;
import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.tile.BE_TileCable;
import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileDiskDrive;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;

public class mod_BetaEnergistics extends BaseMod {
    // Block IDs (240-249 range)
    public static final int ID_CONTROLLER = 240;
    public static final int ID_CABLE = 241;
    public static final int ID_DISK_DRIVE = 242;
    public static final int ID_GRID = 243;

    // Item IDs (700-709 range)
    public static final int ID_STORAGE_DISK = 700;

    // Block instances
    public static Block blockController;
    public static Block blockCable;
    public static Block blockDiskDrive;
    public static Block blockGrid;

    // Item instances
    public static Item itemStorageDisk;

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

        ModLoader.registerBlock(blockController);
        ModLoader.registerBlock(blockCable);
        ModLoader.registerBlock(blockDiskDrive);
        ModLoader.registerBlock(blockGrid);

        // Register tile entities
        ModLoader.registerTileEntity(BE_TileController.class, "BE_Controller");
        ModLoader.registerTileEntity(BE_TileCable.class, "BE_Cable");
        ModLoader.registerTileEntity(BE_TileDiskDrive.class, "BE_DiskDrive");
        ModLoader.registerTileEntity(BE_TileGrid.class, "BE_Grid");

        // Register items
        itemStorageDisk = new BE_ItemStorageDisk(ID_STORAGE_DISK);

        // Block names
        ModLoader.addName(blockController, "ME Controller");
        ModLoader.addName(blockCable, "ME Cable");
        ModLoader.addName(blockDiskDrive, "ME Disk Drive");
        ModLoader.addName(blockGrid, "ME Grid Terminal");

        // Item names
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 0), "1K Storage Disk");
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 1), "4K Storage Disk");
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 2), "16K Storage Disk");
        ModLoader.addName(new ItemStack(itemStorageDisk, 1, 3), "64K Storage Disk");

        // TODO: crafting recipes
        // TODO: texture atlas registration
        // TODO: GUI handler registration
        // TODO: custom cable renderer

        System.out.println("[Beta Energistics] Loaded v" + getVersion());
    }
}
