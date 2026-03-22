package betaenergistics.gui;

import betaenergistics.container.BE_ContainerCraftingMonitor;
import betaenergistics.tile.BE_TileCraftingMonitor;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

/**
 * GUI for the Crafting Monitor.
 * Compact 176x166 panel (same height as a chest GUI).
 * Shows a scrollable list of craft jobs, each as a row with:
 *   [16x16 icon] [item name] [status text] [X cancel button]
 * Bottom: "Cancel All" button.
 */
public class BE_GuiCraftingMonitor extends GuiContainer {
    private static final RenderItem itemRenderer = new RenderItem();

    // Job list area
    private static final int LIST_X = 8;
    private static final int LIST_Y = 18;
    private static final int LIST_W = 152;
    private static final int ROW_H = 20;
    private static final int VISIBLE_ROWS = 5;

    // Cancel X per row
    private static final int XBTN = 7;

    // Cancel All button
    private static final int BTN_X = 56;
    private static final int BTN_Y = 122;
    private static final int BTN_W = 64;
    private static final int BTN_H = 14;

    private BE_TileCraftingMonitor monitor;
    private int scrollOffset = 0;
    private int screenMouseX, screenMouseY;

    public BE_GuiCraftingMonitor(InventoryPlayer playerInv, BE_TileCraftingMonitor monitor) {
        super(new BE_ContainerCraftingMonitor(playerInv, monitor));
        this.monitor = monitor;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.screenMouseX = mouseX;
        this.screenMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Crafting Monitor", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        List<BE_TileCraftingMonitor.CraftJob> jobs = monitor.getCraftJobs();

        if (jobs.isEmpty()) {
            this.fontRenderer.drawString("No active crafts", 42, 60, 0x808080);
        } else {
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int idx = scrollOffset + row;
                if (idx >= jobs.size()) break;
                BE_TileCraftingMonitor.CraftJob job = jobs.get(idx);
                int ry = LIST_Y + row * ROW_H;

                // Item name
                Item item = Item.itemsList[job.outputKey.itemId];
                String name = "???";
                if (item != null) {
                    name = ("" + StringTranslate.getInstance().translateNamedKey(
                        item.getItemNameIS(new ItemStack(job.outputKey.itemId, 1, job.outputKey.damageValue)))).trim();
                }
                int maxNameW = 80;
                String displayName = name;
                while (this.fontRenderer.getStringWidth(displayName) > maxNameW && displayName.length() > 4) {
                    displayName = displayName.substring(0, displayName.length() - 1);
                }
                if (!displayName.equals(name)) displayName += "..";
                this.fontRenderer.drawString(displayName, LIST_X + 20, ry + 2, 0xFFFFFF);

                // Status line
                String status;
                int color;
                if (job.active) {
                    status = "Crafting...";
                    color = 0x55FF55;
                } else {
                    status = "Queued: " + job.pending;
                    color = 0xFFFF55;
                }
                this.fontRenderer.drawString(status, LIST_X + 20, ry + 11, color);
            }
        }

        // Cancel All button
        if (!jobs.isEmpty()) {
            int relMX = screenMouseX - (width - xSize) / 2;
            int relMY = screenMouseY - (height - ySize) / 2;
            BE_GuiUtils.drawButton(this.fontRenderer, BTN_X, BTN_Y, BTN_W, BTN_H, "Cancel All", relMX, relMY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Main panel (Minecraft inventory style)
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);
        // Outer border
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);

        // Job list inset area
        int lx = x + LIST_X - 1;
        int ly = y + LIST_Y - 1;
        int lw = LIST_W + 2;
        int lh = VISIBLE_ROWS * ROW_H + 2;
        // Inset border (dark top-left, light bottom-right)
        drawRect(lx, ly, lx + lw, ly + 1, 0xFF373737);
        drawRect(lx, ly, lx + 1, ly + lh, 0xFF373737);
        drawRect(lx, ly + lh - 1, lx + lw, ly + lh, 0xFFFFFFFF);
        drawRect(lx + lw - 1, ly, lx + lw, ly + lh, 0xFFFFFFFF);
        // Inner fill
        drawRect(lx + 1, ly + 1, lx + lw - 1, ly + lh - 1, 0xFF8B8B8B);

