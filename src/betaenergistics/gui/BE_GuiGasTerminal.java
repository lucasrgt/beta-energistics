package betaenergistics.gui;

import betaenergistics.container.BE_ContainerGasTerminal;
import betaenergistics.storage.BE_GasKey;
import betaenergistics.tile.BE_TileGasTerminal;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Gas Terminal GUI — same layout as Grid/Fluid Terminal but for gases.
 * Each slot shows a colored gas icon with amount.
 */
public class BE_GuiGasTerminal extends GuiContainer {
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 7;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 19;
    private static final int CELL_SIZE = 18;
    private static final String TEXTURE = "/gui/be_grid_terminal.png";

    private BE_ContainerGasTerminal containerGas;
    private BE_TileGasTerminal tileTerminal;
    private int scrollOffset = 0;
    private String searchText = "";
    private boolean searchFocused = false;

    public BE_GuiGasTerminal(InventoryPlayer playerInv, BE_TileGasTerminal terminal) {
        super(new BE_ContainerGasTerminal(playerInv, terminal));
        this.containerGas = (BE_ContainerGasTerminal) this.inventorySlots;
        this.tileTerminal = terminal;
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
        this.fontRenderer.drawString("Gas Terminal", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        String displayText = searchFocused ? searchText + "_" : (searchText.isEmpty() ? "Search..." : searchText);
        this.fontRenderer.drawString(displayText, 100, 6, searchFocused ? 0xFFFFFF : 0xA0A0A0);

        // Tooltip
        containerGas.refreshGases();
        List<BE_ContainerGasTerminal.BE_GasEntry> gases = containerGas.getGases();

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
                if (index >= 0 && index < gases.size()) {
                    BE_ContainerGasTerminal.BE_GasEntry entry = gases.get(index);
                    String name = entry.key.getName() + " - " + formatAmount(entry.amountMB);
                    int tx = relX + 12;
                    int ty = relY - 12;
                    int tw = this.fontRenderer.getStringWidth(name);
                    this.drawGradientRect(tx - 3, ty - 3, tx + tw + 3, ty + 8 + 3, -1073741824, -1073741824);
                    this.fontRenderer.drawStringWithShadow(name, tx, ty, -1);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int texId = this.mc.renderEngine.getTexture(TEXTURE);
        this.mc.renderEngine.bindTexture(texId);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Scrollbar tab
        drawScrollbarTab(x + 172, y + 14, 22, 134);

        // Render gas icons
        containerGas.refreshGases();
        List<BE_ContainerGasTerminal.BE_GasEntry> gases = containerGas.getGases();

        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, 0.0F);

        int startIndex = scrollOffset * GRID_COLS;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = startIndex + row * GRID_COLS + col;
                if (index < gases.size()) {
                    BE_ContainerGasTerminal.BE_GasEntry entry = gases.get(index);
                    int ix = GRID_X + col * CELL_SIZE;
                    int iy = GRID_Y + row * CELL_SIZE;

                    // Gas rendered as colored square with gradient (gas-like appearance)
                    int color = entry.key.getColor();
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;
                    int colorTop = (0xCC << 24) | (Math.min(255, r + 40) << 16) | (Math.min(255, g + 40) << 8) | Math.min(255, b + 40);
                    int colorBot = (0x88 << 24) | (r << 16) | (g << 8) | b;
                    drawGradientRect(ix + 1, iy + 1, ix + 15, iy + 15, colorTop, colorBot);

                    // Amount text
                    renderGasAmount(entry.amountMB, ix, iy);
                }
            }
        }

        // Hover
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

