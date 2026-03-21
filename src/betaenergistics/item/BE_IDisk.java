package betaenergistics.item;

import net.minecraft.src.ItemStack;

/**
 * Common interface for all storage disk items (Item, Fluid, Gas).
 * Provides unified tooltip, naming, and tier management.
 */
public interface BE_IDisk {
    /** Get the tier index (0-5 for items, 0-3 for fluid/gas) from damage value */
    int getTierFromDamage(int damage);

    /** Get display name for this disk (e.g. "16K Storage Disk", "32K Fluid Disk") */
    String getDiskName(ItemStack stack);

    /** Get tooltip lines for the disk drive GUI. Returns array of 2-3 lines. */
    String[] getTooltipLines(ItemStack stack);

    /** Get capacity for a tier index */
    int getCapacityForTier(int tier);

    /** Check if damage value represents a blank (unregistered) disk */
    boolean isBlankDisk(int damage);

    /** Register a blank disk and return the new damage value (registry ID) */
    int registerDisk(int tier);

    /** Get the icon index for the specified tier */
    int getIconForTier(int tier);
}
