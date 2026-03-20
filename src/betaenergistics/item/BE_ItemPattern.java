package betaenergistics.item;

import betaenergistics.storage.BE_PatternRegistry;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

/**
 * Pattern item — blank or encoded crafting pattern.
 *
 * damage 0 = blank pattern
 * damage >= 1 = encoded pattern (ID in PatternRegistry)
 *
 * In Beta 1.7.3, ItemStack has no NBT tags. Recipe data is stored
 * in BE_PatternRegistry, keyed by the item's damage value.
 */
public class BE_ItemPattern extends Item {
    public BE_ItemPattern(int itemId) {
        super(itemId);
        setMaxStackSize(1);
        setHasSubtypes(true);
        setItemName("bePattern");
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        if (stack.getItemDamage() == 0) {
            return "Blank Pattern";
        }
        // Encoded pattern — localization key set by PatternRegistry
        return "bePattern" + stack.getItemDamage();
    }
}
