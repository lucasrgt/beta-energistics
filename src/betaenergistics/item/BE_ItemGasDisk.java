package betaenergistics.item;

import betaenergistics.storage.BE_GasDiskRegistry;
import betaenergistics.storage.BE_GasDiskStorage;

import net.minecraft.src.ItemStack;

public class BE_ItemGasDisk extends BE_ItemDiskBase {
    private static final int[] CAPACITIES = {8000, 32000, 128000, 512000};
    private static final String[] TIER_NAMES = {"8K", "32K", "128K", "512K"};

    public BE_ItemGasDisk(int itemId) {
        super(itemId, "beGasDisk", 4, TIER_NAMES, CAPACITIES, "Gas Disk", "mB", 4);
    }

    public int getTierFromDamage(int damage) {
        if (damage >= 0 && damage < 4) return damage;
        if (damage >= 10) {
            BE_GasDiskStorage s = BE_GasDiskRegistry.getStorage(damage);
            if (s != null) {
                int cap = s.getCapacity();
                if (cap <= 8000) return 0;
                if (cap <= 32000) return 1;
                if (cap <= 128000) return 2;
                return 3;
            }
        }
        return 0;
    }

    public boolean isBlankDisk(int damage) {
        return damage >= 0 && damage < 4;
    }

    public int registerDisk(int tier) {
        return BE_GasDiskRegistry.assignId(tier);
    }

    @Override
    protected boolean isRegisteredDisk(int damage) {
        return BE_GasDiskRegistry.isRegistered(damage);
    }

    @Override
    protected int getStoredAmount(int damage) {
        BE_GasDiskStorage s = BE_GasDiskRegistry.getStorage(damage);
        return s != null ? s.getStored() : -1;
    }

    @Override
    protected int getStoredCapacity(int damage) {
        BE_GasDiskStorage s = BE_GasDiskRegistry.getStorage(damage);
        return s != null ? s.getCapacity() : -1;
    }

    @Override
    protected int getStoredTypes(int damage) {
        BE_GasDiskStorage s = BE_GasDiskRegistry.getStorage(damage);
        return s != null ? s.getAllGases().size() : -1;
    }

    @Override
    protected void updateRegistryDiskName(int damage) {
        BE_GasDiskRegistry.updateDiskName(damage);
    }

    @Override
    protected String getRegistryLocPrefix() {
        return "beGasDisk";
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        int dmg = stack.getItemDamage();
        if (dmg < 4) return "beGasDisk" + dmg;
        if (isRegisteredDisk(dmg)) {
            updateRegistryDiskName(dmg);
            return "beGasDisk" + dmg;
        }
        return "beGasDisk" + getTierFromDamage(dmg);
    }

    // Static helpers

    public static int getCapacity(int tier) {
        if (tier < 0 || tier >= CAPACITIES.length) return CAPACITIES[0];
        return CAPACITIES[tier];
    }
}
