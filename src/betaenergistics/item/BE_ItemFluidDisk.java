package betaenergistics.item;

import betaenergistics.storage.BE_FluidDiskRegistry;
import betaenergistics.storage.BE_FluidDiskStorage;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class BE_ItemFluidDisk extends Item implements BE_IDisk {
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

    // BE_IDisk implementation

    public int getTierFromDamage(int damage) {
        if (damage >= 0 && damage < 4) return damage;
        if (damage >= 10) {
            int tier = BE_FluidDiskRegistry.getTier(damage);
            return tier >= 0 ? tier : 0;
        }
        return 0;
    }

    public String getDiskName(ItemStack stack) {
        int tier = getTierFromDamage(stack.getItemDamage());
        return TIER_NAMES[tier] + " Fluid Disk";
    }

    public String[] getTooltipLines(ItemStack stack) {
        int dmg = stack.getItemDamage();
        int tier = getTierFromDamage(dmg);
        String name = TIER_NAMES[tier] + " Fluid Disk";

        if (BE_FluidDiskRegistry.isRegistered(dmg)) {
            BE_FluidDiskStorage storage = BE_FluidDiskRegistry.getDisk(dmg);
            if (storage != null) {
                int stored = storage.getStored();
                int capacity = storage.getCapacity();
                int types = storage.getAllFluids().size();
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
        return BE_FluidDiskRegistry.isBlank(damage);
    }

    public int registerDisk(int tier) {
        return BE_FluidDiskRegistry.createDisk(tier, getCapacity(tier));
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
        if (BE_FluidDiskRegistry.isRegistered(dmg)) {
            BE_FluidDiskRegistry.updateDiskName(dmg);
            return "beFluidDisk" + dmg;
        }
        return "beFluidDisk" + dmg;
    }

    @Override
    public int getIconFromDamage(int damage) {
        return tierIcons[getTierFromDamage(damage)];
    }
}
