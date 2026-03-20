package betaenergistics.container;

import betaenergistics.item.BE_ItemPattern;
import betaenergistics.tile.BE_TileAutocrafter;

import net.minecraft.src.*;

/**
 * Container for the Autocrafter GUI.
 *
 * Slot layout:
 *   0-8:   pattern slots (3x3 grid, accept only BE_ItemPattern, stack limit 1)
 *   9-35:  player inventory
 *   36-44: player hotbar
 *
 * GUI layout (176x166):
 *   Pattern grid 3x3: starting at (62, 17), 18px spacing
 *   Progress bar area: below grid
 *   Player inventory: standard bottom layout
 */
public class BE_ContainerAutocrafter extends Container {
    private BE_TileAutocrafter crafter;

    public BE_ContainerAutocrafter(InventoryPlayer playerInv, BE_TileAutocrafter crafter) {
        this.crafter = crafter;

        // Pattern slots (3x3 grid) — indices 0-8
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotPattern(crafter, row * 3 + col, 62 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory (3x9)
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
        return crafter.canInteractWith(player);
    }

    public BE_TileAutocrafter getCrafter() { return crafter; }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            int prevSize = slotStack.stackSize;

            if (slotIndex < 9) {
                // Pattern slot → move to player inventory
                this.func_28125_a(slotStack, 9, 45, true);
            } else {
                // Player inventory → try pattern slots if it's a pattern
                if (slotStack.getItem() instanceof BE_ItemPattern) {
                    this.func_28125_a(slotStack, 0, 9, false);
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

    /**
     * Pattern slot — accepts only BE_ItemPattern, stack limit 1.
     */
    static class SlotPattern extends Slot {
        public SlotPattern(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null && stack.getItem() instanceof BE_ItemPattern;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
