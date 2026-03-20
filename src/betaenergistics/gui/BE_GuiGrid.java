package betaenergistics.gui;

import betaenergistics.container.BE_ContainerGrid;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Grid Terminal GUI — scrollable grid of network items.
 * 9 columns x 6 rows visible = 54 items per page.
 * Search bar at top, player inventory at bottom.
 */
public class BE_GuiGrid extends GuiContainer {
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 6;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 18;
    private static final int CELL_SIZE = 18;
    private static final RenderItem itemRenderer = new RenderItem();

    private BE_ContainerGrid containerGrid;
    private BE_TileGrid tileGrid;
    private int scrollOffset = 0;
    private String searchText = "";
    private boolean searchFocused = false;

    public BE_GuiGrid(InventoryPlayer playerInv, BE_TileGrid grid) {
        super(new BE_ContainerGrid(playerInv, grid));
        this.containerGrid = (BE_ContainerGrid) this.inventorySlots;
        this.tileGrid = grid;
        this.xSize = 176;
        this.ySize = 222;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Grid Terminal", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);

        // Draw search box text
        int searchX = 80;
        int searchY = 6;
        String displayText = searchFocused ? searchText + "_" : (searchText.isEmpty() ? "Search..." : searchText);
        int textColor = searchFocused ? 0xFFFFFF : 0x808080;
        this.fontRenderer.drawString(displayText, searchX, searchY, textColor);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        // Draw dark background
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw GUI background (simple dark rect for now, TODO: proper texture)
        drawRect(x, y, x + this.xSize, y + this.ySize, 0xFFC6C6C6); // light gray
        drawRect(x + 7, y + 17, x + 169, y + 125, 0xFF8B8B8B); // grid area darker
        drawRect(x + 7, y + 139, x + 169, y + 215, 0xFF8B8B8B); // inventory area

        // Draw grid cells
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        int startIndex = scrollOffset * GRID_COLS;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int cellX = x + GRID_X + col * CELL_SIZE;
                int cellY = y + GRID_Y + row * CELL_SIZE;

                // Draw slot background
                drawRect(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0xFF373737);
                drawRect(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, 0xFF8B8B8B);

                int index = startIndex + row * GRID_COLS + col;
                if (index < items.size()) {
                    BE_ContainerGrid.BE_GridEntry entry = items.get(index);
                    renderGridItem(entry, cellX + 1, cellY + 1);
                }
            }
        }

        // Draw scrollbar
        int scrollBarX = x + 170;
        int scrollBarY = y + 17;
        int scrollBarH = 108;
        drawRect(scrollBarX, scrollBarY, scrollBarX + 5, scrollBarY + scrollBarH, 0xFF373737);

        int totalRows = (items.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, totalRows - GRID_ROWS);
        if (maxScroll > 0) {
            int thumbH = Math.max(8, scrollBarH * GRID_ROWS / totalRows);
            int thumbY = scrollBarY + (scrollBarH - thumbH) * scrollOffset / maxScroll;
            drawRect(scrollBarX, thumbY, scrollBarX + 5, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    private void renderGridItem(BE_ContainerGrid.BE_GridEntry entry, int x, int y) {
        ItemStack stack = new ItemStack(entry.key.itemId, entry.count, entry.key.damageValue);
        if (stack.getItem() == null) return;

        // Render item icon
        GL11.glEnable(32826); // GL_RESCALE_NORMAL
        RenderHelper.enableStandardItemLighting();
        itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, x, y);

        // Render count overlay
        String countText = formatCount(entry.count);
        GL11.glDisable(2896); // GL_LIGHTING
        GL11.glDisable(2929); // GL_DEPTH_TEST
        int textWidth = this.fontRenderer.getStringWidth(countText);
        this.fontRenderer.drawStringWithShadow(countText, x + 17 - textWidth, y + 9, 0xFFFFFF);
        GL11.glEnable(2896);
        GL11.glEnable(2929);
        RenderHelper.disableStandardItemLighting();
    }

    private String formatCount(int count) {
        if (count >= 1000000) return (count / 1000000) + "M";
        if (count >= 10000) return (count / 1000) + "K";
        if (count >= 1000) return String.format("%.1fK", count / 1000.0);
        return String.valueOf(count);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // Check search box click
        int searchBoxX = guiLeft + 78;
        int searchBoxY = guiTop + 4;
        if (mouseX >= searchBoxX && mouseX < searchBoxX + 90 && mouseY >= searchBoxY && mouseY < searchBoxY + 12) {
            searchFocused = true;
            return;
        } else {
            searchFocused = false;
        }

        // Check grid cell click
        int gridLeft = guiLeft + GRID_X;
        int gridTop = guiTop + GRID_Y;
        int gridRight = gridLeft + GRID_COLS * CELL_SIZE;
        int gridBottom = gridTop + GRID_ROWS * CELL_SIZE;

        if (mouseX >= gridLeft && mouseX < gridRight && mouseY >= gridTop && mouseY < gridBottom) {
            int col = (mouseX - gridLeft) / CELL_SIZE;
            int row = (mouseY - gridTop) / CELL_SIZE;
            int index = (scrollOffset + row) * GRID_COLS + col;

            List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
            BE_ItemKey key = null;
            if (index >= 0 && index < items.size()) {
                key = items.get(index).key;
            }

            boolean shiftHeld = isShiftKeyDown();
            containerGrid.handleGridClick(key, button, shiftHeld, this.mc.thePlayer);
            containerGrid.refreshItems();
            return;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (searchFocused) {
            if (keyCode == 14 && searchText.length() > 0) { // Backspace
                searchText = searchText.substring(0, searchText.length() - 1);
                scrollOffset = 0;
                return;
            } else if (keyCode == 1) { // Escape
                searchFocused = false;
                return;
            } else if (keyCode == 28) { // Enter
                searchFocused = false;
                return;
            } else if (c >= ' ' && c < 127) {
                searchText += c;
                scrollOffset = 0;
                return;
            }
        }
        super.keyTyped(c, keyCode);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0) {
            List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
            int totalRows = (items.size() + GRID_COLS - 1) / GRID_COLS;
            int maxScroll = Math.max(0, totalRows - GRID_ROWS);
            if (scroll < 0) {
                scrollOffset = Math.min(scrollOffset + 1, maxScroll);
            } else {
                scrollOffset = Math.max(scrollOffset - 1, 0);
            }
        }
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

    private static boolean isShiftKeyDown() {
        return org.lwjgl.input.Keyboard.isKeyDown(42) || org.lwjgl.input.Keyboard.isKeyDown(54);
    }
}
