package betaenergistics.container;

import betaenergistics.item.BE_ItemPattern;
import betaenergistics.tile.BE_TileRecipeEncoder;

import net.minecraft.src.*;

/**
 * Container for Recipe Encoder.
 *
 * Slot layout:
 *   0-8:   ghost input grid (3x3) — BE_SlotGhost, items not consumed
 *   9:     ghost output — BE_SlotGhost
 *   10:    blank pattern input (accepts BE_ItemPattern damage 0 only)
 *   11:    encoded pattern output (extract only)
 *   12-38: player inventory (3 rows at y=114)
 *   39-47: player hotbar (at y=172)
 */
public class BE_ContainerRecipeEncoder extends Container {
    private BE_TileRecipeEncoder encoder;

    public BE_ContainerRecipeEncoder(InventoryPlayer playerInv, BE_TileRecipeEncoder encoder) {
        this.encoder = encoder;

        // Ghost input slots (3x3 grid) — indices 0-8 (coords from Machine Maker codegen)
        this.addSlot(new BE_SlotGhost(encoder, 0, 14, 29));
        this.addSlot(new BE_SlotGhost(encoder, 1, 32, 29));
        this.addSlot(new BE_SlotGhost(encoder, 2, 50, 29));
        this.addSlot(new BE_SlotGhost(encoder, 3, 14, 47));
        this.addSlot(new BE_SlotGhost(encoder, 4, 32, 47));
        this.addSlot(new BE_SlotGhost(encoder, 5, 50, 47));
        this.addSlot(new BE_SlotGhost(encoder, 6, 14, 65));
        this.addSlot(new BE_SlotGhost(encoder, 7, 32, 65));
        this.addSlot(new BE_SlotGhost(encoder, 8, 50, 65));

        // Ghost output slot — index 9 (centered in big_slot at 107,40 / 26x26)
        this.addSlot(new BE_SlotGhost(encoder, BE_TileRecipeEncoder.SLOT_OUTPUT, 112, 45));

        // Blank pattern input slot — index 10
        this.addSlot(new SlotPatternInput(encoder, BE_TileRecipeEncoder.SLOT_PATTERN_IN, 146, 29));

        // Encoded pattern output slot — index 11
        this.addSlot(new SlotPatternOutput(encoder, BE_TileRecipeEncoder.SLOT_PATTERN_OUT, 146, 65));

        // Player inventory (3x9) — y = 196 - 83 = 113, +1 border = 114
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 114 + row * 18));
            }
        }
        // Player hotbar at y=172
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 172));
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return encoder.canInteractWith(player);
    }

    public BE_TileRecipeEncoder getEncoder() { return encoder; }

    /**
     * Override slotClick to handle ghost slot behavior.
     * Ghost slots (0-9): copy held item as reference (stackSize=1), don't consume.
     * Slot 11 (output): extract only, no insert.
     * Other slots: normal behavior.
     */
    @Override
    public ItemStack func_27280_a(int slotId, int mouseButton, boolean shiftClick, EntityPlayer player) {
        // Ghost slots: 0-8 (inputs) and 9 (output)
        if (slotId >= 0 && slotId <= 9) {
            ItemStack held = player.inventory.getItemStack();
            boolean isOutput = (slotId == 9);
            boolean rightClick = (mouseButton == 1);

            if (isOutput && !encoder.isProcessingMode()) {
                return null; // Output ghost only editable in processing mode
            }

            if (encoder.isProcessingMode()) {
                // Processing mode: left = set full stack, right = +1/-1
                if (held != null) {
                    if (rightClick) {
                        ItemStack current = encoder.getStackInSlot(slotId);
                        if (current != null && current.itemID == held.itemID && current.getItemDamage() == held.getItemDamage()) {
                            current.stackSize = Math.min(current.stackSize + 1, 64);
                            encoder.setInventorySlotContents(slotId, current);
                        } else {
                            ItemStack one = new ItemStack(held.itemID, 1, held.getItemDamage());
                            if (isOutput) encoder.setOutputSlot(one);
                            else encoder.setGhostSlot(slotId, one);
                        }
                    } else {
                        if (isOutput) encoder.setOutputSlot(held);
                        else encoder.setGhostSlot(slotId, held);
                    }
                } else {
                    if (rightClick) {
                        ItemStack current = encoder.getStackInSlot(slotId);
                        if (current != null && current.stackSize > 1) {
                            current.stackSize--;
                            encoder.setInventorySlotContents(slotId, current);
                        } else {
                            if (isOutput) encoder.setOutputSlot(null);
                            else encoder.setGhostSlot(slotId, null);
                        }
                    } else {
                        if (isOutput) encoder.setOutputSlot(null);
                        else encoder.setGhostSlot(slotId, null);
                    }
                }
            } else {
                // Crafting mode: simple set/clear (qty always 1)
                if (held != null) {
                    encoder.setGhostSlot(slotId, held);
                } else {
                    encoder.setGhostSlot(slotId, null);
                }
            }
            return null;
        }

        // Default handling for pattern slots and player inventory
        return super.func_27280_a(slotId, mouseButton, shiftClick, player);
    }

    /**
     * Toggle between crafting and processing mode.
     */
    public void toggleMode() {
        encoder.toggleMode();
    }

    /**
     * Encode the current recipe.
     */
    public boolean encode() {
        return encoder.encode();
    }

    /**
     * Clear all ghost slots.
     */
    public void clearRecipe() {
        encoder.clearRecipe();
    }

    /**
     * Shift-click transfer logic.
     */
    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            int prevSize = slotStack.stackSize;

            if (slotIndex < 10) {
                // Ghost slots — no shift-click transfer
                return null;
            } else if (slotIndex == 10) {
                // Pattern input → move to player inventory
                this.func_28125_a(slotStack, 12, 48, true);
            } else if (slotIndex == 11) {
                // Encoded pattern output → move to player inventory
                this.func_28125_a(slotStack, 12, 48, true);
            } else {
                // Player inventory → try pattern input slot if it's a pattern
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
    static class BE_SlotGhost extends Slot {
        public BE_SlotGhost(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return true;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    /**
     * Pattern input slot — accepts only blank BE_ItemPattern (damage 0).
     */
    static class SlotPatternInput extends Slot {
        public SlotPatternInput(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null && stack.getItem() instanceof BE_ItemPattern;
        }

        @Override
        public int getSlotStackLimit() {
            return 64;
        }
    }

    /**
     * Pattern output slot — player can take but not insert.
     */
    static class SlotPatternOutput extends Slot {
        public SlotPatternOutput(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false; // Cannot insert into output
        }

        @Override
        public int getSlotStackLimit() {
            return 64;
        }
    }
}
