package betaenergistics.gui;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Abstract base for all terminal GUIs (Grid, Fluid, Gas).
 * Provides shared scrollable grid rendering, search box, scrollbar tab,
 * sort tabs, hover highlight, and mouse/keyboard handling.
 *
 * Subclasses only need to implement type-specific rendering and data access.
 */
public abstract class BE_GuiTerminalBase extends GuiContainer {
    protected static final int GRID_COLS = 9;
    protected static final int GRID_ROWS = 7;
    protected static final int GRID_X = 8;
    protected static final int GRID_Y = 19;
    protected static final int CELL_SIZE = 18;

    // Sort tab dimensions (external, left side)
    protected static final int TAB_W = 26;
    protected static final int TAB_H = 16;
    protected static final int TAB_GAP = 2;

    protected int scrollOffset = 0;
    protected String searchText = "";
    protected boolean searchFocused = false;
    protected int screenMouseX, screenMouseY;

    public BE_GuiTerminalBase(Container container) {
        super(container);
        this.xSize = 176;
        this.ySize = 240;
    }

    // ====== Abstract methods for subclasses ======

    /** Title displayed at top-left (e.g. "Grid Terminal", "Fluid Terminal") */
    protected abstract String getTerminalTitle();

    /** Total number of entries after filtering */
    protected abstract int getEntryCount();

    /** Refresh the data from the container/network */
    protected abstract void refreshData();

    /** Render an entry's icon at the given position (inside glTranslate context) */
    protected abstract void renderEntryAt(int x, int y, int index);

    /** Render the amount text overlay for an entry */
    protected abstract void renderEntryAmount(int index, int x, int y);

    /** Get tooltip text for a hovered entry, or null if none */
    protected abstract String getEntryTooltip(int index);

    /** Handle a click on a grid cell at the given index */
    protected abstract void handleGridClick(int index, int mouseButton);

    /** Get the texture path for the background */
    protected abstract String getTexturePath();

    /** Whether this terminal has sort tabs (Grid has them, Fluid/Gas don't) */
    protected boolean hasSortTabs() { return false; }

    /** Get sort tab labels (override if hasSortTabs returns true) */
    protected String[] getSortTabLabels() { return null; }

    /** Get active sort tab index */
    protected int getActiveSortTab() { return 0; }

    /** Handle sort tab click */
    protected void onSortTabClicked(int tabIndex) {}

    /** Whether this terminal needs RenderHelper lighting setup for grid items */
    protected boolean needsItemLighting() { return false; }

    // ====== Shared implementation ======

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.screenMouseX = mouseX;
        this.screenMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        // Title
        this.fontRenderer.drawString(getTerminalTitle(), 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        // Search box text
        String displayText = searchFocused ? searchText + "_" : (searchText.isEmpty() ? "Search..." : searchText);
        int textColor = searchFocused ? 0xFFFFFF : 0xA0A0A0;
        this.fontRenderer.drawString(displayText, 100, 6, textColor);

        // Tooltip for hovered entry
        refreshData();
        int entryCount = getEntryCount();

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
                if (index >= 0 && index < entryCount) {
                    String tooltip = getEntryTooltip(index);
                    if (tooltip != null && tooltip.length() > 0) {
                        int tx = relX + 12;
                        int ty = relY - 12;
                        int tw = this.fontRenderer.getStringWidth(tooltip);
                        this.drawGradientRect(tx - 3, ty - 3, tx + tw + 3, ty + 8 + 3, -1073741824, -1073741824);
                        this.fontRenderer.drawStringWithShadow(tooltip, tx, ty, -1);
                    }
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int texId = this.mc.renderEngine.getTexture(getTexturePath());
        this.mc.renderEngine.bindTexture(texId);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw GUI background from texture
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Sort tabs (left side, outside panel) — only if supported
        if (hasSortTabs()) {
            String[] tabLabels = getSortTabLabels();
            int activeSort = getActiveSortTab();
            for (int i = 0; i < tabLabels.length; i++) {
                int tabX = x - TAB_W;
                int tabY = y + 18 + i * (TAB_H + TAB_GAP);
                boolean active = (i == activeSort);
                drawSortTab(tabX, tabY, TAB_W, TAB_H, tabLabels[i], active);
            }
        }

        // Scrollbar tab (right)
        drawScrollbarTab(x + 172, y + 14, 22, 134);

        // Render grid entries
        refreshData();
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)x, (float)y, 0.0F);

