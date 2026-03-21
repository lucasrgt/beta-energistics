package betaenergistics.gui;

import betaenergistics.container.BE_ContainerGrid;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

/**
 * Grid Terminal GUI — scrollable grid of network items.
 * Sort tabs rendered as external tabs on the left side (like RetroNism config tabs).
 * Scrollbar tab rendered on the right side outside panel bounds.
 */
public class BE_GuiGrid extends GuiContainer {
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 7;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 19;
    private static final int CELL_SIZE = 18;
    private static final String TEXTURE = "/gui/be_grid_terminal.png";
    private static final RenderItem gridItemRenderer = new RenderItem();

    // Sort tab dimensions (external, left side)
    private static final int TAB_W = 26;
    private static final int TAB_H = 16;
    private static final int TAB_GAP = 2;
    private static final String[] TAB_LABELS = {"ID", "A-Z", "Qty"};

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
        this.ySize = 240;
    }

    private int screenMouseX, screenMouseY;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.screenMouseX = mouseX;
        this.screenMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        // Title
        this.fontRenderer.drawString("Grid Terminal", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        // Search box text
        String displayText = searchFocused ? searchText + "_" : (searchText.isEmpty() ? "Search..." : searchText);
        int textColor = searchFocused ? 0xFFFFFF : 0xA0A0A0;
        this.fontRenderer.drawString(displayText, 100, 6, textColor);

        // Tooltip for hovered grid item
        containerGrid.refreshItems();
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (this.mc.thePlayer.inventory.getItemStack() == null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int relX = this.screenMouseX - guiLeft;
            int relY = this.screenMouseY - guiTop;

            if (relX >= GRID_X && relX < GRID_X + GRID_COLS * CELL_SIZE
                && relY >= GRID_Y && relY < GRID_Y + GRID_ROWS * CELL_SIZE) {
                int col = (relX - GRID_X) / CELL_SIZE;
                int row = (relY - GRID_Y) / CELL_SIZE;
                int index = (scrollOffset + row) * GRID_COLS + col;
                if (index >= 0 && index < items.size()) {
                    BE_ContainerGrid.BE_GridEntry entry = items.get(index);
                    Item item = Item.itemsList[entry.key.itemId];
                    if (item != null) {
                        String name = ("" + StringTranslate.getInstance().translateNamedKey(
                            item.getItemNameIS(new ItemStack(entry.key.itemId, 1, entry.key.damageValue)))).trim();
                        if (name.length() > 0) {
                            int tx = relX + 12;
                            int ty = relY - 12;
                            int tw = this.fontRenderer.getStringWidth(name);
                            this.drawGradientRect(tx - 3, ty - 3, tx + tw + 3, ty + 8 + 3, -1073741824, -1073741824);
                            this.fontRenderer.drawStringWithShadow(name, tx, ty, -1);
                        }
                    }
                }
            }
        }
    }

    private void renderItemCount(int count, int x, int y) {
        if (count <= 1) return;
        String text = formatCount(count);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        float scale = 0.60F;
        int textWidth = this.fontRenderer.getStringWidth(text);
        int tx = (int)((x + 16 - textWidth * scale) / scale);
        int ty = (int)((y + 11) / scale);
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);
        this.fontRenderer.drawStringWithShadow(text, tx, ty, 0xFFFFFF);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private String formatCount(int count) {
        if (count >= 1000000000) return (count / 1000000000) + "G";
        if (count >= 1000000) {
            int m = count / 1000000;
            if (m >= 100) return m + "M";
            int frac = (count / 100000) % 10;
            return m + "." + frac + "M";
        }
        if (count >= 1000) {
            int k = count / 1000;
            if (k >= 100) return k + "K";
            int frac = (count / 100) % 10;
            return k + "." + frac + "K";
        }
        return String.valueOf(count);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int texId = this.mc.renderEngine.getTexture(TEXTURE);
        this.mc.renderEngine.bindTexture(texId);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw GUI background from texture
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Sort tabs (left side, outside panel)
        int activeSort = containerGrid.getSortMode();
        for (int i = 0; i < TAB_LABELS.length; i++) {
            int tabX = x - TAB_W;
            int tabY = y + 18 + i * (TAB_H + TAB_GAP);
            boolean active = (i == activeSort);
            drawSortTab(tabX, tabY, TAB_W, TAB_H, TAB_LABELS[i], active);
        }

        // Scrollbar tab (right) — rendered via drawRect
        drawScrollbarTab(x + 172, y + 14, 22, 134);

        // Render grid items (in background layer = before held item in z-order)
        containerGrid.refreshItems();
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)x, (float)y, 0.0F);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            List<BE_ContainerGrid.BE_GridEntry> bgItems = getFilteredItems();
            int startIndex = scrollOffset * GRID_COLS;
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    int index = startIndex + row * GRID_COLS + col;
                    if (index < bgItems.size()) {
                        BE_ContainerGrid.BE_GridEntry entry = bgItems.get(index);
                        int ix = GRID_X + col * CELL_SIZE;
                        int iy = GRID_Y + row * CELL_SIZE;
                        ItemStack stack = new ItemStack(entry.key.itemId, entry.count, entry.key.damageValue);
                        if (stack.getItem() != null) {
                            gridItemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, ix, iy);
                            renderItemCount(entry.count, ix, iy);
                        }
                    }
                }
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();

            // Hover highlight
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int relX = screenMouseX - x;
            int relY = screenMouseY - y;
            if (relX >= GRID_X && relX < GRID_X + GRID_COLS * CELL_SIZE
                && relY >= GRID_Y && relY < GRID_Y + GRID_ROWS * CELL_SIZE) {
                int hcol = (relX - GRID_X) / CELL_SIZE;
                int hrow = (relY - GRID_Y) / CELL_SIZE;
                int hx = GRID_X + hcol * CELL_SIZE;
                int hy = GRID_Y + hrow * CELL_SIZE;
                drawGradientRect(hx, hy, hx + 16, hy + 16, -2130706433, -2130706433);
            }
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            GL11.glPopMatrix();
        }

        // Scrollbar thumb
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        int totalRows = (items.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, totalRows - GRID_ROWS);
        if (maxScroll > 0) {
            int trackX = x + 172 + 3, trackY = y + 14 + 4;
            int trackW = 22 - 7, trackH = 134 - 8;
            int thumbH = Math.max(10, trackH * GRID_ROWS / totalRows);
            int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;
            // Use Tessellator directly for precise pixel rendering (drawRect uses alpha blending which causes issues)
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
    }

    /**
     * Draw a sort tab on the left side (scrollbar_tab_left style from furnace corners).
     * Left side has rounded corners, right side is open (merges with panel).
     */
    private void drawSortTab(int tx, int ty, int tw, int th, String label, boolean active) {
        int BK = 0xFF000000, WH = 0xFFFFFFFF, BG = active ? 0xFFC6C6C6 : 0xFFA0A0A0;
        int DK = 0xFF555555;
        int tr = tx + tw, tb = ty + th;

        // Top edge
        for (int px = tx + 3; px < tr; px++) drawRect(px, ty, px + 1, ty + 1, BK);
        // Row 1
        drawRect(tx + 2, ty + 1, tx + 3, ty + 2, BK);
        drawRect(tx + 3, ty + 1, tr, ty + 2, WH);
        // Row 2
        drawRect(tx + 1, ty + 2, tx + 2, ty + 3, BK);
        drawRect(tx + 2, ty + 2, tr, ty + 3, WH);
        // Row 3 (transition)
        drawRect(tx, ty + 3, tx + 1, ty + 4, BK);
        drawRect(tx + 1, ty + 3, tx + 2, ty + 4, WH);
        drawRect(tx + 2, ty + 3, tr, ty + 4, BG);
        // Body rows
        for (int py = ty + 4; py < tb - 3; py++) {
            drawRect(tx, py, tx + 1, py + 1, BK);
            drawRect(tx + 1, py, tx + 2, py + 1, WH);
            drawRect(tx + 2, py, tr, py + 1, BG);
        }
        // Bottom transition
        drawRect(tx, tb - 3, tx + 1, tb - 2, BK);
        drawRect(tx + 1, tb - 3, tr, tb - 2, BG);
        // Row B-2
        drawRect(tx + 1, tb - 2, tx + 2, tb - 1, BK);
        drawRect(tx + 2, tb - 2, tr, tb - 1, DK);
        // Row B-1
        drawRect(tx + 2, tb - 1, tx + 3, tb, BK);
        drawRect(tx + 3, tb - 1, tr, tb, DK);
        // Bottom edge
        for (int px = tx + 3; px < tr; px++) drawRect(px, tb, px + 1, tb + 1, BK);

        // Active tab: paint over right edge to merge with panel (BG color)
        if (active) {
            for (int py = ty + 3; py < tb - 2; py++) {
                drawRect(tr - 1, py, tr, py + 1, BG);
            }
        }

        // Label centered
        int labelW = this.fontRenderer.getStringWidth(label);
        int labelX = tx + (tw - labelW) / 2;
        int labelY = ty + (th - 8) / 2 + 1;
        this.fontRenderer.drawString(label, labelX, labelY, active ? 0x404040 : 0x606060);
    }

    /**
     * Draw scrollbar tab on the right side (external, outside panel bounds).
     */
    private void drawScrollbarTab(int tx, int ty, int tw, int th) {
        int BK = 0xFF000000, WH = 0xFFFFFFFF, BG = 0xFFC6C6C6, DK = 0xFF555555;
        int tr = tx + tw - 1, tb = ty + th - 1;

        // Top edge
        for (int px = tx + 2; px <= tr - 3; px++) drawRect(px, ty, px + 1, ty + 1, BK);
        // Row 1
        drawRect(tr - 2, ty + 1, tr - 1, ty + 2, BK);
        drawRect(tx + 1, ty + 1, tr - 2, ty + 2, WH);
        // Row 2
        drawRect(tx + 1, ty + 2, tr - 2, ty + 3, WH);
        drawRect(tr - 2, ty + 2, tr - 1, ty + 3, BG);
        drawRect(tr - 1, ty + 2, tr, ty + 3, BK);
        // Row 3
        drawRect(tx, ty + 3, tr - 2, ty + 4, BG);
        drawRect(tr - 2, ty + 3, tr, ty + 4, DK);
        drawRect(tr, ty + 3, tr + 1, ty + 4, BK);
        // Body
        for (int py = ty + 4; py <= tb - 4; py++) {
            drawRect(tx, py, tr - 2, py + 1, BG);
            drawRect(tr - 2, py, tr, py + 1, DK);
            drawRect(tr, py, tr + 1, py + 1, BK);
        }
        // Bottom transition
        drawRect(tx, tb - 3, tr - 3, tb - 2, BG);
        drawRect(tr - 3, tb - 3, tr, tb - 2, DK);
        drawRect(tr, tb - 3, tr + 1, tb - 2, BK);
        for (int px = tx + 1; px <= tr - 1; px++) drawRect(px, tb - 2, px + 1, tb - 1, DK);
        drawRect(tr, tb - 2, tr + 1, tb - 1, BK);
        for (int px = tx + 2; px <= tr - 2; px++) drawRect(px, tb - 1, px + 1, tb, DK);
        drawRect(tr - 1, tb - 1, tr, tb, BK);
        for (int px = tx + 3; px <= tr - 2; px++) drawRect(px, tb, px + 1, tb + 1, BK);

        // Inner track (inset slot-style)
        int itx = tx + 3, ity = ty + 4, itw = tw - 7, ith = th - 8;
        drawRect(itx, ity, itx + itw - 1, ity + 1, 0xFF373737);
        drawRect(itx, ity, itx + 1, ity + ith - 1, 0xFF373737);
        drawRect(itx, ity + ith - 1, itx + itw, ity + ith, WH);
        drawRect(itx + itw - 1, ity, itx + itw, ity + ith, WH);
        drawRect(itx + 1, ity + 1, itx + itw - 1, ity + ith - 1, 0xFF8B8B8B);
    }

    /** Draw a solid rect without alpha blending (avoids drawRect's GL_BLEND issues) */
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // Check sort tab clicks (left side, outside panel)
        for (int i = 0; i < TAB_LABELS.length; i++) {
            int tabX = guiLeft - TAB_W;
            int tabY = guiTop + 18 + i * (TAB_H + TAB_GAP);
            if (mouseX >= tabX && mouseX < tabX + TAB_W && mouseY >= tabY && mouseY < tabY + TAB_H) {
                containerGrid.setSortMode(i);
                containerGrid.refreshItems();
                return;
            }
        }

        // Check search box click (at x=97, y=4, w=72, h=12)
        int searchBoxX = guiLeft + 97;
        int searchBoxY = guiTop + 4;
        if (mouseX >= searchBoxX && mouseX < searchBoxX + 72 && mouseY >= searchBoxY && mouseY < searchBoxY + 12) {
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
            if (keyCode == 14 && searchText.length() > 0) {
                searchText = searchText.substring(0, searchText.length() - 1);
                scrollOffset = 0;
                return;
            } else if (keyCode == 1) {
                searchFocused = false;
                return;
            } else if (keyCode == 28) {
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

    private static boolean isShiftKeyDown() {
        return org.lwjgl.input.Keyboard.isKeyDown(42) || org.lwjgl.input.Keyboard.isKeyDown(54);
    }
}
