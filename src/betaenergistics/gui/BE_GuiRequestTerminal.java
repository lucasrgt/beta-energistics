package betaenergistics.gui;

import betaenergistics.container.BE_ContainerRequestTerminal;
import betaenergistics.crafting.BE_CraftingPlan;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileRequestTerminal;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

/**
 * Request Terminal GUI — two modes:
 * BROWSE: scrollable grid of craftable items (from Autocrafter patterns)
 * PREVIEW: shows crafting plan with items to take/craft/missing + confirm/cancel
 */
public class BE_GuiRequestTerminal extends GuiContainer {
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 7;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 19;
    private static final int CELL_SIZE = 18;
    private static final RenderItem gridItemRenderer = new RenderItem();

    // Preview mode layout
    private static final int PLAN_X = 8;
    private static final int PLAN_Y = 30;
    private static final int PLAN_ROW_H = 14;
    private static final int PLAN_ROWS = 8;

    // Buttons in preview mode
    private static final int BTN_CONFIRM_X = 8;
    private static final int BTN_CONFIRM_Y = 145;
    private static final int BTN_CANCEL_X = 92;
    private static final int BTN_CANCEL_Y = 145;
    private static final int BTN_W = 80;
    private static final int BTN_H = 12;

    // Quantity buttons
    private static final int QTY_MINUS_X = 120;
    private static final int QTY_PLUS_X = 152;
    private static final int QTY_Y = 6;
    private static final int QTY_BTN_W = 12;
    private static final int QTY_BTN_H = 10;

    private BE_ContainerRequestTerminal containerRT;
    private int scrollOffset = 0;
    private int planScrollOffset = 0;
    private int screenMouseX, screenMouseY;

