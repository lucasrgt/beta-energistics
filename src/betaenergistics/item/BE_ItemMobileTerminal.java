package betaenergistics.item;

import betaenergistics.gui.BE_GuiGrid;
import betaenergistics.mod_BetaEnergistics;
import betaenergistics.storage.BE_MobileTerminalRegistry;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.*;

/**
 * Mobile Terminal — handheld item that opens Grid Terminal GUI remotely.
 *
 * Shift+right-click on a Grid Terminal to link.
 * Right-click in hand to open the linked Grid Terminal's GUI.
 * Only works if the linked terminal's chunk is loaded.
 *
 * damage 0 = unlinked
 * damage >= 1 = linked (ID in MobileTerminalRegistry)
 */
public class BE_ItemMobileTerminal extends Item {
    public BE_ItemMobileTerminal(int itemId) {
        super(itemId);
        setHasSubtypes(false);
        setMaxStackSize(1);
        setMaxDamage(0);
        setItemName("beMobileTerminal");
    }

    /**
     * Right-click on a block — if sneaking and clicking a Grid Terminal, link to it.
     */
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side) {
        if (world.multiplayerWorld) return false;

        if (player.isSneaking()) {
            int blockId = world.getBlockId(x, y, z);
            if (blockId == mod_BetaEnergistics.ID_GRID) {
                TileEntity te = world.getBlockTileEntity(x, y, z);
                if (te instanceof BE_TileGrid) {
                    // Link this terminal to the Grid Terminal
                    int linkId = BE_MobileTerminalRegistry.linkTerminal(x, y, z);
                    stack.setItemDamage(linkId);
                    player.addChatMessage("Mobile Terminal linked to Grid at " + x + ", " + y + ", " + z);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Right-click in air — open linked Grid Terminal GUI if chunk is loaded.
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.multiplayerWorld) return stack;

        int dmg = stack.getItemDamage();
        if (!BE_MobileTerminalRegistry.isLinked(dmg)) {
            player.addChatMessage("Terminal not linked. Shift+right-click a Grid Terminal to link.");
            return stack;
        }

        int[] coords = BE_MobileTerminalRegistry.getLinkedCoords(dmg);
        if (coords == null) return stack;

        int tx = coords[0];
        int ty = coords[1];
        int tz = coords[2];

        // Check if chunk is loaded
        if (!world.checkChunksExist(tx, ty, tz, tx, ty, tz)) {
            player.addChatMessage("Linked terminal is too far away (chunk not loaded).");
            return stack;
        }

        TileEntity te = world.getBlockTileEntity(tx, ty, tz);
        if (!(te instanceof BE_TileGrid)) {
            player.addChatMessage("Linked Grid Terminal no longer exists at " + tx + ", " + ty + ", " + tz + ".");
            return stack;
        }

        BE_TileGrid grid = (BE_TileGrid) te;
        if (grid.getNetwork() == null || !grid.getNetwork().isActive()) {
            player.addChatMessage("Linked network is offline.");
            return stack;
        }

        // Open the Grid Terminal GUI using the remote tile
        ModLoader.OpenGUI(player, new BE_GuiGrid(player.inventory, grid));
        return stack;
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        return super.getItemName();
    }
}
