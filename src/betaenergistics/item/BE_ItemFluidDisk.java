package betaenergistics.item;

import betaenergistics.storage.BE_FluidDiskRegistry;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

/**
 * Fluid storage disk item. Uses damage value as disk identity:
 * - damage 0-3 = blank fluid disk of tier 8K/32K/128K/512K mB
 * - damage >= 10 = registered fluid disk with data in BE_FluidDiskRegistry
 */
public class BE_ItemFluidDisk extends Item {
    public static final int TIER_8K = 0;
    public static final int TIER_32K = 1;
    public static final int TIER_128K = 2;
    public static final int TIER_512K = 3;

    private static final int[] CAPACITIES = {8000, 32000, 128000, 512000};
    private static final String[] TIER_NAMES = {"8K", "32K", "128K", "512K"};

    private int[] tierIcons = new int[4];

    public BE_ItemFluidDisk(int itemId) {
        super(itemId);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setItemName("beFluidDisk");
    }

    public void setTierIcon(int tier, int iconIndex) {
        if (tier >= 0 && tier < tierIcons.length) {
            tierIcons[tier] = iconIndex;
        }
    }

    public static int getCapacity(int tier) {
        if (tier < 0 || tier >= CAPACITIES.length) return CAPACITIES[0];
        return CAPACITIES[tier];
    }

    public static String getTierName(int tier) {
        if (tier < 0 || tier >= TIER_NAMES.length) return TIER_NAMES[0];
        return TIER_NAMES[tier];
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        int dmg = stack.getItemDamage();
        if (BE_FluidDiskRegistry.isRegistered(dmg)) {
            BE_FluidDiskRegistry.updateDiskName(dmg);
            return "beFluidDisk" + dmg;
        }
        return "beFluidDisk" + dmg;
    }

    @Override
    public int getIconFromDamage(int damage) {
        int tier;
        if (damage >= 0 && damage < 4) {
            tier = damage;
        } else if (damage >= 10) {
            tier = BE_FluidDiskRegistry.getTier(damage);
            if (tier < 0) tier = 0;
        } else {
            tier = 0;
        }
        return tierIcons[tier];
    }
}
