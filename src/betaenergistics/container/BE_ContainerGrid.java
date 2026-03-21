package betaenergistics.container;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.network.BE_PacketHandler;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileGrid;
import betaenergistics.tile.BE_TileTerminalBase;

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
public class BE_ContainerGrid extends BE_ContainerTerminalBase {
    public static final int SORT_BY_ID = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_QUANTITY = 2;
    public static final String[] SORT_NAMES = {"ID", "Name", "Qty"};

    private BE_TileGrid grid;
    private List<BE_GridEntry> cachedItems = new ArrayList<BE_GridEntry>();
    private int lastItemHash = 0;
    private int sortMode = SORT_BY_ID;

    public BE_ContainerGrid(InventoryPlayer playerInv, BE_TileGrid grid) {
        this.grid = grid;
        addPlayerInventory(playerInv);
        refreshItems();
    }

    @Override
    protected BE_TileTerminalBase getTerminalTile() {
        return grid;
    }

    public int getSortMode() {
        return sortMode;
    }

    public void cycleSortMode() {
        sortMode = (sortMode + 1) % 3;
    }

    public void setSortMode(int mode) {
        sortMode = mode % 3;
    }

    /** Refresh the cached item list from the network. */
    public void refreshItems() {
        cachedItems.clear();
        Map<BE_ItemKey, Integer> networkItems = grid.getNetworkItems();
        if (networkItems != null) {
            for (Map.Entry<BE_ItemKey, Integer> entry : networkItems.entrySet()) {
                cachedItems.add(new BE_GridEntry(entry.getKey(), entry.getValue()));
            }
            switch (sortMode) {
                case SORT_BY_NAME:
                    Collections.sort(cachedItems, new Comparator<BE_GridEntry>() {
                        public int compare(BE_GridEntry a, BE_GridEntry b) {
                            String nameA = getItemName(a.key);
                            String nameB = getItemName(b.key);
                            int cmp = nameA.compareToIgnoreCase(nameB);
                            return cmp != 0 ? cmp : a.key.itemId - b.key.itemId;
                        }
                    });
                    break;
                case SORT_BY_QUANTITY:
                    Collections.sort(cachedItems, new Comparator<BE_GridEntry>() {
                        public int compare(BE_GridEntry a, BE_GridEntry b) {
                            int cmp = b.count - a.count;
                            return cmp != 0 ? cmp : a.key.itemId - b.key.itemId;
                        }
                    });
                    break;
                default: // SORT_BY_ID
                    Collections.sort(cachedItems, new Comparator<BE_GridEntry>() {
                        public int compare(BE_GridEntry a, BE_GridEntry b) {
                            int cmp = a.key.itemId - b.key.itemId;
                            return cmp != 0 ? cmp : a.key.damageValue - b.key.damageValue;
                        }
                    });
                    break;
            }
        }
    }

    private static String getItemName(BE_ItemKey key) {
        Item item = Item.itemsList[key.itemId];
        if (item == null) return "";
        String name = item.getItemNameIS(new ItemStack(key.itemId, 1, key.damageValue));
        if (name == null) return "";
        return StringTranslate.getInstance().translateNamedKey(name);
    }

    @Override
    public void updateCraftingResults() {
        super.updateCraftingResults();
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
     */
    public void handleGridClick(BE_ItemKey key, int button, boolean shiftHeld, EntityPlayer player) {
        if (mod_BetaEnergistics.isMultiplayer()) {
            BE_PacketHandler.sendToServer(
                BE_PacketHandler.buildGridClick(grid, key, button, shiftHeld));
            return;
        }
        ItemStack held = player.inventory.getItemStack();

        if (key == null && held != null) {
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
            int maxExtract = (button == 1) ? 1 : 64;
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
            BE_ItemKey heldKey = new BE_ItemKey(held.itemID, held.getItemDamage());
            if (heldKey.equals(key)) {
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
    public ItemStack getStackInSlot(int slotIndex) {
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

    /**
     * Receive network item data from a server packet (multiplayer client-side).
     */
    public void receiveNetworkItems(int[] data, int offset, int numEntries) {
        cachedItems.clear();
        for (int i = 0; i < numEntries; i++) {
            int idx = offset + i * 3;
            if (idx + 2 >= data.length) break;
            BE_ItemKey key = new BE_ItemKey(data[idx], data[idx + 1]);
            int count = data[idx + 2];
            cachedItems.add(new BE_GridEntry(key, count));
        }
        applySortOrder();
    }

    /** Apply the current sort mode to cached items. */
    private void applySortOrder() {
        switch (sortMode) {
            case SORT_BY_NAME:
                Collections.sort(cachedItems, new Comparator<BE_GridEntry>() {
                    public int compare(BE_GridEntry a, BE_GridEntry b) {
                        String nameA = getItemName(a.key);
                        String nameB = getItemName(b.key);
                        int cmp = nameA.compareToIgnoreCase(nameB);
                        return cmp != 0 ? cmp : a.key.itemId - b.key.itemId;
                    }
                });
                break;
            case SORT_BY_QUANTITY:
                Collections.sort(cachedItems, new Comparator<BE_GridEntry>() {
                    public int compare(BE_GridEntry a, BE_GridEntry b) {
                        int cmp = b.count - a.count;
                        return cmp != 0 ? cmp : a.key.itemId - b.key.itemId;
                    }
                });
                break;
            default:
                Collections.sort(cachedItems, new Comparator<BE_GridEntry>() {
                    public int compare(BE_GridEntry a, BE_GridEntry b) {
                        int cmp = a.key.itemId - b.key.itemId;
                        return cmp != 0 ? cmp : a.key.damageValue - b.key.damageValue;
                    }
                });
                break;
        }
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
