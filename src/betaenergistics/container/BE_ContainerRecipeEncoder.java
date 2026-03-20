package betaenergistics.container;

import betaenergistics.item.BE_ItemPattern;
import betaenergistics.tile.BE_TileRecipeEncoder;

import net.minecraft.src.*;

/**
 * Container for Recipe Encoder.
 *
 * Slot layout:
 *   0-8:   ghost input grid (3x3) — special handling, items not consumed
 *   9:     output preview (read-only)
 *   10:    pattern slot (accepts BE_ItemPattern only)
 *   11-37: player inventory
 *   38-46: player hotbar
 *
 * GUI layout (176x166):
 *   Ghost grid 3x3: starting at (30, 17), 18px spacing
 *   Output preview: (124, 35)
 *   Pattern slot: (124, 53) — below output
 *   Encode button: rendered via GUI drawRect
 */
public class BE_ContainerRecipeEncoder extends Container {
    private BE_TileRecipeEncoder encoder;

    public BE_ContainerRecipeEncoder(InventoryPlayer playerInv, BE_TileRecipeEncoder encoder) {
        this.encoder = encoder;

        // Ghost input slots (3x3 grid) — indices 0-8
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotGhost(encoder, row * 3 + col, 30 + col * 18, 17 + row * 18));
            }
        }

        // Output preview slot — index 9 (read-only)
        this.addSlot(new SlotOutputPreview(encoder, BE_TileRecipeEncoder.SLOT_OUTPUT, 124, 35));

        // Pattern slot — index 10
        this.addSlot(new SlotPattern(encoder, BE_TileRecipeEncoder.SLOT_PATTERN, 124, 53));

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
        return encoder.canInteractWith(player);
    }

    /**
     * Handle ghost slot clicks — copy item to slot without consuming.
     */
    public void handleGhostClick(int slotIndex, ItemStack cursorStack) {
        if (slotIndex < 0 || slotIndex >= BE_TileRecipeEncoder.GHOST_SLOTS) return;
        if (cursorStack != null) {
            encoder.setGhostSlot(slotIndex, cursorStack);
        } else {
            encoder.setGhostSlot(slotIndex, null);
        }
    }

    /**
     * Encode the current recipe.
     */
    public boolean encode() {
        return encoder.encodePattern();
    }

    public BE_TileRecipeEncoder getEncoder() { return encoder; }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            int prevSize = slotStack.stackSize;

            if (slotIndex < 9) {
                // Ghost slots — no shift-click
                return null;
            } else if (slotIndex == 9) {
                // Output preview — no shift-click
                return null;
            } else if (slotIndex == 10) {
                // Pattern slot → move to player
                this.func_28125_a(slotStack, 11, 47, true);
            } else {
                // Player inventory → try pattern slot if it's a pattern
                if (slotStack.getItem() instanceof BE_ItemPattern) {
                    this.func_28125_a(slotStack, 10, 11, false);
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
     * Ghost slot — shows item but doesn't actually hold it. Item not consumed on click.
     */
    static class SlotGhost extends Slot {
        public SlotGhost(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return true; // Accept anything visually
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    /**
     * Output preview slot — read-only, cannot take items.
     */
    static class SlotOutputPreview extends Slot {
        public SlotOutputPreview(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public int getSlotStackLimit() {
            return 0;
        }
    }

    /**
     * Pattern slot — accepts only BE_ItemPattern.
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
