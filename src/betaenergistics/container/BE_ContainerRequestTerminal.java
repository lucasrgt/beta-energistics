package betaenergistics.container;

import betaenergistics.crafting.BE_CraftingPlan;
import betaenergistics.mod_BetaEnergistics;
import betaenergistics.network.BE_PacketHandler;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileAutocrafter;
import betaenergistics.tile.BE_TileRequestTerminal;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Container for the Request Terminal.
 * Two modes: BROWSE (shows craftable items) and PREVIEW (shows crafting plan).
 */
public class BE_ContainerRequestTerminal extends Container {
    public static final int MODE_BROWSE = 0;
    public static final int MODE_PREVIEW = 1;

    private BE_TileRequestTerminal tile;
    private List<CraftableEntry> craftableItems = new ArrayList<CraftableEntry>();
    private int mode = MODE_BROWSE;

    // Preview state
    private BE_ItemKey selectedItem;
    private int requestQuantity = 1;
    private BE_CraftingPlan currentPlan;
    private List<PlanEntry> planEntries = new ArrayList<PlanEntry>();

    public BE_ContainerRequestTerminal(InventoryPlayer playerInv, BE_TileRequestTerminal tile) {
        this.tile = tile;

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

        refreshCraftableItems();
    }

    /** Refresh the list of craftable items from the network. */
    public void refreshCraftableItems() {
        craftableItems.clear();
        Map<BE_ItemKey, List<BE_TileAutocrafter>> craftable = tile.getCraftableItems();
        if (craftable != null) {
            for (Map.Entry<BE_ItemKey, List<BE_TileAutocrafter>> entry : craftable.entrySet()) {
                craftableItems.add(new CraftableEntry(entry.getKey(), entry.getValue().size()));
            }
            Collections.sort(craftableItems, new Comparator<CraftableEntry>() {
                public int compare(CraftableEntry a, CraftableEntry b) {
                    int cmp = a.key.itemId - b.key.itemId;
                    return cmp != 0 ? cmp : a.key.damageValue - b.key.damageValue;
                }
            });
        }
    }

    // Action type constants for multiplayer packets
    public static final int ACTION_SELECT = 0;
    public static final int ACTION_CONFIRM = 1;
    public static final int ACTION_CANCEL = 2;
    public static final int ACTION_INC_QTY = 3;
    public static final int ACTION_DEC_QTY = 4;

    /** Send a request terminal action to server (multiplayer) or execute locally. */
    public void sendAction(int actionType, BE_ItemKey key, int quantity) {
        if (mod_BetaEnergistics.isMultiplayer()) {
            BE_PacketHandler.sendToServer(
                BE_PacketHandler.buildRequestTerminalAction(tile, actionType, key, quantity));
            return;
        }
        executeAction(actionType, key, quantity);
    }

    /** Execute a request terminal action locally. */
    public void executeAction(int actionType, BE_ItemKey key, int quantity) {
        switch (actionType) {
            case ACTION_SELECT: selectItem(key, quantity); break;
            case ACTION_CONFIRM: confirmCraft(); break;
            case ACTION_CANCEL: cancelPreview(); break;
            case ACTION_INC_QTY: incrementQuantity(); break;
            case ACTION_DEC_QTY: decrementQuantity(); break;
        }
    }

    /** Select a craftable item and calculate its crafting plan. */
    public void selectItem(BE_ItemKey key, int quantity) {
        this.selectedItem = key;
        this.requestQuantity = Math.max(1, quantity);
        recalculatePlan();
        this.mode = MODE_PREVIEW;
    }

