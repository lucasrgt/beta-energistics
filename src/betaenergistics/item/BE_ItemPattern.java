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
        setHasSubtypes(false);
        setItemName("bePattern");
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        int dmg = stack.getItemDamage();
        if (dmg >= 1 && BE_PatternRegistry.isRegistered(dmg)) {
            return "bePattern" + dmg;
        }
        return super.getItemName();
    }
}
