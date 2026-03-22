package betaenergistics.item;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

/**
 * Abstract base for all disk items (storage, fluid, gas).
 * Provides shared setTierIcon, getIconFromDamage, tooltip patterns.
 * Subclasses provide registry-specific logic (isRegistered, registerDisk, etc.).
 */
public abstract class BE_ItemDiskBase extends Item implements BE_IDisk {
    protected final int[] tierIcons;
    protected final String[] tierNames;
    protected final int[] capacities;
    protected final String diskPrefix;
    protected final String diskLabel;
    protected final String unit;
    protected final int maxTypes;

    /**
     * @param itemId     the item ID
     * @param itemName   the item name key (e.g. "beStorageDisk")
     * @param numTiers   number of tiers (6 for items, 4 for fluid/gas)
     * @param tierNames  display names per tier (e.g. {"1K","4K",...})
     * @param capacities capacity per tier
     * @param diskLabel  label after tier name (e.g. "Storage Disk", "Fluid Disk")
     * @param unit       unit for tooltip (e.g. "items", "mB")
     * @param maxTypes   max types for tooltip display
     */
    public BE_ItemDiskBase(int itemId, String itemName, int numTiers,
                           String[] tierNames, int[] capacities,
                           String diskLabel, String unit, int maxTypes) {
        super(itemId);
        setHasSubtypes(false);
        setMaxStackSize(1);
        setMaxDamage(0);
        setItemName(itemName);
        this.tierIcons = new int[numTiers];
        this.tierNames = tierNames;
        this.capacities = capacities;
        this.diskPrefix = itemName;
        this.diskLabel = diskLabel;
        this.unit = unit;
        this.maxTypes = maxTypes;
    }

    public void setTierIcon(int tier, int iconIndex) {
        if (tier >= 0 && tier < tierIcons.length) {
            tierIcons[tier] = iconIndex;
        }
    }

    public String getDiskName(ItemStack stack) {
        int tier = getTierFromDamage(stack.getItemDamage());
        return tierNames[tier] + " " + diskLabel;
    }

    public int getCapacityForTier(int tier) {
        if (tier < 0 || tier >= capacities.length) return capacities[0];
        return capacities[tier];
    }

    public int getIconForTier(int tier) {
        return tierIcons[Math.min(tier, tierIcons.length - 1)];
    }

    /**
     * Check if this damage value represents a registered disk in the registry.
     */
    protected abstract boolean isRegisteredDisk(int damage);

    /**
     * Get stored amount for a registered disk. Returns -1 if not found.
     */
    protected abstract int getStoredAmount(int damage);

    /**
     * Get capacity for a registered disk. Returns -1 if not found.
     */
    protected abstract int getStoredCapacity(int damage);

    /**
     * Get type count for a registered disk. Returns -1 if not found.
     */
    protected abstract int getStoredTypes(int damage);

    /**
     * Update the registry's disk name localization for the given damage.
     */
    protected abstract void updateRegistryDiskName(int damage);

    /**
     * Get the localization key prefix for registered disks (e.g. "beDisk", "beFluidDisk").
     */
    protected abstract String getRegistryLocPrefix();

    public String[] getTooltipLines(ItemStack stack) {
        int dmg = stack.getItemDamage();
        int tier = getTierFromDamage(dmg);
        String name = tierNames[tier] + " " + diskLabel;

        if (isRegisteredDisk(dmg)) {
            int stored = getStoredAmount(dmg);
            int capacity = getStoredCapacity(dmg);
            int types = getStoredTypes(dmg);
            if (stored >= 0) {
                return new String[]{
                    name,
                    stored + " / " + capacity + " " + unit,
                    types + " / " + maxTypes + " types"
                };
            }
        }
        return new String[]{name, "Empty"};
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        int dmg = stack.getItemDamage();
        if (isRegisteredDisk(dmg)) {
            updateRegistryDiskName(dmg);
            return getRegistryLocPrefix() + dmg;
        }
        // For blank tiers (0 to numTiers-1), use tier-specific name
        if (dmg >= 0 && dmg < tierNames.length) {
            return diskPrefix + dmg;
        }
        // For any other damage value (TMI artifacts), use tier 0 name
        return diskPrefix + "0";
    }

    @Override
    public int getIconFromDamage(int damage) {
        return tierIcons[getTierFromDamage(damage)];
    }

    // Static helper for tier name lookups (used by registries)
    public static String getTierNameStatic(String[] names, int tier) {
        if (tier < 0 || tier >= names.length) return names[0];
        return names[tier];
    }
}