    /** Recalculate the plan for the current selection. */
    public void recalculatePlan() {
        planEntries.clear();
        if (selectedItem == null) return;

        currentPlan = tile.calculatePlan(selectedItem, requestQuantity);
        if (currentPlan == null) return;

        // Build display list: items to take (green), items to craft (yellow), missing (red)
        for (Map.Entry<BE_ItemKey, Integer> entry : currentPlan.itemsToTake.entrySet()) {
            planEntries.add(new PlanEntry(entry.getKey(), entry.getValue(), PlanEntry.TYPE_TAKE));
        }
        for (Map.Entry<BE_ItemKey, Integer> entry : currentPlan.itemsToCraft.entrySet()) {
            planEntries.add(new PlanEntry(entry.getKey(), entry.getValue(), PlanEntry.TYPE_CRAFT));
        }
        for (Map.Entry<BE_ItemKey, Integer> entry : currentPlan.missing.entrySet()) {
            planEntries.add(new PlanEntry(entry.getKey(), entry.getValue(), PlanEntry.TYPE_MISSING));
        }
    }

    /** Confirm the craft request. */
    public boolean confirmCraft() {
        if (selectedItem == null || currentPlan == null) return false;
        if (!currentPlan.isComplete()) return false;
        boolean result = tile.requestCraft(selectedItem);
        if (result) {
            cancelPreview();
        }
        return result;
    }

    /** Cancel back to browse mode. */
    public void cancelPreview() {
        mode = MODE_BROWSE;
        selectedItem = null;
        currentPlan = null;
        planEntries.clear();
        requestQuantity = 1;
        refreshCraftableItems();
    }

    public void incrementQuantity() {
        requestQuantity = Math.min(requestQuantity + 1, 999);
        recalculatePlan();
    }

    public void decrementQuantity() {
        requestQuantity = Math.max(requestQuantity - 1, 1);
        recalculatePlan();
    }

    // Accessors
    public int getMode() { return mode; }
    public List<CraftableEntry> getCraftableItems() { return craftableItems; }
    public List<PlanEntry> getPlanEntries() { return planEntries; }
    public BE_ItemKey getSelectedItem() { return selectedItem; }
    public int getRequestQuantity() { return requestQuantity; }
    public BE_CraftingPlan getCurrentPlan() { return currentPlan; }
    public BE_TileRequestTerminal getTile() { return tile; }

    @Override
    public void updateCraftingResults() {
        super.updateCraftingResults();
        if (mode == MODE_BROWSE) {
            refreshCraftableItems();
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile
            && player.getDistanceSq(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5) <= 64.0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return null; // no shift-click into network
    }

    // ====== Multiplayer receive methods ======

    /**
     * Receive craftable items list from a server packet (multiplayer client-side).
     */
    public void receiveCraftableItems(int[] data, int offset, int numEntries) {
        craftableItems.clear();
        for (int i = 0; i < numEntries; i++) {
            int idx = offset + i * 3;
            if (idx + 2 >= data.length) break;
            BE_ItemKey key = new BE_ItemKey(data[idx], data[idx + 1]);
            int crafterCount = data[idx + 2];
            craftableItems.add(new CraftableEntry(key, crafterCount));
        }
    }

    /**
     * Receive plan entries from a server packet (multiplayer client-side).
     */
    public void receivePlanEntries(int[] data, int offset, int numEntries) {
        planEntries.clear();
        for (int i = 0; i < numEntries; i++) {
            int idx = offset + i * 4;
            if (idx + 3 >= data.length) break;
            BE_ItemKey key = new BE_ItemKey(data[idx], data[idx + 1]);
            int count = data[idx + 2];
            int type = data[idx + 3];
            planEntries.add(new PlanEntry(key, count, type));
        }
        mode = MODE_PREVIEW;
    }

    /** Entry for a craftable item in browse mode. */
    public static class CraftableEntry {
        public final BE_ItemKey key;
        public final int crafterCount;

        public CraftableEntry(BE_ItemKey key, int crafterCount) {
            this.key = key;
            this.crafterCount = crafterCount;
        }
    }

    /** Entry for the crafting plan display. */
    public static class PlanEntry {
        public static final int TYPE_TAKE = 0;    // from storage (green)
        public static final int TYPE_CRAFT = 1;   // needs crafting (yellow)
        public static final int TYPE_MISSING = 2; // missing (red)

        public final BE_ItemKey key;
        public final int count;
        public final int type;

        public PlanEntry(BE_ItemKey key, int count, int type) {
            this.key = key;
            this.count = count;
            this.type = type;
        }
    }
}
