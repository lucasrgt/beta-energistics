package betaenergistics.container;

import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.tile.BE_TileDiskDrive;

import net.minecraft.src.*;

/**
 * Container for Disk Drive — 6 disk slots + player inventory.
 */
public class BE_ContainerDiskDrive extends Container {
    private BE_TileDiskDrive drive;

    public BE_ContainerDiskDrive(InventoryPlayer playerInv, BE_TileDiskDrive drive) {
        this.drive = drive;

        // 6 disk slots (2 columns x 3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                this.addSlot(new BE_SlotDisk(drive, row * 2 + col, 62 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory
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
        return drive.isUseableByPlayer(player);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            if (slotIndex < 6) {
                // Shift-click from disk slot to player inventory
                if (!this.func_28125_a(slotStack, 6, 42, true)) return null;
            } else {
                // Shift-click from player to disk slot
                if (slotStack.getItem() instanceof BE_ItemStorageDisk) {
                    if (!this.func_28125_a(slotStack, 0, 6, false)) return null;
                } else {
                    return null;
                }
            }
            if (slotStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            if (slotStack.stackSize == result.stackSize) return null;
            slot.onPickupFromSlot(slotStack);
        }
        return result;
    }

    /** Slot that only accepts storage disks. */
    static class BE_SlotDisk extends Slot {
        public BE_SlotDisk(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null && stack.getItem() instanceof BE_ItemStorageDisk;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
