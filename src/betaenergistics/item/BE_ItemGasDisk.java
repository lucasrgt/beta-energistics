package betaenergistics.item;

import betaenergistics.storage.BE_GasDiskRegistry;
import betaenergistics.storage.BE_GasDiskStorage;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class BE_ItemGasDisk extends Item implements BE_IDisk {
    private static final int[] CAPACITIES = {8000, 32000, 128000, 512000};
    private static final String[] TIER_NAMES = {"8K", "32K", "128K", "512K"};

    private int[] tierIcons = new int[4];

    public BE_ItemGasDisk(int itemId) {
        super(itemId);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setItemName("beGasDisk");
    }

    public void setTierIcon(int tier, int iconIndex) {
        if (tier >= 0 && tier < tierIcons.length) {
            tierIcons[tier] = iconIndex;
        }
    }

    // BE_IDisk implementation

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

    public String getDiskName(ItemStack stack) {
        int tier = getTierFromDamage(stack.getItemDamage());
        return TIER_NAMES[tier] + " Gas Disk";
    }

    public String[] getTooltipLines(ItemStack stack) {
        int dmg = stack.getItemDamage();
        int tier = getTierFromDamage(dmg);
        String name = TIER_NAMES[tier] + " Gas Disk";

        if (BE_GasDiskRegistry.isRegistered(dmg)) {
            BE_GasDiskStorage storage = BE_GasDiskRegistry.getStorage(dmg);
            if (storage != null) {
                int stored = storage.getStored();
                int capacity = storage.getCapacity();
                int types = storage.getAllGases().size();
                return new String[]{
                    name,
                    stored + " / " + capacity + " mB",
                    types + " / 4 types"
                };
            }
        }
        return new String[]{name, "Empty"};
    }

    public int getCapacityForTier(int tier) {
        return getCapacity(tier);
    }

    public boolean isBlankDisk(int damage) {
        return damage >= 0 && damage < 4;
    }

    public int registerDisk(int tier) {
        return BE_GasDiskRegistry.assignId(tier);
    }

    public int getIconForTier(int tier) {
        return tierIcons[Math.min(tier, tierIcons.length - 1)];
    }

    // Static helpers

    public static int getCapacity(int tier) {
        if (tier < 0 || tier >= CAPACITIES.length) return CAPACITIES[0];
        return CAPACITIES[tier];
    }

    // Item overrides

    @Override
    public String getItemNameIS(ItemStack stack) {
        int dmg = stack.getItemDamage();
        if (dmg < 4) return "beGasDisk" + dmg;
        if (BE_GasDiskRegistry.isRegistered(dmg)) {
            BE_GasDiskRegistry.updateDiskName(dmg);
            return "beGasDisk" + dmg;
        }
        return "beGasDisk" + getTierFromDamage(dmg);
    }

    @Override
    public int getIconFromDamage(int damage) {
        return tierIcons[getTierFromDamage(damage)];
    }
}
