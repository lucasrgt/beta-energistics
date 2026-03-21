package betaenergistics.container;

import betaenergistics.tile.BE_TileFluidRedstoneEmitter;

import net.minecraft.src.*;

/**
 * Container for the Fluid Redstone Emitter.
 * No item slots — fluid filter is a ghost interaction handled by the GUI.
 */
public class BE_ContainerFluidRedstoneEmitter extends Container {
    private BE_TileFluidRedstoneEmitter tile;

    public BE_ContainerFluidRedstoneEmitter(InventoryPlayer playerInv, BE_TileFluidRedstoneEmitter tile) {
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

    public BE_TileFluidRedstoneEmitter getTile() {
        return tile;
    }
}
