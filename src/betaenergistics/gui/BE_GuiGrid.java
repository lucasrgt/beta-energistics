package betaenergistics.gui;

import betaenergistics.container.BE_ContainerGrid;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Grid Terminal GUI — scrollable grid of network items.
 * Extends BE_GuiTerminalBase for shared layout/input handling.
 */
public class BE_GuiGrid extends BE_GuiTerminalBase {
    private static final String TEXTURE = "/gui/be_grid_terminal.png";
    private static final String[] TAB_LABELS = {"ID", "A-Z", "Qty"};
    private static final RenderItem gridItemRenderer = new RenderItem();

    private BE_ContainerGrid containerGrid;
    private BE_TileGrid tileGrid;

    public BE_GuiGrid(InventoryPlayer playerInv, BE_TileGrid grid) {
        super(new BE_ContainerGrid(playerInv, grid));
        this.containerGrid = (BE_ContainerGrid) this.inventorySlots;
        this.tileGrid = grid;
    }

    // ====== Abstract implementations ======

    @Override
    protected String getTerminalTitle() { return "Grid Terminal"; }

    @Override
    protected String getTexturePath() { return TEXTURE; }

    @Override
    protected boolean hasSortTabs() { return true; }

    @Override
    protected String[] getSortTabLabels() { return TAB_LABELS; }

    @Override
    protected int getActiveSortTab() { return containerGrid.getSortMode(); }

    @Override
    protected void onSortTabClicked(int tabIndex) {
        containerGrid.setSortMode(tabIndex);
        containerGrid.refreshItems();
    }

    @Override
    protected boolean needsItemLighting() { return true; }

    @Override
    protected void refreshData() {
        containerGrid.refreshItems();
    }

    @Override
    protected int getEntryCount() {
        return getFilteredItems().size();
    }

    @Override
    protected void renderEntryAt(int x, int y, int index) {
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        if (index >= items.size()) return;
        BE_ContainerGrid.BE_GridEntry entry = items.get(index);
        ItemStack stack = new ItemStack(entry.key.itemId, entry.count, entry.key.damageValue);
        if (stack.getItem() != null) {
            gridItemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, x, y);
        }
    }

    @Override
    protected void renderEntryAmount(int index, int x, int y) {
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        if (index >= items.size()) return;
        int count = items.get(index).count;
        if (count <= 1) return;
        renderScaledAmount(formatItemCount(count), x, y);
    }

    @Override
    protected String getEntryTooltip(int index) {
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        if (index >= items.size()) return null;
        BE_ContainerGrid.BE_GridEntry entry = items.get(index);
        Item item = Item.itemsList[entry.key.itemId];
        if (item == null) return null;
        String name = ("" + StringTranslate.getInstance().translateNamedKey(
            item.getItemNameIS(new ItemStack(entry.key.itemId, 1, entry.key.damageValue)))).trim();
        return name.length() > 0 ? name : null;
    }

    @Override
    protected void handleGridClick(int index, int mouseButton) {
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        BE_ItemKey key = null;
        if (index >= 0 && index < items.size()) {
            key = items.get(index).key;
        }
        boolean shiftHeld = isShiftKeyDown();
        containerGrid.handleGridClick(key, mouseButton, shiftHeld, this.mc.thePlayer);
        containerGrid.refreshItems();
    }

    @Override
    protected void drawScrollbarThumb(int trackX, int thumbY, int trackW, int thumbH) {
        // Pixel-perfect Tessellator rendering (avoids drawRect alpha blending issues)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        Tessellator t = Tessellator.instance;
        // Body (C6C6C6)
        fillSolid(t, trackX + 1, thumbY + 1, trackX + trackW - 1, thumbY + thumbH - 1, 198, 198, 198);
        // Top highlight (white)
        fillSolid(t, trackX, thumbY, trackX + trackW - 1, thumbY + 1, 255, 255, 255);
        // Left highlight (white)
        fillSolid(t, trackX, thumbY, trackX + 1, thumbY + thumbH - 1, 255, 255, 255);
        // Bottom shadow (dark)
        fillSolid(t, trackX + 1, thumbY + thumbH - 1, trackX + trackW, thumbY + thumbH, 85, 85, 85);
        // Right shadow (dark)
        fillSolid(t, trackX + trackW - 1, thumbY + 1, trackX + trackW, thumbY + thumbH, 85, 85, 85);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /** Draw a solid rect without alpha blending */
    private void fillSolid(Tessellator t, int x1, int y1, int x2, int y2, int r, int g, int b) {
        t.startDrawingQuads();
        t.setColorOpaque(r, g, b);
        t.addVertex(x1, y2, 0.0);
        t.addVertex(x2, y2, 0.0);
        t.addVertex(x2, y1, 0.0);
        t.addVertex(x1, y1, 0.0);
        t.draw();
    }

    private List<BE_ContainerGrid.BE_GridEntry> getFilteredItems() {
        if (searchText.isEmpty()) return containerGrid.getItems();
        List<BE_ContainerGrid.BE_GridEntry> filtered = new java.util.ArrayList<BE_ContainerGrid.BE_GridEntry>();
        String query = searchText.toLowerCase();
        for (BE_ContainerGrid.BE_GridEntry entry : containerGrid.getItems()) {
            Item item = Item.itemsList[entry.key.itemId];
            if (item != null) {
                String name = item.getItemName();
                if (name != null && name.toLowerCase().contains(query)) {
                    filtered.add(entry);
                }
            }
        }
        return filtered;
    }
}