            if (needsItemLighting()) {
                GL11.glPushMatrix();
                GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
                RenderHelper.enableStandardItemLighting();
                GL11.glPopMatrix();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            }

            int entryCount = getEntryCount();
            int startIndex = scrollOffset * GRID_COLS;
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    int index = startIndex + row * GRID_COLS + col;
                    if (index < entryCount) {
                        int ix = GRID_X + col * CELL_SIZE;
                        int iy = GRID_Y + row * CELL_SIZE;
                        renderEntryAt(ix, iy, index);
                        renderEntryAmount(index, ix, iy);
                    }
                }
            }

            if (needsItemLighting()) {
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.disableStandardItemLighting();
            }

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
        int entryCount = getEntryCount();
        int totalRows = (entryCount + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, totalRows - GRID_ROWS);
        if (maxScroll > 0) {
            int trackX = x + 172 + 3, trackY = y + 14 + 4;
            int trackW = 22 - 7, trackH = 134 - 8;
            int thumbH = Math.max(10, trackH * GRID_ROWS / totalRows);
            int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;
            drawScrollbarThumb(trackX, thumbY, trackW, thumbH);
        }
    }

    /**
     * Draw scrollbar thumb. Default uses drawRect (fluid/gas style).
     * Override for pixel-perfect Tessellator rendering (grid style).
     */
    protected void drawScrollbarThumb(int trackX, int thumbY, int trackW, int thumbH) {
        drawRect(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xFFC6C6C6);
        drawRect(trackX, thumbY, trackX + trackW, thumbY + 1, 0xFFFFFFFF);
        drawRect(trackX, thumbY, trackX + 1, thumbY + thumbH, 0xFFFFFFFF);
        drawRect(trackX, thumbY + thumbH - 1, trackX + trackW, thumbY + thumbH, 0xFF555555);
        drawRect(trackX + trackW - 1, thumbY, trackX + trackW, thumbY + thumbH, 0xFF555555);
    }

    /**
     * Render a scaled amount text at the bottom-right of a cell.
     * Shared by all terminal types.
     */
    protected void renderScaledAmount(String text, int x, int y) {
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

    /** Format item count: 1K, 1.5K, 1M, 1.5M, 1G */
    protected String formatItemCount(int count) {
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

    /** Format fluid/gas mB amount: 1mB, 1B, 1.5B, 1MB */
    protected String formatFluidAmount(int mB) {
        if (mB >= 1000000) return (mB / 1000000) + "MB";
        if (mB >= 1000) {
            int b = mB / 1000;
            int frac = (mB % 1000) / 100;
            if (frac > 0 && b < 100) return b + "." + frac + "B";
            return b + "B";
        }
        return mB + "mB";
    }

    /** Full mB format for tooltips */
    protected String formatFluidAmountFull(int mB) {
        return mB + " mB";
    }

    // ====== Sort tab rendering ======

    protected void drawSortTab(int tx, int ty, int tw, int th, String label, boolean active) {
        BE_GuiUtils.drawTabLeft(this.fontRenderer, tx, ty, tw, th, label, active);
    }

    // ====== Scrollbar tab rendering ======

    protected void drawScrollbarTab(int tx, int ty, int tw, int th) {
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

    // ====== Input handling ======

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // Check sort tab clicks (left side, outside panel)
        if (hasSortTabs()) {
            String[] tabLabels = getSortTabLabels();
            for (int i = 0; i < tabLabels.length; i++) {
                int tabX = guiLeft - TAB_W;
                int tabY = guiTop + 18 + i * (TAB_H + TAB_GAP);
                if (mouseX >= tabX && mouseX < tabX + TAB_W && mouseY >= tabY && mouseY < tabY + TAB_H) {
                    onSortTabClicked(i);
                    return;
                }
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
            handleGridClick(index, button);
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
            int entryCount = getEntryCount();
            int totalRows = (entryCount + GRID_COLS - 1) / GRID_COLS;
            int maxScroll = Math.max(0, totalRows - GRID_ROWS);
            if (scroll < 0) {
                scrollOffset = Math.min(scrollOffset + 1, maxScroll);
            } else {
                scrollOffset = Math.max(scrollOffset - 1, 0);
            }
        }
    }

    protected static boolean isShiftKeyDown() {
        return org.lwjgl.input.Keyboard.isKeyDown(42) || org.lwjgl.input.Keyboard.isKeyDown(54);
    }
}
