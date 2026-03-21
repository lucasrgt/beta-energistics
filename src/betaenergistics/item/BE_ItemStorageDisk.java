package betaenergistics.item;

import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_DiskStorage;

import net.minecraft.src.ItemStack;

public class BE_ItemStorageDisk extends BE_ItemDiskBase {
    private static final int[] CAPACITIES = {1024, 4096, 16384, 65536, 262144, 1048576};
    private static final String[] TIER_NAMES = {"1K", "4K", "16K", "64K", "256K", "1024K"};
    private static final int MAX_TYPES = 63;

    public BE_ItemStorageDisk(int itemId) {
        super(itemId, "beStorageDisk", 6, TIER_NAMES, CAPACITIES, "Storage Disk", "items", MAX_TYPES);
    }

    public int getTierFromDamage(int damage) {
        if (damage >= 0 && damage < 6) return damage;
        if (damage >= 10) {
            int tier = BE_DiskRegistry.getTier(damage);
            return tier >= 0 ? tier : 0;
        }
        return 0;
    }

    public boolean isBlankDisk(int damage) {
        return BE_DiskRegistry.isBlank(damage);
    }

    public int registerDisk(int tier) {
        return BE_DiskRegistry.createDisk(tier, getCapacityForTier(tier));
    }

    @Override
    protected boolean isRegisteredDisk(int damage) {
        return BE_DiskRegistry.isRegistered(damage);
    }

    @Override
    protected int getStoredAmount(int damage) {
        BE_DiskStorage s = BE_DiskRegistry.getDisk(damage);
        return s != null ? s.getStored() : -1;
    }

    @Override
    protected int getStoredCapacity(int damage) {
        BE_DiskStorage s = BE_DiskRegistry.getDisk(damage);
        return s != null ? s.getCapacity() : -1;
    }

    @Override
    protected int getStoredTypes(int damage) {
        BE_DiskStorage s = BE_DiskRegistry.getDisk(damage);
        return s != null ? s.getTypeCount() : -1;
    }

    @Override
    protected void updateRegistryDiskName(int damage) {
        BE_DiskRegistry.updateDiskName(damage);
    }

    @Override
    protected String getRegistryLocPrefix() {
        return "beDisk";
    }

    // Static helpers (used by BE_DiskRegistry)

    public static String getTierName(int tier) {
        return getTierNameStatic(TIER_NAMES, tier);
    }

    public static int getCapacity(int tier) {
        if (tier < 0 || tier >= CAPACITIES.length) return CAPACITIES[0];
        return CAPACITIES[tier];
    }
}
