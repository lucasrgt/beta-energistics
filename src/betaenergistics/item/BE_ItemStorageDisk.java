package betaenergistics.item;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

/**
 * Storage disk item — tier marker for Disk Drives.
 * 4 tiers: 1K, 4K, 16K, 64K (damage values 0-3).
 *
 * In Beta 1.7.3, ItemStack has no NBT tags. Disk data is stored
 * directly in the BE_TileDiskDrive's own NBT, per slot.
 * The item only indicates the tier; data is tied to the drive slot.
 */
public class BE_ItemStorageDisk extends Item {
    public static final int TIER_1K = 0;
    public static final int TIER_4K = 1;
    public static final int TIER_16K = 2;
    public static final int TIER_64K = 3;

    private static final int[] CAPACITIES = {1024, 4096, 16384, 65536};
    private static final String[] TIER_NAMES = {"1K", "4K", "16K", "64K"};

    public BE_ItemStorageDisk(int itemId) {
        super(itemId);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
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
        return "BE Storage Disk " + getTierName(stack.getItemDamage());
    }

    @Override
    public int getIconFromDamage(int damage) {
        // TODO: different icons per tier
        return 0;
    }
}
