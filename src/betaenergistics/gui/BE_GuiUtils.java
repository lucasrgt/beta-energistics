package betaenergistics.gui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.Gui;

/**
 * Shared GUI rendering utilities for all Beta Energistics GUIs.
 * Static methods for tabs, buttons, scrollbar, etc.
 */
public class BE_GuiUtils extends Gui {

    private static final int BK = 0xFF000000;
    private static final int WH = 0xFFFFFFFF;
    private static final int DK = 0xFF555555;
    private static final int BG_ACTIVE = 0xFFC6C6C6;
    private static final int BG_INACTIVE = 0xFFA0A0A0;

    /**
     * Draw a left-side tab with rounded corners (same style as Grid Terminal sort tabs).
     * The tab opens to the right (merges with panel when active).
     */
    public static void drawTabLeft(FontRenderer font, int tx, int ty, int tw, int th, String label, boolean active) {
        int BG = active ? BG_ACTIVE : BG_INACTIVE;
        int tr = tx + tw, tb = ty + th;

        // Top edge
        for (int px = tx + 3; px < tr; px++) drawRect(px, ty, px + 1, ty + 1, BK);
        // Row 1
        drawRect(tx + 2, ty + 1, tx + 3, ty + 2, BK);
        drawRect(tx + 3, ty + 1, tr, ty + 2, WH);
        // Row 2
        drawRect(tx + 1, ty + 2, tx + 2, ty + 3, BK);
        drawRect(tx + 2, ty + 2, tr, ty + 3, WH);
        // Row 3
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

        // Active tab: paint over right edge to merge with panel
        if (active) {
            for (int py = ty + 3; py < tb - 2; py++) {
                drawRect(tr - 1, py, tr, py + 1, BG);
            }
        }

        // Label centered
        int labelW = font.getStringWidth(label);
        int labelX = tx + (tw - labelW) / 2;
        int labelY = ty + (th - 8) / 2 + 1;
        font.drawString(label, labelX, labelY, active ? 0x404040 : 0x606060);
    }

    /**
     * Draw a 3D beveled button (Minecraft style).
     * Returns true if the mouse is hovering over it.
     */
    public static boolean drawButton(FontRenderer font, int bx, int by, int bw, int bh,
                                      String label, int mouseX, int mouseY) {
        boolean hovered = (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh);
        int fill = hovered ? 0xFFBBBBBB : 0xFFAAAAAA;
        drawRect(bx, by, bx + bw, by + bh, fill);
        drawRect(bx, by, bx + bw, by + 1, WH);
        drawRect(bx, by, bx + 1, by + bh, WH);
        drawRect(bx + bw - 1, by, bx + bw, by + bh, DK);
        drawRect(bx, by + bh - 1, bx + bw, by + bh, DK);

        int textW = font.getStringWidth(label);
        font.drawString(label, bx + (bw - textW) / 2, by + (bh - 8) / 2 + 1, 0x404040);
        return hovered;
    }

    /**
     * Check if mouse is inside a rectangle.
     */
    public static boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