        // Scrollbar thumb
        int totalRows = (gases.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, totalRows - GRID_ROWS);
        if (maxScroll > 0) {
            int trackX = x + 172 + 3, trackY = y + 14 + 4;
            int trackW = 22 - 7, trackH = 134 - 8;
            int thumbH = Math.max(10, trackH * GRID_ROWS / totalRows);
            int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;
            drawRect(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xFFC6C6C6);
            drawRect(trackX, thumbY, trackX + trackW, thumbY + 1, 0xFFFFFFFF);
            drawRect(trackX, thumbY, trackX + 1, thumbY + thumbH, 0xFFFFFFFF);
            drawRect(trackX, thumbY + thumbH - 1, trackX + trackW, thumbY + thumbH, 0xFF555555);
            drawRect(trackX + trackW - 1, thumbY, trackX + trackW, thumbY + thumbH, 0xFF555555);
        }
    }

    private void renderGasAmount(int mB, int x, int y) {
        if (mB <= 0) return;
        String text = formatAmount(mB);
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

    private String formatAmount(int mB) {
        if (mB >= 1000000) return (mB / 1000000) + "MB";
        if (mB >= 1000) {
            int b = mB / 1000;
            int frac = (mB % 1000) / 100;
            if (frac > 0 && b < 100) return b + "." + frac + "B";
            return b + "B";
        }
        return mB + "mB";
    }

    private void drawScrollbarTab(int tx, int ty, int tw, int th) {
        int BK = 0xFF000000, WH = 0xFFFFFFFF, BG = 0xFFC6C6C6, DK = 0xFF555555;
        int tr = tx + tw - 1, tb = ty + th - 1;
        for (int px = tx + 2; px <= tr - 3; px++) drawRect(px, ty, px + 1, ty + 1, BK);
        drawRect(tr - 2, ty + 1, tr - 1, ty + 2, BK);
        drawRect(tx + 1, ty + 1, tr - 2, ty + 2, WH);
        drawRect(tx + 1, ty + 2, tr - 2, ty + 3, WH);
        drawRect(tr - 2, ty + 2, tr - 1, ty + 3, BG);
        drawRect(tr - 1, ty + 2, tr, ty + 3, BK);
        drawRect(tx, ty + 3, tr - 2, ty + 4, BG);
        drawRect(tr - 2, ty + 3, tr, ty + 4, DK);
        drawRect(tr, ty + 3, tr + 1, ty + 4, BK);
        for (int py = ty + 4; py <= tb - 4; py++) {
            drawRect(tx, py, tr - 2, py + 1, BG);
            drawRect(tr - 2, py, tr, py + 1, DK);
            drawRect(tr, py, tr + 1, py + 1, BK);
        }
        drawRect(tx, tb - 3, tr - 3, tb - 2, BG);
        drawRect(tr - 3, tb - 3, tr, tb - 2, DK);
        drawRect(tr, tb - 3, tr + 1, tb - 2, BK);
        for (int px = tx + 1; px <= tr - 1; px++) drawRect(px, tb - 2, px + 1, tb - 1, DK);
        drawRect(tr, tb - 2, tr + 1, tb - 1, BK);
        for (int px = tx + 2; px <= tr - 2; px++) drawRect(px, tb - 1, px + 1, tb, DK);
        drawRect(tr - 1, tb - 1, tr, tb, BK);
        for (int px = tx + 3; px <= tr - 2; px++) drawRect(px, tb, px + 1, tb + 1, BK);
        int itx = tx + 3, ity = ty + 4, itw = tw - 7, ith = th - 8;
        drawRect(itx, ity, itx + itw - 1, ity + 1, 0xFF373737);
        drawRect(itx, ity, itx + 1, ity + ith - 1, 0xFF373737);
        drawRect(itx, ity + ith - 1, itx + itw, ity + ith, WH);
        drawRect(itx + itw - 1, ity, itx + itw, ity + ith, WH);
        drawRect(itx + 1, ity + 1, itx + itw - 1, ity + ith - 1, 0xFF8B8B8B);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int searchBoxX = guiLeft + 97;
        int searchBoxY = guiTop + 4;
        if (mouseX >= searchBoxX && mouseX < searchBoxX + 72 && mouseY >= searchBoxY && mouseY < searchBoxY + 12) {
            searchFocused = true;
            return;
        } else {
            searchFocused = false;
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
            } else if (keyCode == 1) { searchFocused = false; return; }
            else if (keyCode == 28) { searchFocused = false; return; }
            else if (c >= ' ' && c < 127) { searchText += c; scrollOffset = 0; return; }
        }
        super.keyTyped(c, keyCode);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0) {
            List<BE_ContainerGasTerminal.BE_GasEntry> gases = containerGas.getGases();
            int totalRows = (gases.size() + GRID_COLS - 1) / GRID_COLS;
            int maxScroll = Math.max(0, totalRows - GRID_ROWS);
            if (scroll < 0) scrollOffset = Math.min(scrollOffset + 1, maxScroll);
            else scrollOffset = Math.max(scrollOffset - 1, 0);
        }
    }
}
