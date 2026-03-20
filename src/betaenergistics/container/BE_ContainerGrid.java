package betaenergistics.container;

import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Container for the Grid Terminal.
 * No machine slots — just the player inventory + virtual network storage.
 * Items in the network are NOT real slots; they're rendered as a virtual grid.
 */
public class BE_ContainerGrid extends Container {
    private BE_TileGrid grid;
    private List<BE_GridEntry> cachedItems = new ArrayList<BE_GridEntry>();
    private int lastItemHash = 0;

    public BE_ContainerGrid(InventoryPlayer playerInv, BE_TileGrid grid) {
        this.grid = grid;

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }

        refreshItems();
    }

    /** Refresh the cached item list from the network. */
    public void refreshItems() {
        cachedItems.clear();
        Map<BE_ItemKey, Integer> networkItems = grid.getNetworkItems();
        if (networkItems != null) {
            for (Map.Entry<BE_ItemKey, Integer> entry : networkItems.entrySet()) {
                cachedItems.add(new BE_GridEntry(entry.getKey(), entry.getValue()));
            }
            // Sort by item name/id
            Collections.sort(cachedItems, new Comparator<BE_GridEntry>() {
                public int compare(BE_GridEntry a, BE_GridEntry b) {
                    // Sort by item ID, then damage
                    int cmp = a.key.itemId - b.key.itemId;
                    return cmp != 0 ? cmp : a.key.damageValue - b.key.damageValue;
                }
            });
        }
    }

    @Override
    public void updateCraftingResults() {
        super.updateCraftingResults();
        // Periodically refresh the item list
        refreshItems();
    }

    public List<BE_GridEntry> getItems() {
        return cachedItems;
    }

    public BE_TileGrid getGrid() {
        return grid;
    }

    /**
     * Handle click on a virtual grid cell (not a real slot).
     * Called from the GUI when the player clicks on a network item.
     *
     * @param key The item key clicked
     * @param button 0=left (extract stack), 1=right (extract 1)
     * @param shiftHeld true if shift was held
     * @param player the player
     */
    public void handleGridClick(BE_ItemKey key, int button, boolean shiftHeld, EntityPlayer player) {
        ItemStack held = player.inventory.getItemStack();

        if (key == null && held != null) {
            // Clicked empty area with item on cursor → insert into network
            BE_ItemKey insertKey = new BE_ItemKey(held.itemID, held.getItemDamage());
            int toInsert = (button == 1) ? 1 : held.stackSize;
            int inserted = grid.insertItem(insertKey, toInsert);
            if (inserted > 0) {
                held.stackSize -= inserted;
                if (held.stackSize <= 0) {
                    player.inventory.setItemStack(null);
                } else {
                    player.inventory.setItemStack(held);
                }
            }
            return;
        }

        if (key != null && held == null) {
            // Clicked item with empty cursor → extract from network
            int maxExtract = (button == 1) ? 1 : 64;
            // Check item stack size limit
            Item item = Item.itemsList[key.itemId];
            if (item != null) {
                maxExtract = Math.min(maxExtract, item.getItemStackLimit());
            }
            int extracted = grid.extractItem(key, maxExtract);
            if (extracted > 0) {
                ItemStack stack = new ItemStack(key.itemId, extracted, key.damageValue);
                player.inventory.setItemStack(stack);
            }
            return;
        }

        if (key != null && held != null) {
            // Clicked item with item on cursor
            BE_ItemKey heldKey = new BE_ItemKey(held.itemID, held.getItemDamage());
            if (heldKey.equals(key)) {
                // Same item type → try to fill cursor stack
                Item item = Item.itemsList[key.itemId];
                int maxStack = (item != null) ? item.getItemStackLimit() : 64;
                int space = maxStack - held.stackSize;
                if (space > 0) {
                    int extracted = grid.extractItem(key, (button == 1) ? 1 : space);
                    if (extracted > 0) {
                        held.stackSize += extracted;
                        player.inventory.setItemStack(held);
                    }
                }
            } else {
                // Different item → insert held item into network
                BE_ItemKey insertKey = new BE_ItemKey(held.itemID, held.getItemDamage());
                int inserted = grid.insertItem(insertKey, held.stackSize);
                if (inserted > 0) {
                    held.stackSize -= inserted;
                    if (held.stackSize <= 0) {
                        player.inventory.setItemStack(null);
                    }
                }
            }
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return grid.worldObj.getBlockTileEntity(grid.xCoord, grid.yCoord, grid.zCoord) == grid
            && player.getDistanceSq(grid.xCoord + 0.5, grid.yCoord + 0.5, grid.zCoord + 0.5) <= 64.0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        // Shift-click from player inventory → insert into network
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot == null || !slot.getHasStack()) return null;

        ItemStack slotStack = slot.getStack();
        ItemStack result = slotStack.copy();

        BE_ItemKey key = new BE_ItemKey(slotStack.itemID, slotStack.getItemDamage());
        int inserted = grid.insertItem(key, slotStack.stackSize);
        if (inserted > 0) {
            slotStack.stackSize -= inserted;
            if (slotStack.stackSize <= 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            return result;
        }
        return null;
    }

    /** Entry in the virtual grid: an item type + count. */
    public static class BE_GridEntry {
        public final BE_ItemKey key;
        public int count;

        public BE_GridEntry(BE_ItemKey key, int count) {
            this.key = key;
            this.count = count;
        }
    }
}
