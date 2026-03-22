package betaenergistics;

import betaenergistics.block.*;
import betaenergistics.gui.*;
import betaenergistics.item.*;
import betaenergistics.network.BE_PacketHandler;
import betaenergistics.render.BE_RenderCable;
import betaenergistics.tile.*;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;

public class mod_BetaEnergistics extends BaseModMp {
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

    // Fluid block IDs (230-233 range)
    public static final int ID_FLUID_IMPORTER = 230;
    public static final int ID_FLUID_EXPORTER = 231;
    public static final int ID_FLUID_STORAGE_BUS = 232;
    public static final int ID_FLUID_REDSTONE_EMITTER = 233;
    public static final int ID_GAS_TERMINAL = 234;
    public static final int ID_GAS_IMPORTER = 235;
    public static final int ID_GAS_EXPORTER = 236;
    public static final int ID_CRAFTING_MONITOR = 237;

    // Item IDs (700-719 range)
    public static final int ID_STORAGE_DISK = 700;
    public static final int ID_PATTERN = 701;
    public static final int ID_FLUID_DISK = 702;
    public static final int ID_MOBILE_TERMINAL = 703;
    public static final int ID_FACADE = 704;
    public static final int ID_GAS_DISK = 705;

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
    public static Block blockFluidRedstoneEmitter;
    public static Block blockGasTerminal;
    public static Block blockCraftingMonitor;
    public static Block blockGasImporter;
    public static Block blockGasExporter;

    // Item instances
    public static Item itemStorageDisk;
    public static Item itemPattern;
    public static Item itemFluidDisk;
    public static Item itemMobileTerminal;
    public static Item itemFacade;
    public static Item itemGasDisk;

