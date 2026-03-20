package betaenergistics.container;

import betaenergistics.tile.BE_TileRedstoneEmitter;

import net.minecraft.src.*;

/**
 * Container for the Redstone Emitter.
 * No item slots — filter is a ghost slot handled by the GUI.
 * Threshold and mode changes are delegated to the tile entity.
 */
public class BE_ContainerRedstoneEmitter extends Container {
    private BE_TileRedstoneEmitter tile;

    public BE_ContainerRedstoneEmitter(InventoryPlayer playerInv, BE_TileRedstoneEmitter tile) {
        this.tile = tile;

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile
            && player.getDistanceSq(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5) <= 64.0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return null;
    }

    public BE_TileRedstoneEmitter getTile() {
        return tile;
    }
}
