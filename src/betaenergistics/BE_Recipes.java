package betaenergistics;

import net.minecraft.src.*;

/**
 * Crafting recipes for all Beta Energistics blocks and items.
 */
public class BE_Recipes {
    public static void registerAll() {
        Block controller = mod_BetaEnergistics.blockController;
        Block cable = mod_BetaEnergistics.blockCable;
        Item facade = mod_BetaEnergistics.itemFacade;
        Block diskDrive = mod_BetaEnergistics.blockDiskDrive;
        Block grid = mod_BetaEnergistics.blockGrid;
        Block craftingTerminal = mod_BetaEnergistics.blockCraftingTerminal;
        Block importer = mod_BetaEnergistics.blockImporter;
        Block exporter = mod_BetaEnergistics.blockExporter;
        Block autocrafter = mod_BetaEnergistics.blockAutocrafter;
        Block storageBus = mod_BetaEnergistics.blockStorageBus;
        Block energyAcceptor = mod_BetaEnergistics.blockEnergyAcceptor;
        Block recipeEncoder = mod_BetaEnergistics.blockRecipeEncoder;
        Block coprocessor = mod_BetaEnergistics.blockCoprocessor;
        Block requestTerminal = mod_BetaEnergistics.blockRequestTerminal;
        Block redstoneEmitter = mod_BetaEnergistics.blockRedstoneEmitter;
        Block advancedInterface = mod_BetaEnergistics.blockAdvancedInterface;
        Block fluidTerminal = mod_BetaEnergistics.blockFluidTerminal;
        Block fluidImporter = mod_BetaEnergistics.blockFluidImporter;
        Block fluidExporter = mod_BetaEnergistics.blockFluidExporter;
        Block fluidStorageBus = mod_BetaEnergistics.blockFluidStorageBus;
        Block fluidRedstoneEmitter = mod_BetaEnergistics.blockFluidRedstoneEmitter;
        Item storageDisk = mod_BetaEnergistics.itemStorageDisk;
        Item pattern = mod_BetaEnergistics.itemPattern;
        Item fluidDisk = mod_BetaEnergistics.itemFluidDisk;
        Item mobileTerminal = mod_BetaEnergistics.itemMobileTerminal;

        // ME Controller: diamond + iron + redstone
        ModLoader.AddRecipe(new ItemStack(controller), new Object[]{
            "IRI", "RDR", "IRI",
            'D', Item.diamond,
            'R', Item.redstone,
            'I', Item.ingotIron
        });

        // ME Cable (8): glass + redstone + iron
        ModLoader.AddRecipe(new ItemStack(cable, 8), new Object[]{
            "GRG",
            'G', Block.glass,
            'R', Item.redstone
        });

        // ME Disk Drive: iron + cable + chest
        ModLoader.AddRecipe(new ItemStack(diskDrive), new Object[]{
            "ICI", "I I", "ICI",
            'I', Item.ingotIron,
            'C', cable
        });

        // ME Grid Terminal: glass pane + cable + iron
        ModLoader.AddRecipe(new ItemStack(grid), new Object[]{
            "GPG", "PCP", "III",
            'G', Block.glass,
            'P', Item.lightStoneDust,
            'C', cable,
            'I', Item.ingotIron
        });

        // Crafting Terminal: grid + crafting table
        ModLoader.AddRecipe(new ItemStack(craftingTerminal), new Object[]{
            "G", "W",
            'G', grid,
            'W', Block.workbench
        });

        // Import Bus: iron + cable + hopper (piston)
        ModLoader.AddRecipe(new ItemStack(importer), new Object[]{
            " R ", "ICI",
            'R', Item.redstone,
            'I', Item.ingotIron,
            'C', cable
        });

        // Export Bus: iron + cable + redstone
        ModLoader.AddRecipe(new ItemStack(exporter), new Object[]{
            "ICI", " R ",
            'R', Item.redstone,
            'I', Item.ingotIron,
            'C', cable
        });

        // Autocrafter: iron + cable + crafting table
        ModLoader.AddRecipe(new ItemStack(autocrafter), new Object[]{
            "ICI", "CWC", "ICI",
            'I', Item.ingotIron,
            'C', cable,
            'W', Block.workbench
        });

        // ME Storage Bus: iron + cable + chest
        ModLoader.AddRecipe(new ItemStack(storageBus), new Object[]{
            "ICI", "CHC", "ICI",
            'I', Item.ingotIron,
            'C', cable,
            'H', Block.chest
        });

        // ME Energy Acceptor: iron + cable + redstone + gold
        ModLoader.AddRecipe(new ItemStack(energyAcceptor), new Object[]{
            "IRI", "CGC", "IRI",
            'I', Item.ingotIron,
            'R', Item.redstone,
            'C', cable,
            'G', Item.ingotGold
        });

        // ME Recipe Encoder: iron + cable + crafting table + diamond
        ModLoader.AddRecipe(new ItemStack(recipeEncoder), new Object[]{
            "ICI", "CWC", "IDI",
            'I', Item.ingotIron,
            'C', cable,
            'W', Block.workbench,
            'D', Item.diamond
        });

        // ME Crafting Coprocessor: iron + cable + diamond + gold
        ModLoader.AddRecipe(new ItemStack(coprocessor), new Object[]{
            "IGI", "CDC", "IGI",
            'I', Item.ingotIron,
            'G', Item.ingotGold,
            'C', cable,
            'D', Item.diamond
        });

        // ME Request Terminal: grid terminal + autocrafter + diamond
        ModLoader.AddRecipe(new ItemStack(requestTerminal), new Object[]{
            "D", "G", "A",
            'D', Item.diamond,
            'G', grid,
            'A', autocrafter
        });

        // ME Redstone Emitter: iron + cable + redstone + redstone torch
        ModLoader.AddRecipe(new ItemStack(redstoneEmitter), new Object[]{
            "IRI", "RCR", "ITI",
            'I', Item.ingotIron,
            'R', Item.redstone,
            'C', cable,
            'T', Block.torchRedstoneActive
        });

        // ME Advanced Interface: iron + cable + gold + diamond + piston
        ModLoader.AddRecipe(new ItemStack(advancedInterface), new Object[]{
            "IPI", "GCG", "IDI",
            'I', Item.ingotIron,
            'P', Block.pistonBase,
            'G', Item.ingotGold,
            'C', cable,
            'D', Item.diamond
        });

        // Blank Pattern (2): glass + redstone + paper
        ModLoader.AddRecipe(new ItemStack(pattern, 2), new Object[]{
            "GRG", "RPR", "GRG",
            'G', Block.glass,
            'R', Item.redstone,
            'P', Item.paper
        });

        // Storage Disk 1K: iron + redstone + glass
        ModLoader.AddRecipe(new ItemStack(storageDisk, 1, 0), new Object[]{
            "RGR", "GIG", "RGR",
            'R', Item.redstone,
            'G', Block.glass,
            'I', Item.ingotIron
        });

        // Storage Disk 4K: 3x 1K + gold
        ModLoader.AddRecipe(new ItemStack(storageDisk, 1, 1), new Object[]{
            "SIS", "IGI", "SIS",
            'S', new ItemStack(storageDisk, 1, 0),
            'I', Item.ingotIron,
            'G', Item.ingotGold
        });

        // Storage Disk 16K: 3x 4K + diamond
        ModLoader.AddRecipe(new ItemStack(storageDisk, 1, 2), new Object[]{
            "SIS", "IDI", "SIS",
            'S', new ItemStack(storageDisk, 1, 1),
            'I', Item.ingotGold,
            'D', Item.diamond
        });

        // Storage Disk 64K: 3x 16K + diamond block
        ModLoader.AddRecipe(new ItemStack(storageDisk, 1, 3), new Object[]{
            "SDS", "DED", "SDS",
            'S', new ItemStack(storageDisk, 1, 2),
            'D', Item.diamond,
            'E', Block.blockDiamond
        });

        // Storage Disk 256K: 3x 64K + gold block
        ModLoader.AddRecipe(new ItemStack(storageDisk, 1, 4), new Object[]{
            "SGS", "GDG", "SGS",
            'S', new ItemStack(storageDisk, 1, 3),
            'G', Block.blockGold,
            'D', Block.blockDiamond
        });

        // Storage Disk 1024K: 3x 256K + diamond block
        ModLoader.AddRecipe(new ItemStack(storageDisk, 1, 5), new Object[]{
            "SDS", "DED", "SDS",
            'S', new ItemStack(storageDisk, 1, 4),
            'D', Item.diamond,
            'E', Block.blockDiamond
        });

        // ME Mobile Terminal: grid terminal + gold + redstone + cable
        ModLoader.AddRecipe(new ItemStack(mobileTerminal), new Object[]{
            " G ", "RCR", " D ",
            'G', grid,
            'R', Item.redstone,
            'C', cable,
            'D', Item.diamond
        });

        // --- Fluid blocks ---

        // ME Fluid Terminal: glass + cable + iron + bucket
        ModLoader.AddRecipe(new ItemStack(fluidTerminal), new Object[]{
            "GPG", "BCB", "III",
            'G', Block.glass,
            'P', Item.lightStoneDust,
            'C', cable,
            'B', Item.bucketEmpty,
            'I', Item.ingotIron
        });

        // ME Fluid Import Bus: iron + cable + bucket
        ModLoader.AddRecipe(new ItemStack(fluidImporter), new Object[]{
            " B ", "ICI",
            'B', Item.bucketEmpty,
            'I', Item.ingotIron,
            'C', cable
        });

        // ME Fluid Export Bus: iron + cable + bucket
        ModLoader.AddRecipe(new ItemStack(fluidExporter), new Object[]{
            "ICI", " B ",
            'B', Item.bucketEmpty,
            'I', Item.ingotIron,
            'C', cable
        });

        // ME Fluid Storage Bus: iron + cable + bucket + chest
        ModLoader.AddRecipe(new ItemStack(fluidStorageBus), new Object[]{
            "IBI", "CHC", "IBI",
            'I', Item.ingotIron,
            'B', Item.bucketEmpty,
            'C', cable,
            'H', Block.chest
        });

        // ME Fluid Redstone Emitter: redstone emitter + bucket
        ModLoader.AddRecipe(new ItemStack(fluidRedstoneEmitter), new Object[]{
            "IBI", "RCR", "ITI",
            'I', Item.ingotIron,
            'B', Item.bucketEmpty,
            'R', Item.redstone,
            'C', cable,
            'T', Block.torchRedstoneActive
        });

        // --- Fluid Disks ---

        // Fluid Disk 8K: iron + redstone + glass + bucket
        ModLoader.AddRecipe(new ItemStack(fluidDisk, 1, 0), new Object[]{
            "RBR", "GIG", "RGR",
            'R', Item.redstone,
            'G', Block.glass,
            'I', Item.ingotIron,
            'B', Item.bucketEmpty
        });

        // Fluid Disk 32K: 3x 8K + gold
        ModLoader.AddRecipe(new ItemStack(fluidDisk, 1, 1), new Object[]{
            "SIS", "IGI", "SIS",
            'S', new ItemStack(fluidDisk, 1, 0),
            'I', Item.ingotIron,
            'G', Item.ingotGold
        });

        // Fluid Disk 128K: 3x 32K + diamond
        ModLoader.AddRecipe(new ItemStack(fluidDisk, 1, 2), new Object[]{
            "SIS", "IDI", "SIS",
            'S', new ItemStack(fluidDisk, 1, 1),
            'I', Item.ingotGold,
            'D', Item.diamond
        });

        // Fluid Disk 512K: 3x 128K + diamond block
        ModLoader.AddRecipe(new ItemStack(fluidDisk, 1, 3), new Object[]{
            "SDS", "DED", "SDS",
            'S', new ItemStack(fluidDisk, 1, 2),
            'D', Item.diamond,
            'E', Block.blockDiamond
        });

        // --- Facades: cable + block = 4 facades ---
        registerFacadeRecipes(cable, facade);
    }

    /**
     * Register facade recipes for common solid blocks.
     * Recipe: cable surrounded by block = 4 facades of that block.
     */
    private static void registerFacadeRecipes(Block cable, Item facade) {
        // Register facade for all solid blocks (exclude fluids, air, plants, technical blocks)
        for (int bid = 1; bid < Block.blocksList.length; bid++) {
            Block b = Block.blocksList[bid];
            if (b == null) continue;
            Material mat = b.blockMaterial;
            // Skip non-solid materials
            if (mat == Material.air || mat == Material.water || mat == Material.lava
                || mat == Material.plants || mat == Material.fire
                || mat == Material.portal || mat == Material.circuits
                || mat == Material.snow || mat == Material.cactus) continue;
            // Skip blocks without valid item form (beds, pistons extended, etc)
            if (bid == Block.bedrock.blockID) continue; // can't obtain
            ModLoader.AddShapelessRecipe(new ItemStack(facade, 4, bid), new Object[]{
                new ItemStack(cable), new ItemStack(b)
            });
        }
    }
}