    public mod_BetaEnergistics() {
        // Register tick handler for devtools (F9/F10) and game logic
        ModLoader.SetInGameHook(this, true, false);

        // Initialize shared devtools (no-op if -Daero.dev=true not set)
        Aero_DevBootstrap.init();
        Aero_DevTextureReload.registerMod("betaenergistics");

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
        blockFluidRedstoneEmitter = new BE_BlockFluidRedstoneEmitter(ID_FLUID_REDSTONE_EMITTER);
        blockGasTerminal = new BE_BlockGasTerminal(ID_GAS_TERMINAL);
        blockCraftingMonitor = new BE_BlockCraftingMonitor(ID_CRAFTING_MONITOR);
        // blockGasImporter = new BE_BlockGasImporter(ID_GAS_IMPORTER);  // TODO
        // blockGasExporter = new BE_BlockGasExporter(ID_GAS_EXPORTER);  // TODO

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
        ModLoader.RegisterBlock(blockFluidRedstoneEmitter);
        ModLoader.RegisterBlock(blockGasTerminal);
        ModLoader.RegisterBlock(blockCraftingMonitor);

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
        ModLoader.RegisterTileEntity(BE_TileGasTerminal.class, "BE_GasTerminal");
        ModLoader.RegisterTileEntity(BE_TileCraftingMonitor.class, "BE_CraftingMonitor");
        ModLoader.RegisterTileEntity(BE_TileFluidStorageBus.class, "BE_FluidStorageBus");
        ModLoader.RegisterTileEntity(BE_TileFluidRedstoneEmitter.class, "BE_FluidRedstoneEmitter");

        // Register items
        itemStorageDisk = new BE_ItemStorageDisk(ID_STORAGE_DISK);
        itemPattern = new BE_ItemPattern(ID_PATTERN);
        itemFluidDisk = new BE_ItemFluidDisk(ID_FLUID_DISK);
        itemGasDisk = new BE_ItemGasDisk(ID_GAS_DISK);
        itemMobileTerminal = new BE_ItemMobileTerminal(ID_MOBILE_TERMINAL);
        itemFacade = new BE_ItemFacade(ID_FACADE);

        // Block names
        ModLoader.AddName(blockController, "BE Controller");
        ModLoader.AddName(blockCable, "BE Cable");
        ModLoader.AddName(blockDiskDrive, "BE Disk Drive");
        ModLoader.AddName(blockGrid, "BE Grid Terminal");
        ModLoader.AddName(blockCraftingTerminal, "BE Crafting Terminal");
        ModLoader.AddName(blockImporter, "BE Import Bus");
        ModLoader.AddName(blockExporter, "BE Export Bus");
        ModLoader.AddName(blockAutocrafter, "BE Autocrafter");
        ModLoader.AddName(blockStorageBus, "BE Storage Bus");
        ModLoader.AddName(blockEnergyAcceptor, "BE Energy Acceptor");
        ModLoader.AddName(blockRecipeEncoder, "BE Recipe Encoder");
        ModLoader.AddName(blockCoprocessor, "BE Crafting Coprocessor");
        ModLoader.AddName(blockRequestTerminal, "BE Request Terminal");
        ModLoader.AddName(blockRedstoneEmitter, "BE Redstone Emitter");
        ModLoader.AddName(blockAdvancedInterface, "BE Advanced Interface");
        ModLoader.AddName(blockFluidTerminal, "BE Fluid Terminal");
        ModLoader.AddName(blockFluidImporter, "BE Fluid Import Bus");
        ModLoader.AddName(blockFluidExporter, "BE Fluid Export Bus");
        ModLoader.AddName(blockFluidStorageBus, "BE Fluid Storage Bus");
        ModLoader.AddName(blockFluidRedstoneEmitter, "BE Fluid Redstone Emitter");
        ModLoader.AddName(blockGasTerminal, "BE Gas Terminal");
        ModLoader.AddName(blockCraftingMonitor, "ME Crafting Monitor");
        ModLoader.AddName(new ItemStack(itemGasDisk, 1, 0), "8K Gas Disk");
        ModLoader.AddName(new ItemStack(itemGasDisk, 1, 1), "32K Gas Disk");
        ModLoader.AddName(new ItemStack(itemGasDisk, 1, 2), "128K Gas Disk");
        ModLoader.AddName(new ItemStack(itemGasDisk, 1, 3), "512K Gas Disk");

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
        ModLoader.AddName(itemMobileTerminal, "BE Mobile Terminal");
        ModLoader.AddLocalization("beMobileTerminalLinked.name", "BE Mobile Terminal (Linked)");

        // Facade names — register for ALL blocks that have facade recipes
        ModLoader.AddName(itemFacade, "BE Cable Facade");
        for (int bid = 1; bid < Block.blocksList.length; bid++) {
            Block b = Block.blocksList[bid];
            if (b == null) continue;
            if (bid == Block.bedrock.blockID) continue;
            boolean valid = b.isOpaqueCube()
                || bid == Block.glass.blockID
                || bid == Block.ice.blockID;
            if (!valid) continue;
            String blockName = StringTranslate.getInstance().translateNamedKey(b.getBlockName());
            if (blockName == null || blockName.isEmpty()) blockName = "Block " + bid;
            ModLoader.AddName(new ItemStack(itemFacade, 1, bid), blockName + " Facade");
        }

        // Block textures (tracked for F9 hot-reload)
        int texController = tex("/terrain.png", "/blocks/be_controller.png");
        int texCable = tex("/terrain.png", "/blocks/be_cable.png");
        int texDiskDrive = tex("/terrain.png", "/blocks/be_disk_drive.png");
        int texGrid = tex("/terrain.png", "/blocks/be_grid_terminal.png");
        int texCraftTerm = tex("/terrain.png", "/blocks/be_crafting_terminal.png");
        int texImporter = tex("/terrain.png", "/blocks/be_importer.png");
        int texExporter = tex("/terrain.png", "/blocks/be_exporter.png");
        int texAutocrafter = tex("/terrain.png", "/blocks/be_autocrafter.png");
        int texStorageBus = tex("/terrain.png", "/blocks/be_storage_bus.png");
        int texEnergyAcceptor = tex("/terrain.png", "/blocks/be_energy_acceptor.png");
        int texRecipeEncoder = tex("/terrain.png", "/blocks/be_recipe_encoder.png");
        int texCoprocessor = tex("/terrain.png", "/blocks/be_coprocessor.png");
        int texRequestTerminal = tex("/terrain.png", "/blocks/be_request_terminal.png");
        int texRedstoneEmitter = tex("/terrain.png", "/blocks/be_redstone_emitter.png");
        int texAdvancedInterface = tex("/terrain.png", "/blocks/be_advanced_interface.png");
        int texFluidTerminal = tex("/terrain.png", "/blocks/be_fluid_terminal.png");
        int texFluidImporter = tex("/terrain.png", "/blocks/be_fluid_importer.png");
        int texFluidExporter = tex("/terrain.png", "/blocks/be_fluid_exporter.png");
        int texFluidStorageBus = tex("/terrain.png", "/blocks/be_fluid_storage_bus.png");

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

        int texFluidRedstoneEmitter = tex("/terrain.png", "/blocks/be_fluid_redstone_emitter.png");
        blockFluidRedstoneEmitter.blockIndexInTexture = texFluidRedstoneEmitter;
        int texGasTerminal = tex("/terrain.png", "/blocks/be_gas_terminal.png");
        blockGasTerminal.blockIndexInTexture = texGasTerminal;
        int texCraftingMonitor = tex("/terrain.png", "/blocks/be_autocrafter.png");
        blockCraftingMonitor.blockIndexInTexture = texCraftingMonitor;

        // Gas disk textures
        int texGasDisk8k = tex("/gui/items.png", "/item/be_gas_disk_8k.png");
        int texGasDisk32k = tex("/gui/items.png", "/item/be_gas_disk_32k.png");
        int texGasDisk128k = tex("/gui/items.png", "/item/be_gas_disk_128k.png");
        int texGasDisk512k = tex("/gui/items.png", "/item/be_gas_disk_512k.png");
        itemGasDisk.setIconIndex(texGasDisk8k);
        ((BE_ItemGasDisk) itemGasDisk).setTierIcon(0, texGasDisk8k);
        ((BE_ItemGasDisk) itemGasDisk).setTierIcon(1, texGasDisk32k);
        ((BE_ItemGasDisk) itemGasDisk).setTierIcon(2, texGasDisk128k);
        ((BE_ItemGasDisk) itemGasDisk).setTierIcon(3, texGasDisk512k);

        // Item textures — storage disk per-tier icons
        int texDisk1k = tex("/gui/items.png", "/item/be_storage_disk_1k.png");
        int texDisk4k = tex("/gui/items.png", "/item/be_storage_disk_4k.png");
        int texDisk16k = tex("/gui/items.png", "/item/be_storage_disk_16k.png");
        int texDisk64k = tex("/gui/items.png", "/item/be_storage_disk_64k.png");
        int texDisk256k = tex("/gui/items.png", "/item/be_storage_disk_256k.png");
        int texDisk1024k = tex("/gui/items.png", "/item/be_storage_disk_1024k.png");
        itemStorageDisk.setIconIndex(texDisk1k);
        ((BE_ItemStorageDisk) itemStorageDisk).setTierIcon(0, texDisk1k);
        ((BE_ItemStorageDisk) itemStorageDisk).setTierIcon(1, texDisk4k);
        ((BE_ItemStorageDisk) itemStorageDisk).setTierIcon(2, texDisk16k);
        ((BE_ItemStorageDisk) itemStorageDisk).setTierIcon(3, texDisk64k);
        ((BE_ItemStorageDisk) itemStorageDisk).setTierIcon(4, texDisk256k);
        ((BE_ItemStorageDisk) itemStorageDisk).setTierIcon(5, texDisk1024k);

        // Fluid disk per-tier icons
        int texFluidDisk8k = tex("/gui/items.png", "/item/be_fluid_disk_8k.png");
        int texFluidDisk32k = tex("/gui/items.png", "/item/be_fluid_disk_32k.png");
        int texFluidDisk128k = tex("/gui/items.png", "/item/be_fluid_disk_128k.png");
        int texFluidDisk512k = tex("/gui/items.png", "/item/be_fluid_disk_512k.png");
        itemFluidDisk.setIconIndex(texFluidDisk8k);
        ((BE_ItemFluidDisk) itemFluidDisk).setTierIcon(0, texFluidDisk8k);
        ((BE_ItemFluidDisk) itemFluidDisk).setTierIcon(1, texFluidDisk32k);
        ((BE_ItemFluidDisk) itemFluidDisk).setTierIcon(2, texFluidDisk128k);
        ((BE_ItemFluidDisk) itemFluidDisk).setTierIcon(3, texFluidDisk512k);

        // Pattern, mobile terminal, and facade
        int texPattern = tex("/gui/items.png", "/item/be_pattern.png");
        int texMobileTerminal = tex("/gui/items.png", "/item/be_mobile_terminal.png");
        int texFacade = tex("/gui/items.png", "/item/be_facade.png");
        itemPattern.setIconIndex(texPattern);
        itemMobileTerminal.setIconIndex(texMobileTerminal);
        itemFacade.setIconIndex(texFacade);

        // Register recipes
        BE_Recipes.registerAll();

        System.out.println("[Beta Energistics] Loaded v" + Version());
    }

