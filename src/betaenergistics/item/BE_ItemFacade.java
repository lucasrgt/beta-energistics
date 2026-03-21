package betaenergistics.item;

import betaenergistics.tile.BE_TileCable;
import mod_BetaEnergistics;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

/**
 * Facade item — disguises a cable face as a solid block.
 *
 * Damage value = block ID of the facade appearance.
 * Right-click on a cable to apply. Wrench/break to remove.
 */
public class BE_ItemFacade extends Item {
    public BE_ItemFacade(int itemId) {
        super(itemId);
        setMaxStackSize(64);
        setHasSubtypes(true);
        setItemName("beFacade");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int face) {
        if (world.multiplayerWorld) return false;

        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (!(te instanceof BE_TileCable)) return false;

        BE_TileCable cable = (BE_TileCable) te;
        int blockId = stack.getItemDamage();

        // Validate block ID
        if (blockId <= 0 || blockId >= Block.blocksList.length || Block.blocksList[blockId] == null) {
            return false;
        }

        // face: 0=bottom,1=top,2=north,3=south,4=west,5=east
        if (cable.getFacade(face) != 0) {
            return false; // already has a facade on this face
        }

        cable.setFacade(face, blockId);
        stack.stackSize--;
        world.markBlockNeedsUpdate(x, y, z);
        return true;
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        int blockId = stack.getItemDamage();
        if (blockId > 0 && blockId < Block.blocksList.length && Block.blocksList[blockId] != null) {
            return "ME Facade";
        }
        return "ME Facade";
    }

    /**
     * Get the texture index of the facade block for rendering the item.
     */
    @Override
    public int getIconFromDamage(int damage) {
        if (damage > 0 && damage < Block.blocksList.length && Block.blocksList[damage] != null) {
            return Block.blocksList[damage].getBlockTextureFromSide(2);
        }
        return iconIndex;
    }

    /**
     * Create a facade ItemStack for a given block ID.
     */
    public static ItemStack createFacade(int blockId) {
        return new ItemStack(mod_BetaEnergistics.itemFacade, 1, blockId);
    }
}
