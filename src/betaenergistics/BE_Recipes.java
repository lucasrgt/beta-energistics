package betaenergistics;

import net.minecraft.src.*;

/**
 * Crafting recipes for all Beta Energistics blocks and items.
 */
public class BE_Recipes {
    public static void registerAll() {
        Block controller = mod_BetaEnergistics.blockController;
        Block cable = mod_BetaEnergistics.blockCable;
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
        Item storageDisk = mod_BetaEnergistics.itemStorageDisk;
        Item pattern = mod_BetaEnergistics.itemPattern;

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
            "IGI", "CDC", "IGI",
            'I', Item.ingotIron,
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
    }
}