    @Override
    public boolean OnTickInGame(Minecraft mc) {
        if (Aero_DevBootstrap.IS_DEV) {
            Aero_DevConsole.onTick(mc);
        }
        return true;
    }

    /** Register texture override + track for devtools F9 hot-reload */
    private static int tex(String atlas, String path) {
        return Aero_DevTextureReload.override("betaenergistics", atlas, path);
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
     * Handle GUI opening for blocks (singleplayer — direct tile reference).
     */
    public static void openGui(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileCraftingTerminal) {
            ModLoader.OpenGUI(player, new BE_GuiCraftingTerminal(player.inventory, (BE_TileCraftingTerminal) te));
        } else if (te instanceof BE_TileGrid) {
            ModLoader.OpenGUI(player, new BE_GuiGrid(player.inventory, (BE_TileGrid) te));
        } else if (te instanceof BE_TileDiskDrive) {
            ModLoader.OpenGUI(player, new BE_GuiDiskDrive(player.inventory, (BE_TileDiskDrive) te));
        } else if (te instanceof BE_TileRecipeEncoder) {
            ModLoader.OpenGUI(player, new BE_GuiRecipeEncoder(player.inventory, (BE_TileRecipeEncoder) te));
        } else if (te instanceof BE_TileAutocrafter) {
            ModLoader.OpenGUI(player, new BE_GuiAutocrafter(player.inventory, (BE_TileAutocrafter) te));
        } else if (te instanceof BE_TileRequestTerminal) {
            ModLoader.OpenGUI(player, new BE_GuiRequestTerminal(player.inventory, (BE_TileRequestTerminal) te));
        } else if (te instanceof BE_TileCraftingMonitor) {
            ModLoader.OpenGUI(player, new BE_GuiCraftingMonitor(player.inventory, (BE_TileCraftingMonitor) te));
        } else if (te instanceof BE_TileRedstoneEmitter) {
            ModLoader.OpenGUI(player, new BE_GuiRedstoneEmitter(player.inventory, (BE_TileRedstoneEmitter) te));
        } else if (te instanceof BE_TileFluidRedstoneEmitter) {
            ModLoader.OpenGUI(player, new BE_GuiFluidRedstoneEmitter(player.inventory, (BE_TileFluidRedstoneEmitter) te));
        } else if (te instanceof BE_TileFluidTerminal) {
            ModLoader.OpenGUI(player, new BE_GuiFluidTerminal(player.inventory, (BE_TileFluidTerminal) te));
        }
    }

    // ====== Multiplayer packet handling ======

    /**
     * Handle packets received from the server (client-side).
     * Dispatches to BE_PacketHandler for processing.
     */
    @Override
    public void HandlePacket(Packet230ModLoader packet) {
        BE_PacketHandler.handleClientPacket(packet);
    }

    /**
     * Check if the world is multiplayer (client connected to remote server).
     * In singleplayer, world.multiplayerWorld is false.
     */
    public static boolean isMultiplayer() {
        Minecraft mc = ModLoader.getMinecraftInstance();
        return mc != null && mc.theWorld != null && mc.theWorld.multiplayerWorld;
    }
}
