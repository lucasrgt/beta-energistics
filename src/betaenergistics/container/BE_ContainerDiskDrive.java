package betaenergistics.container;

import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.tile.BE_TileDiskDrive;

import net.minecraft.src.*;

/**
 * Container for Disk Drive — 8 disk slots (2x4) + player inventory.
 */
public class BE_ContainerDiskDrive extends Container {
    private BE_TileDiskDrive drive;

    public BE_ContainerDiskDrive(InventoryPlayer playerInv, BE_TileDiskDrive drive) {
        this.drive = drive;

        // 8 disk slots (2 columns x 4 rows), matching texture coordinates
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 2; col++) {
                this.addSlot(new BE_SlotDisk(drive, row * 2 + col, 71 + col * 18, 21 + row * 18));
            }
        }

        // Player inventory (y offset for 196px height GUI)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 114 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 172));
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return drive.canInteractWith(player);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            int prevSize = slotStack.stackSize;
            if (slotIndex < 8) {
                this.func_28125_a(slotStack, 8, 44, true);
            } else {
                if (slotStack.getItem() instanceof BE_ItemStorageDisk) {
                    this.func_28125_a(slotStack, 0, 8, false);
                } else {
                    return null;
                }
            }
            if (slotStack.stackSize == prevSize) return null;
            if (slotStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            slot.onPickupFromSlot(slotStack);
        }
        return result;
    }

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
