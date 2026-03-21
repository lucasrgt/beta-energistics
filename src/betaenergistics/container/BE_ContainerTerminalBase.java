package betaenergistics.container;

import betaenergistics.tile.BE_TileTerminalBase;

import net.minecraft.src.*;

/**
 * Abstract base for all terminal containers (Grid, Fluid, Gas).
 * Provides shared player inventory slot layout and canInteractWith check.
 */
public abstract class BE_ContainerTerminalBase extends Container {

    /** Get the terminal tile entity for this container. */
    protected abstract BE_TileTerminalBase getTerminalTile();

    /**
     * Add the standard player inventory slots at the terminal positions.
     * 3 rows at y=158, hotbar at y=216, all starting at x=8.
     */
    protected void addPlayerInventory(InventoryPlayer playerInv) {
        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 158 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 216));
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        BE_TileTerminalBase tile = getTerminalTile();
        return tile.canInteractWith(player);
    }
}
