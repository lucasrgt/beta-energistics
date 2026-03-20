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
        Item storageDisk = mod_BetaEnergistics.itemStorageDisk;
        Item pattern = mod_BetaEnergistics.itemPattern;

        // ME Controller: diamond + iron + redstone
        ModLoader.addRecipe(new ItemStack(controller), new Object[]{
            "IRI", "RDR", "IRI",
            'D', Item.diamond,
            'R', Item.redstone,
            'I', Item.ingotIron
        });

        // ME Cable (8): glass + redstone + iron
        ModLoader.addRecipe(new ItemStack(cable, 8), new Object[]{
            "GRG",
            'G', Block.glass,
            'R', Item.redstone
        });

        // ME Disk Drive: iron + cable + chest
        ModLoader.addRecipe(new ItemStack(diskDrive), new Object[]{
            "ICI", "I I", "ICI",
            'I', Item.ingotIron,
            'C', cable
        });

        // ME Grid Terminal: glass pane + cable + iron
        ModLoader.addRecipe(new ItemStack(grid), new Object[]{
            "GPG", "PCP", "III",
            'G', Block.thinGlass,
            'P', Item.glowstoneDust,
            'C', cable,
            'I', Item.ingotIron
        });

        // Crafting Terminal: grid + crafting table
        ModLoader.addRecipe(new ItemStack(craftingTerminal), new Object[]{
            "G", "W",
            'G', grid,
            'W', Block.workbench
        });

        // Import Bus: iron + cable + hopper (piston)
        ModLoader.addRecipe(new ItemStack(importer), new Object[]{
            " R ", "ICI",
            'R', Item.redstone,
            'I', Item.ingotIron,
            'C', cable
        });

        // Export Bus: iron + cable + redstone
        ModLoader.addRecipe(new ItemStack(exporter), new Object[]{
            "ICI", " R ",
            'R', Item.redstone,
            'I', Item.ingotIron,
            'C', cable
        });

        // Autocrafter: iron + cable + crafting table
        ModLoader.addRecipe(new ItemStack(autocrafter), new Object[]{
            "ICI", "CWC", "ICI",
            'I', Item.ingotIron,
            'C', cable,
            'W', Block.workbench
        });

        // Blank Pattern (2): glass + redstone + paper
        ModLoader.addRecipe(new ItemStack(pattern, 2), new Object[]{
            "GRG", "RPR", "GRG",
            'G', Block.glass,
            'R', Item.redstone,
            'P', Item.paper
        });

        // Storage Disk 1K: iron + redstone + glass
        ModLoader.addRecipe(new ItemStack(storageDisk, 1, 0), new Object[]{
            "RGR", "GIG", "RGR",
            'R', Item.redstone,
            'G', Block.glass,
            'I', Item.ingotIron
        });

        // Storage Disk 4K: 3x 1K + gold
        ModLoader.addRecipe(new ItemStack(storageDisk, 1, 1), new Object[]{
            "SIS", "IGI", "SIS",
            'S', new ItemStack(storageDisk, 1, 0),
            'I', Item.ingotIron,
            'G', Item.ingotGold
        });

        // Storage Disk 16K: 3x 4K + diamond
        ModLoader.addRecipe(new ItemStack(storageDisk, 1, 2), new Object[]{
            "SIS", "IDI", "SIS",
            'S', new ItemStack(storageDisk, 1, 1),
            'I', Item.ingotGold,
            'D', Item.diamond
        });

        // Storage Disk 64K: 3x 16K + diamond block
        ModLoader.addRecipe(new ItemStack(storageDisk, 1, 3), new Object[]{
            "SDS", "DED", "SDS",
            'S', new ItemStack(storageDisk, 1, 2),
            'D', Item.diamond,
            'E', Block.blockEmerald
        });
    }
}
