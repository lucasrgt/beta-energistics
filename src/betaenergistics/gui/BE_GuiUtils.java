package betaenergistics.gui;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.Gui;

/**
 * Shared GUI rendering utilities for all Beta Energistics GUIs.
 * Static methods for tabs, buttons, scrollbar, etc.
 */
public class BE_GuiUtils extends Gui {

    private static final BE_GuiUtils INSTANCE = new BE_GuiUtils();
    private static final int BK = 0xFF000000;
    private static final int WH = 0xFFFFFFFF;
    private static final int DK = 0xFF555555;
    private static final int BG_ACTIVE = 0xFFC6C6C6;
    private static final int BG_INACTIVE = 0xFFA0A0A0;

    private static void rect(int x1, int y1, int x2, int y2, int color) {
        INSTANCE.drawRect(x1, y1, x2, y2, color);
    }

    /**
     * Draw a left-side tab with text label.
     */
    public static void drawTabLeft(FontRenderer font, int tx, int ty, int tw, int th, String label, boolean active) {
        drawTabLeft(tx, ty, tw, th, active);
        int labelW = font.getStringWidth(label);
        int labelX = tx + (tw - labelW) / 2;
        int labelY = ty + (th - 8) / 2 + 1;
        font.drawString(label, labelX, labelY, active ? 0x404040 : 0x606060);
    }

    /**
     * Draw a left-side tab with rounded corners (no content).
     * The tab opens to the right (merges with panel when active).
     * Content (text or icon) must be drawn by the caller after this.
     */
    public static void drawTabLeft(int tx, int ty, int tw, int th, boolean active) {
        int BG = active ? BG_ACTIVE : BG_INACTIVE;
        int re = tx + tw - 1; // right edge (1px border when inactive)
        int tb = ty + th;

        // Top edge (black)
        for (int px = tx + 2; px <= re; px++) rect(px, ty, px + 1, ty + 1, BK);
        // Row 1: corner + highlight
        rect(tx + 1, ty + 1, tx + 2, ty + 2, BK);
        rect(tx + 2, ty + 1, re, ty + 2, WH);
        // Row 2: corner + highlight + body start
        rect(tx, ty + 2, tx + 1, ty + 3, BK);
        rect(tx + 1, ty + 2, tx + 2, ty + 3, WH);
        rect(tx + 2, ty + 2, re, ty + 3, BG);
        // Body rows
        for (int py = ty + 3; py < tb - 2; py++) {
            rect(tx, py, tx + 1, py + 1, BK);
            rect(tx + 1, py, tx + 2, py + 1, WH);
            rect(tx + 2, py, re, py + 1, BG);
        }
        // Bottom -2: border + body
        rect(tx, tb - 2, tx + 1, tb - 1, BK);
        rect(tx + 1, tb - 2, re, tb - 1, BG);
        // Bottom -1: corner + shadow
        rect(tx + 1, tb - 1, tx + 2, tb, BK);
        rect(tx + 2, tb - 1, re, tb, DK);
        // Bottom edge (black)
        for (int px = tx + 2; px <= re; px++) rect(px, tb, px + 1, tb + 1, BK);

        // Right edge: panel BG when active (merge), black when inactive
        if (active) {
            for (int py = ty + 1; py < tb; py++) {
                rect(re, py, re + 1, py + 1, BG);
            }
        } else {
            for (int py = ty; py <= tb; py++) {
                rect(re, py, re + 1, py + 1, BK);
            }
        }
    }

    /**
     * Draw a 3D beveled button (Minecraft style).
     * Returns true if the mouse is hovering over it.
     */
    public static boolean drawButton(FontRenderer font, int bx, int by, int bw, int bh,
                                      String label, int mouseX, int mouseY) {
        boolean hovered = (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh);

        // Outer border (black frame)
        rect(bx, by, bx + bw, by + 1, BK);
        rect(bx, by + bh - 1, bx + bw, by + bh, BK);
        rect(bx, by, bx + 1, by + bh, BK);
        rect(bx + bw - 1, by, bx + bw, by + bh, BK);

        // Fill
        int fill = hovered ? 0xFF7B7B7B : 0xFF6C6C6C;
        rect(bx + 1, by + 1, bx + bw - 1, by + bh - 1, fill);

        // Top highlight
        rect(bx + 1, by + 1, bx + bw - 1, by + 2, hovered ? 0xFFAAAAAA : 0xFF9A9A9A);
        // Left highlight
        rect(bx + 1, by + 1, bx + 2, by + bh - 1, hovered ? 0xFFAAAAAA : 0xFF9A9A9A);
        // Bottom shadow
        rect(bx + 1, by + bh - 2, bx + bw - 1, by + bh - 1, DK);
        // Right shadow
        rect(bx + bw - 2, by + 1, bx + bw - 1, by + bh - 1, DK);

        // Label centered (white with shadow for contrast)
        int textW = font.getStringWidth(label);
        int tx = bx + (bw - textW) / 2;
        int ty = by + (bh - 8) / 2;
        font.drawString(label, tx + 1, ty + 1, 0xFF282828);
        font.drawString(label, tx, ty, hovered ? 0xFFFFFFA0 : 0xFFE0E0E0);
        return hovered;
    }

    /**
     * Check if mouse is inside a rectangle.
     */
    public static boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
