package betaenergistics.item;

import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_DiskStorage;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

/**
 * Storage disk item. Uses damage value as disk identity:
 * - damage 0-5 = blank disk of tier 1K/4K/16K/64K/256K/1024K
 * - damage >= 10 = registered disk with data in BE_DiskRegistry
 */
public class BE_ItemStorageDisk extends Item {
    public static final int TIER_1K = 0;
    public static final int TIER_4K = 1;
    public static final int TIER_16K = 2;
    public static final int TIER_64K = 3;
    public static final int TIER_256K = 4;
    public static final int TIER_1024K = 5;

    private static final int[] CAPACITIES = {1024, 4096, 16384, 65536, 262144, 1048576};
    private static final String[] TIER_NAMES = {"1K", "4K", "16K", "64K", "256K", "1024K"};

    public BE_ItemStorageDisk(int itemId) {
        super(itemId);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setItemName("beStorageDisk");
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
        if (BE_DiskRegistry.isRegistered(dmg)) {
            // Update localization dynamically so native tooltip shows current info
            BE_DiskRegistry.updateDiskName(dmg);
            return "beDisk" + dmg;
        }
        return "beStorageDisk" + dmg;
    }

    @Override
    public int getIconFromDamage(int damage) {
        // TODO: different icons per tier
        return 0;
    }
}
