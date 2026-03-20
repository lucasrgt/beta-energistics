package betaenergistics.item;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

/**
 * Pattern item — blank crafting pattern.
 *
 * In Beta 1.7.3, ItemStack has no NBT tags. Recipe data (inputs + output)
 * is stored directly in the BE_TileAutocrafter's own NBT, per pattern slot.
 * The item is always a blank pattern; encoding happens in the Autocrafter tile.
 */
public class BE_ItemPattern extends Item {
    public BE_ItemPattern(int itemId) {
        super(itemId);
        setMaxStackSize(1);
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        return "Blank Pattern";
    }
}
