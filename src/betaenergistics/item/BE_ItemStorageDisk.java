package betaenergistics.item;

import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_DiskStorage;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class BE_ItemStorageDisk extends Item implements BE_IDisk {
    private static final int[] CAPACITIES = {1024, 4096, 16384, 65536, 262144, 1048576};
    private static final String[] TIER_NAMES = {"1K", "4K", "16K", "64K", "256K", "1024K"};
    private static final int MAX_TYPES = 63;

    private int[] tierIcons = new int[6];

    public BE_ItemStorageDisk(int itemId) {
        super(itemId);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setItemName("beStorageDisk");
    }

    public void setTierIcon(int tier, int iconIndex) {
        if (tier >= 0 && tier < tierIcons.length) {
            tierIcons[tier] = iconIndex;
        }
    }

    // BE_IDisk implementation

    public int getTierFromDamage(int damage) {
        if (damage >= 0 && damage < 6) return damage;
        if (damage >= 10) {
            int tier = BE_DiskRegistry.getTier(damage);
            return tier >= 0 ? tier : 0;
        }
        return 0;
    }

    public String getDiskName(ItemStack stack) {
        int tier = getTierFromDamage(stack.getItemDamage());
        return TIER_NAMES[tier] + " Storage Disk";
    }

    public String[] getTooltipLines(ItemStack stack) {
        int dmg = stack.getItemDamage();
        int tier = getTierFromDamage(dmg);
        String name = TIER_NAMES[tier] + " Storage Disk";

        if (BE_DiskRegistry.isRegistered(dmg)) {
            BE_DiskStorage storage = BE_DiskRegistry.getDisk(dmg);
            if (storage != null) {
                int stored = storage.getStored();
                int capacity = storage.getCapacity();
                int types = storage.getTypeCount();
                return new String[]{
                    name,
                    stored + " / " + capacity + " items",
                    types + " / " + MAX_TYPES + " types"
                };
            }
        }
        return new String[]{name, "Empty"};
    }

    public int getCapacityForTier(int tier) {
        return getCapacity(tier);
    }

    public boolean isBlankDisk(int damage) {
        return BE_DiskRegistry.isBlank(damage);
    }

    public int registerDisk(int tier) {
        return BE_DiskRegistry.createDisk(tier, getCapacity(tier));
    }

    public int getIconForTier(int tier) {
        return tierIcons[Math.min(tier, tierIcons.length - 1)];
    }

    // Static helpers

    public static String getTierName(int tier) {
        if (tier < 0 || tier >= TIER_NAMES.length) return TIER_NAMES[0];
        return TIER_NAMES[tier];
    }

    public static int getCapacity(int tier) {
        if (tier < 0 || tier >= CAPACITIES.length) return CAPACITIES[0];
        return CAPACITIES[tier];
    }

    // Item overrides

    @Override
    public String getItemNameIS(ItemStack stack) {
        int dmg = stack.getItemDamage();
        if (BE_DiskRegistry.isRegistered(dmg)) {
            BE_DiskRegistry.updateDiskName(dmg);
            return "beDisk" + dmg;
        }
        return "beStorageDisk" + dmg;
    }

    @Override
    public int getIconFromDamage(int damage) {
        return tierIcons[getTierFromDamage(damage)];
    }
}
