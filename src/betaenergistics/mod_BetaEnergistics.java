package betaenergistics;

import betaenergistics.block.*;
import betaenergistics.gui.*;
import betaenergistics.item.*;
import betaenergistics.render.BE_RenderCable;
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
    public static final int ID_STORAGE_BUS = 248;
    public static final int ID_ENERGY_ACCEPTOR = 249;
    public static final int ID_RECIPE_ENCODER = 250;
    public static final int ID_COPROCESSOR = 251;
    public static final int ID_REQUEST_TERMINAL = 252;
    public static final int ID_REDSTONE_EMITTER = 253;
    public static final int ID_ADVANCED_INTERFACE = 254;
    public static final int ID_FLUID_TERMINAL = 255;

    // Fluid block IDs (230-232 range)
    public static final int ID_FLUID_IMPORTER = 230;
    public static final int ID_FLUID_EXPORTER = 231;
    public static final int ID_FLUID_STORAGE_BUS = 232;

    // Item IDs (700-719 range)
    public static final int ID_STORAGE_DISK = 700;
    public static final int ID_PATTERN = 701;
    public static final int ID_FLUID_DISK = 702;
    public static final int ID_MOBILE_TERMINAL = 703;

    // Render IDs
    public static int cableRenderID;

    // Block instances
    public static Block blockController;
    public static Block blockCable;
    public static Block blockDiskDrive;
    public static Block blockGrid;
    public static Block blockCraftingTerminal;
    public static Block blockImporter;
    public static Block blockExporter;
    public static Block blockAutocrafter;
    public static Block blockStorageBus;
    public static Block blockEnergyAcceptor;
    public static Block blockRecipeEncoder;
    public static Block blockCoprocessor;
    public static Block blockRequestTerminal;
    public static Block blockRedstoneEmitter;
    public static Block blockAdvancedInterface;
    public static Block blockFluidTerminal;
    public static Block blockFluidImporter;
    public static Block blockFluidExporter;
    public static Block blockFluidStorageBus;

    // Item instances
    public static Item itemStorageDisk;
    public static Item itemPattern;
    public static Item itemFluidDisk;
    public static Item itemMobileTerminal;

    public mod_BetaEnergistics() {
        // Register render IDs
        cableRenderID = ModLoader.getUniqueBlockModelID(this, true);

        // Register blocks
        blockController = new BE_BlockController(ID_CONTROLLER);
        blockCable = new BE_BlockCable(ID_CABLE);
        blockDiskDrive = new BE_BlockDiskDrive(ID_DISK_DRIVE);
        blockGrid = new BE_BlockGrid(ID_GRID);
        blockCraftingTerminal = new BE_BlockCraftingTerminal(ID_CRAFTING_TERMINAL);
        blockImporter = new BE_BlockImporter(ID_IMPORTER);
        blockExporter = new BE_BlockExporter(ID_EXPORTER);
        blockAutocrafter = new BE_BlockAutocrafter(ID_AUTOCRAFTER);
        blockStorageBus = new BE_BlockStorageBus(ID_STORAGE_BUS);
        blockEnergyAcceptor = new BE_BlockEnergyAcceptor(ID_ENERGY_ACCEPTOR);
        blockRecipeEncoder = new BE_BlockRecipeEncoder(ID_RECIPE_ENCODER);
        blockCoprocessor = new BE_BlockCoprocessor(ID_COPROCESSOR);
        blockRequestTerminal = new BE_BlockRequestTerminal(ID_REQUEST_TERMINAL);
        blockRedstoneEmitter = new BE_BlockRedstoneEmitter(ID_REDSTONE_EMITTER);
        blockAdvancedInterface = new BE_BlockAdvancedInterface(ID_ADVANCED_INTERFACE);
        blockFluidTerminal = new BE_BlockFluidTerminal(ID_FLUID_TERMINAL);
        blockFluidImporter = new BE_BlockFluidImporter(ID_FLUID_IMPORTER);
        blockFluidExporter = new BE_BlockFluidExporter(ID_FLUID_EXPORTER);
        blockFluidStorageBus = new BE_BlockFluidStorageBus(ID_FLUID_STORAGE_BUS);

        ModLoader.RegisterBlock(blockController);
        ModLoader.RegisterBlock(blockCable);
        ModLoader.RegisterBlock(blockDiskDrive);
        ModLoader.RegisterBlock(blockGrid);
        ModLoader.RegisterBlock(blockCraftingTerminal);
        ModLoader.RegisterBlock(blockImporter);
        ModLoader.RegisterBlock(blockExporter);
        ModLoader.RegisterBlock(blockAutocrafter);
        ModLoader.RegisterBlock(blockStorageBus);
        ModLoader.RegisterBlock(blockEnergyAcceptor);
        ModLoader.RegisterBlock(blockRecipeEncoder);
        ModLoader.RegisterBlock(blockCoprocessor);
        ModLoader.RegisterBlock(blockRequestTerminal);
        ModLoader.RegisterBlock(blockRedstoneEmitter);
        ModLoader.RegisterBlock(blockAdvancedInterface);
        ModLoader.RegisterBlock(blockFluidTerminal);
        ModLoader.RegisterBlock(blockFluidImporter);
        ModLoader.RegisterBlock(blockFluidExporter);
        ModLoader.RegisterBlock(blockFluidStorageBus);

        // Register tile entities
        ModLoader.RegisterTileEntity(BE_TileController.class, "BE_Controller");
        ModLoader.RegisterTileEntity(BE_TileCable.class, "BE_Cable");
        ModLoader.RegisterTileEntity(BE_TileDiskDrive.class, "BE_DiskDrive");
        ModLoader.RegisterTileEntity(BE_TileGrid.class, "BE_Grid");
        ModLoader.RegisterTileEntity(BE_TileCraftingTerminal.class, "BE_CraftingTerminal");
        ModLoader.RegisterTileEntity(BE_TileImporter.class, "BE_Importer");
        ModLoader.RegisterTileEntity(BE_TileExporter.class, "BE_Exporter");
        ModLoader.RegisterTileEntity(BE_TileAutocrafter.class, "BE_Autocrafter");
        ModLoader.RegisterTileEntity(BE_TileStorageBus.class, "BE_StorageBus");
        ModLoader.RegisterTileEntity(BE_TileEnergyAcceptor.class, "BE_EnergyAcceptor");
        ModLoader.RegisterTileEntity(BE_TileRecipeEncoder.class, "BE_RecipeEncoder");
        ModLoader.RegisterTileEntity(BE_TileCoprocessor.class, "BE_Coprocessor");
        ModLoader.RegisterTileEntity(BE_TileRequestTerminal.class, "BE_RequestTerminal");
        ModLoader.RegisterTileEntity(BE_TileRedstoneEmitter.class, "BE_RedstoneEmitter");
        ModLoader.RegisterTileEntity(BE_TileAdvancedInterface.class, "BE_AdvancedInterface");
        ModLoader.RegisterTileEntity(BE_TileFluidTerminal.class, "BE_FluidTerminal");
        ModLoader.RegisterTileEntity(BE_TileFluidImporter.class, "BE_FluidImporter");
        ModLoader.RegisterTileEntity(BE_TileFluidExporter.class, "BE_FluidExporter");
        ModLoader.RegisterTileEntity(BE_TileFluidStorageBus.class, "BE_FluidStorageBus");

        // Register items
        itemStorageDisk = new BE_ItemStorageDisk(ID_STORAGE_DISK);
        itemPattern = new BE_ItemPattern(ID_PATTERN);
        itemFluidDisk = new BE_ItemFluidDisk(ID_FLUID_DISK);
        itemMobileTerminal = new BE_ItemMobileTerminal(ID_MOBILE_TERMINAL);

        // Block names
        ModLoader.AddName(blockController, "ME Controller");
        ModLoader.AddName(blockCable, "ME Cable");
        ModLoader.AddName(blockDiskDrive, "ME Disk Drive");
        ModLoader.AddName(blockGrid, "ME Grid Terminal");
        ModLoader.AddName(blockCraftingTerminal, "ME Crafting Terminal");
        ModLoader.AddName(blockImporter, "ME Import Bus");
        ModLoader.AddName(blockExporter, "ME Export Bus");
        ModLoader.AddName(blockAutocrafter, "ME Autocrafter");
        ModLoader.AddName(blockStorageBus, "ME Storage Bus");
        ModLoader.AddName(blockEnergyAcceptor, "ME Energy Acceptor");
        ModLoader.AddName(blockRecipeEncoder, "ME Recipe Encoder");
        ModLoader.AddName(blockCoprocessor, "ME Crafting Coprocessor");
        ModLoader.AddName(blockRequestTerminal, "ME Request Terminal");
        ModLoader.AddName(blockRedstoneEmitter, "ME Redstone Emitter");
        ModLoader.AddName(blockAdvancedInterface, "ME Advanced Interface");
        ModLoader.AddName(blockFluidTerminal, "ME Fluid Terminal");
        ModLoader.AddName(blockFluidImporter, "ME Fluid Import Bus");
        ModLoader.AddName(blockFluidExporter, "ME Fluid Export Bus");
        ModLoader.AddName(blockFluidStorageBus, "ME Fluid Storage Bus");

        // Item names — blank disks (damage 0-5)
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 0), "1K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 1), "4K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 2), "16K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 3), "64K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 4), "256K Storage Disk");
        ModLoader.AddName(new ItemStack(itemStorageDisk, 1, 5), "1024K Storage Disk");
        // Used disk names are updated dynamically by BE_DiskRegistry.updateDiskName()
        ModLoader.AddName(itemPattern, "Blank Pattern");

        // Fluid disk names — blank fluid disks (damage 0-3)
        ModLoader.AddName(new ItemStack(itemFluidDisk, 1, 0), "8K Fluid Disk");
        ModLoader.AddName(new ItemStack(itemFluidDisk, 1, 1), "32K Fluid Disk");
        ModLoader.AddName(new ItemStack(itemFluidDisk, 1, 2), "128K Fluid Disk");
        ModLoader.AddName(new ItemStack(itemFluidDisk, 1, 3), "512K Fluid Disk");

        // Mobile Terminal names
        ModLoader.AddName(itemMobileTerminal, "ME Mobile Terminal");
        ModLoader.AddLocalization("beMobileTerminalLinked.name", "ME Mobile Terminal (Linked)");

        // Block textures
        int texController = ModLoader.addOverride("/terrain.png", "/blocks/be_controller.png");
        int texCable = ModLoader.addOverride("/terrain.png", "/blocks/be_cable.png");
        int texDiskDrive = ModLoader.addOverride("/terrain.png", "/blocks/be_disk_drive.png");
        int texGrid = ModLoader.addOverride("/terrain.png", "/blocks/be_grid_terminal.png");
        int texCraftTerm = ModLoader.addOverride("/terrain.png", "/blocks/be_crafting_terminal.png");
        int texImporter = ModLoader.addOverride("/terrain.png", "/blocks/be_importer.png");
        int texExporter = ModLoader.addOverride("/terrain.png", "/blocks/be_exporter.png");
        int texAutocrafter = ModLoader.addOverride("/terrain.png", "/blocks/be_autocrafter.png");
        int texStorageBus = ModLoader.addOverride("/terrain.png", "/blocks/be_storage_bus.png");
        int texEnergyAcceptor = ModLoader.addOverride("/terrain.png", "/blocks/be_energy_acceptor.png");
        int texRecipeEncoder = ModLoader.addOverride("/terrain.png", "/blocks/be_recipe_encoder.png");
        int texCoprocessor = ModLoader.addOverride("/terrain.png", "/blocks/be_coprocessor.png");
        int texRequestTerminal = ModLoader.addOverride("/terrain.png", "/blocks/be_request_terminal.png");
        int texRedstoneEmitter = ModLoader.addOverride("/terrain.png", "/blocks/be_redstone_emitter.png");
        int texAdvancedInterface = ModLoader.addOverride("/terrain.png", "/blocks/be_advanced_interface.png");
        int texFluidTerminal = ModLoader.addOverride("/terrain.png", "/blocks/be_fluid_terminal.png");
        int texFluidImporter = ModLoader.addOverride("/terrain.png", "/blocks/be_fluid_importer.png");
        int texFluidExporter = ModLoader.addOverride("/terrain.png", "/blocks/be_fluid_exporter.png");
        int texFluidStorageBus = ModLoader.addOverride("/terrain.png", "/blocks/be_fluid_storage_bus.png");

        blockController.blockIndexInTexture = texController;
        blockCable.blockIndexInTexture = texCable;
        blockDiskDrive.blockIndexInTexture = texDiskDrive;
        blockGrid.blockIndexInTexture = texGrid;
        blockCraftingTerminal.blockIndexInTexture = texCraftTerm;
        blockImporter.blockIndexInTexture = texImporter;
        blockExporter.blockIndexInTexture = texExporter;
        blockAutocrafter.blockIndexInTexture = texAutocrafter;
        blockStorageBus.blockIndexInTexture = texStorageBus;
        blockEnergyAcceptor.blockIndexInTexture = texEnergyAcceptor;
        blockRecipeEncoder.blockIndexInTexture = texRecipeEncoder;
        blockCoprocessor.blockIndexInTexture = texCoprocessor;
        blockRequestTerminal.blockIndexInTexture = texRequestTerminal;
        blockRedstoneEmitter.blockIndexInTexture = texRedstoneEmitter;
        blockAdvancedInterface.blockIndexInTexture = texAdvancedInterface;
        blockFluidTerminal.blockIndexInTexture = texFluidTerminal;
        blockFluidImporter.blockIndexInTexture = texFluidImporter;
        blockFluidExporter.blockIndexInTexture = texFluidExporter;
        blockFluidStorageBus.blockIndexInTexture = texFluidStorageBus;

        // Item textures
        int texDisk = ModLoader.addOverride("/gui/items.png", "/item/be_storage_disk.png");
        int texPattern = ModLoader.addOverride("/gui/items.png", "/item/be_pattern.png");
        int texFluidDisk = ModLoader.addOverride("/gui/items.png", "/item/be_fluid_disk.png");
        int texMobileTerminal = ModLoader.addOverride("/gui/items.png", "/item/be_mobile_terminal.png");
        itemStorageDisk.setIconIndex(texDisk);
        itemPattern.setIconIndex(texPattern);
        itemFluidDisk.setIconIndex(texFluidDisk);
        itemMobileTerminal.setIconIndex(texMobileTerminal);

        // Register recipes
        BE_Recipes.registerAll();

        System.out.println("[Beta Energistics] Loaded v" + Version());
    }

    @Override
    public String Version() {
        return "0.1.0";
    }

    @Override
    public boolean RenderWorldBlock(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int modelID) {
        if (modelID == cableRenderID) {
            return BE_RenderCable.renderWorld(renderer, world, x, y, z, block);
        }
        return false;
    }

    @Override
    public void RenderInvBlock(RenderBlocks renderer, Block block, int metadata, int modelID) {
        if (modelID == cableRenderID) {
            BE_RenderCable.renderInventory(renderer, block, metadata);
        }
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
        } else if (te instanceof BE_TileRecipeEncoder) {
            ModLoader.OpenGUI(player, new BE_GuiRecipeEncoder(player.inventory, (BE_TileRecipeEncoder) te));
        } else if (te instanceof BE_TileAutocrafter) {
            ModLoader.OpenGUI(player, new BE_GuiAutocrafter(player.inventory, (BE_TileAutocrafter) te));
        } else if (te instanceof BE_TileRequestTerminal) {
            ModLoader.OpenGUI(player, new BE_GuiRequestTerminal(player.inventory, (BE_TileRequestTerminal) te));
        } else if (te instanceof BE_TileRedstoneEmitter) {
            ModLoader.OpenGUI(player, new BE_GuiRedstoneEmitter(player.inventory, (BE_TileRedstoneEmitter) te));
        } else if (te instanceof BE_TileFluidTerminal) {
            ModLoader.OpenGUI(player, new BE_GuiFluidTerminal(player.inventory, (BE_TileFluidTerminal) te));
        }
    }
}
