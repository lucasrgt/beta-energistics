package betaenergistics.item;

import betaenergistics.storage.BE_FluidDiskRegistry;
import betaenergistics.storage.BE_FluidDiskStorage;

import net.minecraft.src.ItemStack;

public class BE_ItemFluidDisk extends BE_ItemDiskBase {
    private static final int[] CAPACITIES = {8000, 32000, 128000, 512000};
    private static final String[] TIER_NAMES = {"8K", "32K", "128K", "512K"};

    public BE_ItemFluidDisk(int itemId) {
        super(itemId, "beFluidDisk", 4, TIER_NAMES, CAPACITIES, "Fluid Disk", "mB", 4);
    }

    public int getTierFromDamage(int damage) {
        if (damage >= 0 && damage < 4) return damage;
        if (damage >= 10) {
            int tier = BE_FluidDiskRegistry.getTier(damage);
            return tier >= 0 ? tier : 0;
        }
        return 0;
    }

    public boolean isBlankDisk(int damage) {
        return BE_FluidDiskRegistry.isBlank(damage);
    }

    public int registerDisk(int tier) {
        return BE_FluidDiskRegistry.createDisk(tier, getCapacityForTier(tier));
    }

    @Override
    protected boolean isRegisteredDisk(int damage) {
        return BE_FluidDiskRegistry.isRegistered(damage);
    }

    @Override
    protected int getStoredAmount(int damage) {
        BE_FluidDiskStorage s = BE_FluidDiskRegistry.getDisk(damage);
        return s != null ? s.getStored() : -1;
    }

    @Override
    protected int getStoredCapacity(int damage) {
        BE_FluidDiskStorage s = BE_FluidDiskRegistry.getDisk(damage);
        return s != null ? s.getCapacity() : -1;
    }

    @Override
    protected int getStoredTypes(int damage) {
        BE_FluidDiskStorage s = BE_FluidDiskRegistry.getDisk(damage);
        return s != null ? s.getTypeCount() : -1;
    }

    @Override
    protected void updateRegistryDiskName(int damage) {
        BE_FluidDiskRegistry.updateDiskName(damage);
    }

    @Override
    protected String getRegistryLocPrefix() {
        return "beFluidDisk";
    }

    // Static helpers (used by BE_FluidDiskRegistry)

    public static String getTierName(int tier) {
        return getTierNameStatic(TIER_NAMES, tier);
    }

    public static int getCapacity(int tier) {
        if (tier < 0 || tier >= CAPACITIES.length) return CAPACITIES[0];
        return CAPACITIES[tier];
    }
}
