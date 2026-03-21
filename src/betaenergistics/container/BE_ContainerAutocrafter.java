package betaenergistics.container;

import betaenergistics.item.BE_ItemPattern;
import betaenergistics.tile.BE_TileAutocrafter;

import net.minecraft.src.*;

public class BE_ContainerAutocrafter extends Container {

    private BE_TileAutocrafter tile;

    public BE_ContainerAutocrafter(InventoryPlayer playerInv, BE_TileAutocrafter tile) {
        this.tile = tile;

        // Crafting grid 3x3 — slots 0-8
        addSlot(new SlotPattern(tile, 0, 29, 19));
        addSlot(new SlotPattern(tile, 1, 47, 19));
        addSlot(new SlotPattern(tile, 2, 65, 19));
        addSlot(new SlotPattern(tile, 3, 29, 37));
        addSlot(new SlotPattern(tile, 4, 47, 37));
        addSlot(new SlotPattern(tile, 5, 65, 37));
        addSlot(new SlotPattern(tile, 6, 29, 55));
        addSlot(new SlotPattern(tile, 7, 47, 55));
        addSlot(new SlotPattern(tile, 8, 65, 55));

        // Pattern storage — slots 9-17
        addSlot(new SlotPattern(tile, 9, 8, 85));
        addSlot(new SlotPattern(tile, 10, 26, 85));
        addSlot(new SlotPattern(tile, 11, 44, 85));
        addSlot(new SlotPattern(tile, 12, 62, 85));
        addSlot(new SlotPattern(tile, 13, 80, 85));
        addSlot(new SlotPattern(tile, 14, 98, 85));
        addSlot(new SlotPattern(tile, 15, 116, 85));
        addSlot(new SlotPattern(tile, 16, 134, 85));
        addSlot(new SlotPattern(tile, 17, 152, 85));

        // Output slot — slot 18 (centered in big_slot at 122,30 / 26x26)
        addSlot(new Slot(tile, 18, 127, 35));

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 176));
        }
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return tile.canInteractWith(player);
    }

    public BE_TileAutocrafter getCrafter() { return tile; }

    public ItemStack getStackInSlot(int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            int prevSize = slotStack.stackSize;
            if (slotIndex < 19) {
                func_28125_a(slotStack, 19, 55, true);
            } else {
                func_28125_a(slotStack, 0, 18, false);
            }
            if (slotStack.stackSize == prevSize) return null;
            if (slotStack.stackSize == 0) slot.putStack(null);
            else slot.onSlotChanged();
            slot.onPickupFromSlot(slotStack);
        }
        return result;
    }

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