        // Job rows
        List<BE_TileCraftingMonitor.CraftJob> jobs = monitor.getCraftJobs();
        if (!jobs.isEmpty()) {
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int idx = scrollOffset + row;
                if (idx >= jobs.size()) break;
                BE_TileCraftingMonitor.CraftJob job = jobs.get(idx);

                int rx = x + LIST_X;
                int ry = y + LIST_Y + row * ROW_H;

                // Row background (alternating subtle shade)
                int rowBg = (row % 2 == 0) ? 0xFF7A7A7A : 0xFF828282;
                drawRect(rx, ry, rx + LIST_W, ry + ROW_H, rowBg);

                // Hover highlight
                if (screenMouseX >= rx && screenMouseX < rx + LIST_W
                    && screenMouseY >= ry && screenMouseY < ry + ROW_H) {
                    drawRect(rx, ry, rx + LIST_W, ry + ROW_H, 0x20FFFFFF);
                }

                // Row separator line
                drawRect(rx, ry + ROW_H - 1, rx + LIST_W, ry + ROW_H, 0xFF666666);

                // Cancel X button (right side)
                int xbX = rx + LIST_W - XBTN - 4;
                int xbY = ry + (ROW_H - XBTN) / 2;
                boolean xHover = screenMouseX >= xbX && screenMouseX < xbX + XBTN
                              && screenMouseY >= xbY && screenMouseY < xbY + XBTN;

                drawRect(xbX, xbY, xbX + XBTN, xbY + XBTN, xHover ? 0xFFDD4444 : 0xFFAA3333);
                // X mark
                int xc = 0xFFFFFFFF;
                int mx = xbX + 1, my = xbY + 1;
                drawRect(mx, my, mx + 1, my + 1, xc);
                drawRect(mx + 4, my, mx + 5, my + 1, xc);
                drawRect(mx + 1, my + 1, mx + 2, my + 2, xc);
                drawRect(mx + 3, my + 1, mx + 4, my + 2, xc);
                drawRect(mx + 2, my + 2, mx + 3, my + 3, xc);
                drawRect(mx + 1, my + 3, mx + 2, my + 4, xc);
                drawRect(mx + 3, my + 3, mx + 4, my + 4, xc);
                drawRect(mx, my + 4, mx + 1, my + 5, xc);
                drawRect(mx + 4, my + 4, mx + 5, my + 5, xc);

                // Progress bar for active craft
                if (job.active) {
                    int barX = rx + 20;
                    int barY = ry + ROW_H - 4;
                    int barW = LIST_W - XBTN - 30;
                    drawRect(barX, barY, barX + barW, barY + 2, 0xFF444444);
                    int fillW = job.progress * barW / 100;
                    if (fillW > 0) drawRect(barX, barY, barX + fillW, barY + 2, 0xFF55FF55);
                }
            }

            // Render item icons
            GL11.glPushMatrix();
            GL11.glTranslatef((float) x, (float) y, 0.0F);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int idx = scrollOffset + row;
                if (idx >= jobs.size()) break;
                BE_TileCraftingMonitor.CraftJob job = jobs.get(idx);
                int ix = LIST_X + 2;
                int iy = LIST_Y + row * ROW_H + 2;
                ItemStack stack = new ItemStack(job.outputKey.itemId, 1, job.outputKey.damageValue);
                if (stack.getItem() != null) {
                    itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, ix, iy);
                }
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
        }

        // Scrollbar (right of list area)
        if (!jobs.isEmpty() && jobs.size() > VISIBLE_ROWS) {
            int sbX = x + LIST_X + LIST_W + 2;
            int sbY = y + LIST_Y;
            int sbW = 6;
            int sbH = VISIBLE_ROWS * ROW_H;
            // Track
            drawRect(sbX, sbY, sbX + sbW, sbY + sbH, 0xFF555555);
            // Thumb
            int maxScroll = jobs.size() - VISIBLE_ROWS;
            int thumbH = Math.max(8, sbH * VISIBLE_ROWS / jobs.size());
            int thumbY = sbY + (sbH - thumbH) * scrollOffset / maxScroll;
            drawRect(sbX, thumbY, sbX + sbW, thumbY + thumbH, 0xFFC6C6C6);
            drawRect(sbX, thumbY, sbX + sbW, thumbY + 1, 0xFFFFFFFF);
            drawRect(sbX, thumbY + thumbH - 1, sbX + sbW, thumbY + thumbH, 0xFF888888);
        }

        // Player inventory slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBg(x + 7 + col * 18, y + ySize - 83 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotBg(x + 7 + col * 18, y + ySize - 25);
        }
    }

    private void drawSlotBg(int sx, int sy) {
        drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFFFFFFFF);
        drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
        drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;

        List<BE_TileCraftingMonitor.CraftJob> jobs = monitor.getCraftJobs();

        // Per-row cancel X
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int idx = scrollOffset + row;
            if (idx >= jobs.size()) break;

            int ry = LIST_Y + row * ROW_H;
            int xbX = LIST_X + LIST_W - XBTN - 4;
            int xbY = ry + (ROW_H - XBTN) / 2;

            if (relX >= xbX && relX < xbX + XBTN && relY >= xbY && relY < xbY + XBTN) {
                jobs.get(idx).crafter.cancelSlotCrafts(jobs.get(idx).slotIndex);
                return;
            }
        }

        // Cancel All button
        if (!jobs.isEmpty() && relX >= BTN_X && relX < BTN_X + BTN_W && relY >= BTN_Y && relY < BTN_Y + BTN_H) {
            monitor.cancelAllCrafts();
            return;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0) {
            List<BE_TileCraftingMonitor.CraftJob> jobs = monitor.getCraftJobs();
            int maxScroll = Math.max(0, jobs.size() - VISIBLE_ROWS);
            if (scroll < 0) scrollOffset = Math.min(scrollOffset + 1, maxScroll);
            else scrollOffset = Math.max(scrollOffset - 1, 0);
        }
    }
}