    public BE_GuiRequestTerminal(InventoryPlayer playerInv, BE_TileRequestTerminal tile) {
        super(new BE_ContainerRequestTerminal(playerInv, tile));
        this.containerRT = (BE_ContainerRequestTerminal) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 240;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.screenMouseX = mouseX;
        this.screenMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        if (containerRT.getMode() == BE_ContainerRequestTerminal.MODE_BROWSE) {
            drawBrowseForeground();
        } else {
            drawPreviewForeground();
        }
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);
    }

    private void drawBrowseForeground() {
        this.fontRenderer.drawString("Request Terminal", 8, 6, 4210752);

        containerRT.refreshCraftableItems();
        List<BE_ContainerRequestTerminal.CraftableEntry> items = containerRT.getCraftableItems();

        // Tooltip for hovered craftable item
        if (this.mc.thePlayer.inventory.getItemStack() == null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int relX = screenMouseX - guiLeft;
            int relY = screenMouseY - guiTop;

            if (relX >= GRID_X && relX < GRID_X + GRID_COLS * CELL_SIZE
                && relY >= GRID_Y && relY < GRID_Y + GRID_ROWS * CELL_SIZE) {
                int col = (relX - GRID_X) / CELL_SIZE;
                int row = (relY - GRID_Y) / CELL_SIZE;
                int index = (scrollOffset + row) * GRID_COLS + col;
                if (index >= 0 && index < items.size()) {
                    BE_ContainerRequestTerminal.CraftableEntry entry = items.get(index);
                    Item item = Item.itemsList[entry.key.itemId];
                    if (item != null) {
                        String name = ("" + StringTranslate.getInstance().translateNamedKey(
                            item.getItemNameIS(new ItemStack(entry.key.itemId, 1, entry.key.damageValue)))).trim();
                        if (name.length() > 0) {
                            GL11.glDisable(GL11.GL_LIGHTING);
                            GL11.glDisable(GL11.GL_DEPTH_TEST);
                            int tx = relX + 12;
                            int ty = relY - 12;
                            int tw = this.fontRenderer.getStringWidth(name);
                            this.drawGradientRect(tx - 3, ty - 3, tx + tw + 3, ty + 8 + 3, -1073741824, -1073741824);
                            this.fontRenderer.drawStringWithShadow(name, tx, ty, -1);
                            GL11.glEnable(GL11.GL_LIGHTING);
                            GL11.glEnable(GL11.GL_DEPTH_TEST);
                        }
                    }
                }
            }
        }
    }

    private void drawPreviewForeground() {
        // Title: "Request: <item name> x<qty>"
        BE_ItemKey sel = containerRT.getSelectedItem();
        String title = "Request Terminal";
        if (sel != null) {
            Item item = Item.itemsList[sel.itemId];
            if (item != null) {
                String name = ("" + StringTranslate.getInstance().translateNamedKey(
                    item.getItemNameIS(new ItemStack(sel.itemId, 1, sel.damageValue)))).trim();
                title = "Request: " + name;
            }
        }
        this.fontRenderer.drawString(title, 8, 6, 4210752);

        // Quantity display
        String qtyStr = "x" + containerRT.getRequestQuantity();
        this.fontRenderer.drawString(qtyStr, 135, 7, 4210752);

        // Quantity buttons
        draw3DButton(QTY_MINUS_X, QTY_Y, QTY_BTN_W, QTY_BTN_H, "-");
        draw3DButton(QTY_PLUS_X, QTY_Y, QTY_BTN_W, QTY_BTN_H, "+");

        // Plan header
        this.fontRenderer.drawString("Crafting Plan:", 8, 20, 4210752);

        // Plan entries
        List<BE_ContainerRequestTerminal.PlanEntry> entries = containerRT.getPlanEntries();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int visibleStart = planScrollOffset;
        int visibleEnd = Math.min(entries.size(), planScrollOffset + PLAN_ROWS);
        for (int i = visibleStart; i < visibleEnd; i++) {
            BE_ContainerRequestTerminal.PlanEntry entry = entries.get(i);
            int py = PLAN_Y + (i - planScrollOffset) * PLAN_ROW_H;

            // Color based on type
            int color;
            String prefix;
            switch (entry.type) {
                case BE_ContainerRequestTerminal.PlanEntry.TYPE_TAKE:
                    color = 0x00AA00; // green
                    prefix = "[TAKE] ";
                    break;
                case BE_ContainerRequestTerminal.PlanEntry.TYPE_CRAFT:
                    color = 0xAAAA00; // yellow
                    prefix = "[CRAFT] ";
                    break;
                default:
                    color = 0xAA0000; // red
                    prefix = "[MISS] ";
                    break;
            }

            // Item name + count
            Item item = Item.itemsList[entry.key.itemId];
            String name = "???";
            if (item != null) {
                name = ("" + StringTranslate.getInstance().translateNamedKey(
                    item.getItemNameIS(new ItemStack(entry.key.itemId, 1, entry.key.damageValue)))).trim();
            }
            String line = prefix + name + " x" + entry.count;
            // Truncate if too long
            if (this.fontRenderer.getStringWidth(line) > 160) {
                while (this.fontRenderer.getStringWidth(line + "...") > 160 && line.length() > 10) {
                    line = line.substring(0, line.length() - 1);
                }
                line += "...";
            }
            this.fontRenderer.drawString(line, PLAN_X, py, color);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // Status line
        BE_CraftingPlan plan = containerRT.getCurrentPlan();
        if (plan != null) {
            String status = plan.isComplete() ? "Ready to craft" : "Missing items!";
            int statusColor = plan.isComplete() ? 0x00AA00 : 0xAA0000;
            this.fontRenderer.drawString(status, 8, 143, statusColor);
        }

        // Confirm / Cancel buttons
        boolean canConfirm = plan != null && plan.isComplete();
        draw3DButton(BTN_CONFIRM_X, BTN_CONFIRM_Y, BTN_W, BTN_H, canConfirm ? "Confirm" : "Confirm");
        draw3DButton(BTN_CANCEL_X, BTN_CANCEL_Y, BTN_W, BTN_H, "Cancel");
    }

    private void draw3DButton(int bx, int by, int bw, int bh, String label) {
        drawRect(bx, by, bx + bw, by + bh, 0xFF555555);
        drawRect(bx, by, bx + bw - 1, by + 1, 0xFFFFFFFF);
        drawRect(bx, by, bx + 1, by + bh - 1, 0xFFFFFFFF);
        drawRect(bx + 1, by + 1, bx + bw - 1, by + bh - 1, 0xFFA0A0A0);
        int labelW = this.fontRenderer.getStringWidth(label);
        this.fontRenderer.drawString(label, bx + (bw - labelW) / 2, by + 2, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw background (dark gray fill + border)
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);
        // Top/left highlight
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        // Bottom/right shadow
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);

        if (containerRT.getMode() == BE_ContainerRequestTerminal.MODE_BROWSE) {
            drawBrowseBackground(x, y);
        } else {
            drawPreviewBackground(x, y);
        }

        // Player inventory slots background
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + 157 + row * 18;
                drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFFFFFFFF);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
                drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = y + 215;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFFFFFFFF);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
            drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
        }
    }

    private void drawBrowseBackground(int x, int y) {
        // Grid slot backgrounds
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int sx = x + GRID_X - 1 + col * CELL_SIZE;
                int sy = y + GRID_Y - 1 + row * CELL_SIZE;
                drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFFFFFFFF);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
                drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
            }
        }

        // Render craftable items
        containerRT.refreshCraftableItems();
        List<BE_ContainerRequestTerminal.CraftableEntry> items = containerRT.getCraftableItems();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, 0.0F);
        GL11.glPushMatrix();
        GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        int startIndex = scrollOffset * GRID_COLS;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = startIndex + row * GRID_COLS + col;
                if (index < items.size()) {
                    BE_ContainerRequestTerminal.CraftableEntry entry = items.get(index);
                    int ix = GRID_X + col * CELL_SIZE;
                    int iy = GRID_Y + row * CELL_SIZE;
                    ItemStack stack = new ItemStack(entry.key.itemId, 1, entry.key.damageValue);
                    if (stack.getItem() != null) {
                        gridItemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, ix, iy);
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

    private void drawPreviewBackground(int x, int y) {
        // Preview area background (slightly darker)
        drawRect(x + 4, y + 17, x + 172, y + 155, 0xFF8B8B8B);
        drawRect(x + 5, y + 18, x + 171, y + 154, 0xFFC6C6C6);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;

        if (containerRT.getMode() == BE_ContainerRequestTerminal.MODE_BROWSE) {
            // Check grid cell click
            if (relX >= GRID_X && relX < GRID_X + GRID_COLS * CELL_SIZE
                && relY >= GRID_Y && relY < GRID_Y + GRID_ROWS * CELL_SIZE) {
                int col = (relX - GRID_X) / CELL_SIZE;
                int row = (relY - GRID_Y) / CELL_SIZE;
                int index = (scrollOffset + row) * GRID_COLS + col;

                List<BE_ContainerRequestTerminal.CraftableEntry> items = containerRT.getCraftableItems();
                if (index >= 0 && index < items.size()) {
                    BE_ItemKey key = items.get(index).key;
                    containerRT.sendAction(BE_ContainerRequestTerminal.ACTION_SELECT, key, 1);
                    planScrollOffset = 0;
                }
                return;
            }
        } else {
            // Preview mode clicks

            // Quantity minus button
            if (relX >= QTY_MINUS_X && relX < QTY_MINUS_X + QTY_BTN_W
                && relY >= QTY_Y && relY < QTY_Y + QTY_BTN_H) {
                containerRT.sendAction(BE_ContainerRequestTerminal.ACTION_DEC_QTY, null, 0);
                return;
            }
            // Quantity plus button
            if (relX >= QTY_PLUS_X && relX < QTY_PLUS_X + QTY_BTN_W
                && relY >= QTY_Y && relY < QTY_Y + QTY_BTN_H) {
                containerRT.sendAction(BE_ContainerRequestTerminal.ACTION_INC_QTY, null, 0);
                return;
            }

            // Confirm button
            if (relX >= BTN_CONFIRM_X && relX < BTN_CONFIRM_X + BTN_W
                && relY >= BTN_CONFIRM_Y && relY < BTN_CONFIRM_Y + BTN_H) {
                containerRT.sendAction(BE_ContainerRequestTerminal.ACTION_CONFIRM, null, 0);
                return;
            }
            // Cancel button
            if (relX >= BTN_CANCEL_X && relX < BTN_CANCEL_X + BTN_W
                && relY >= BTN_CANCEL_Y && relY < BTN_CANCEL_Y + BTN_H) {
                containerRT.sendAction(BE_ContainerRequestTerminal.ACTION_CANCEL, null, 0);
                scrollOffset = 0;
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0) {
            if (containerRT.getMode() == BE_ContainerRequestTerminal.MODE_BROWSE) {
                List<BE_ContainerRequestTerminal.CraftableEntry> items = containerRT.getCraftableItems();
                int totalRows = (items.size() + GRID_COLS - 1) / GRID_COLS;
                int maxScroll = Math.max(0, totalRows - GRID_ROWS);
                if (scroll < 0) {
                    scrollOffset = Math.min(scrollOffset + 1, maxScroll);
                } else {
                    scrollOffset = Math.max(scrollOffset - 1, 0);
                }
            } else {
                List<BE_ContainerRequestTerminal.PlanEntry> entries = containerRT.getPlanEntries();
                int maxScroll = Math.max(0, entries.size() - PLAN_ROWS);
                if (scroll < 0) {
                    planScrollOffset = Math.min(planScrollOffset + 1, maxScroll);
                } else {
                    planScrollOffset = Math.max(planScrollOffset - 1, 0);
                }
            }
        }
    }
}
