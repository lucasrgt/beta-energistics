package betaenergistics.container;

import betaenergistics.tile.BE_TileCraftingMonitor;

import net.minecraft.src.*;

/**
 * Container for the Crafting Monitor (176x166).
 * No machine slots — just player inventory.
 */
public class BE_ContainerCraftingMonitor extends Container {
    private BE_TileCraftingMonitor monitor;

    public BE_ContainerCraftingMonitor(InventoryPlayer playerInv, BE_TileCraftingMonitor monitor) {
        this.monitor = monitor;

        // Player inventory (3 rows) — ySize=166, inv at ySize-83=83
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar at ySize-25=141
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public BE_TileCraftingMonitor getMonitor() { return monitor; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return monitor.canInteractWith(player);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return null;
    }
}
