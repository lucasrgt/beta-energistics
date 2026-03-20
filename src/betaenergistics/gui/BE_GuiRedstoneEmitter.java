package betaenergistics.gui;

import betaenergistics.container.BE_ContainerRedstoneEmitter;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileRedstoneEmitter;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Redstone Emitter GUI — ghost filter slot, threshold +/- buttons, mode cycle button.
 * Compact layout: filter slot + mode button + threshold display/buttons + status.
 */
public class BE_GuiRedstoneEmitter extends GuiContainer {
    private static final int GUI_W = 176;
    private static final int GUI_H = 166;

    // Ghost filter slot position (relative to guiLeft/guiTop)
    private static final int FILTER_X = 8;
    private static final int FILTER_Y = 20;

    // Mode button
    private static final int MODE_X = 32;
    private static final int MODE_Y = 20;
    private static final int MODE_W = 30;
    private static final int MODE_H = 16;

    // Threshold display and buttons
    private static final int THR_LABEL_X = 70;
    private static final int THR_LABEL_Y = 24;
    private static final int THR_MINUS_X = 110;
    private static final int THR_MINUS10_X = 90;
    private static final int THR_PLUS_X = 150;
    private static final int THR_PLUS10_X = 130;
    private static final int THR_BTN_Y = 20;
    private static final int THR_BTN_W = 16;
    private static final int THR_BTN_H = 16;

    // Status area
    private static final int STATUS_Y = 44;

    private static final RenderItem itemRenderer = new RenderItem();

    private BE_ContainerRedstoneEmitter containerRE;
    private BE_TileRedstoneEmitter tile;

    public BE_GuiRedstoneEmitter(InventoryPlayer playerInv, BE_TileRedstoneEmitter tile) {
        super(new BE_ContainerRedstoneEmitter(playerInv, tile));
        this.containerRE = (BE_ContainerRedstoneEmitter) this.inventorySlots;
        this.tile = tile;
        this.xSize = GUI_W;
        this.ySize = GUI_H;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Redstone Emitter", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        // Mode button
        draw3DButton(MODE_X, MODE_Y, MODE_W, MODE_H, tile.getModeLabel());

        // Threshold buttons: -10, -1, value, +1, +10
        draw3DButton(THR_MINUS10_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "-10");
        draw3DButton(THR_MINUS_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "-1");
        draw3DButton(THR_PLUS10_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "+1");
        draw3DButton(THR_PLUS_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "+10");

        // Threshold value
        String thrStr = "T:" + tile.getThreshold();
        this.fontRenderer.drawString(thrStr, THR_LABEL_X, THR_LABEL_Y, 4210752);

        // Status line: current count and output
        BE_ItemKey filter = tile.getFilterItem();
        if (filter != null) {
            String status = "Signal: " + (tile.getRedstoneOutput() > 0 ? "ON" : "OFF");
            int statusColor = tile.getRedstoneOutput() > 0 ? 0x00AA00 : 0xAA0000;
            this.fontRenderer.drawString(status, 8, STATUS_Y, statusColor);

            // Show item name
            Item item = Item.itemsList[filter.itemId];
            if (item != null) {
                String name = ("" + StringTranslate.getInstance().translateNamedKey(
                    item.getItemNameIS(new ItemStack(filter.itemId, 1, filter.damageValue)))).trim();
                this.fontRenderer.drawString(name, 8, STATUS_Y + 12, 4210752);
            }
        } else {
            this.fontRenderer.drawString("No filter set", 8, STATUS_Y, 0x808080);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Main background
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);
        // Top/left highlight
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        // Bottom/right shadow
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);

        // Ghost filter slot background
        int fsx = x + FILTER_X - 1;
        int fsy = y + FILTER_Y - 1;
        drawRect(fsx, fsy, fsx + 18, fsy + 18, 0xFF8B8B8B);
        drawRect(fsx + 1, fsy + 1, fsx + 17, fsy + 17, 0xFFFFFFFF);
        drawRect(fsx + 1, fsy + 1, fsx + 17, fsy + 2, 0xFF373737);
        drawRect(fsx + 1, fsy + 1, fsx + 2, fsy + 17, 0xFF373737);

        // Render the filter item if set
        BE_ItemKey filter = tile.getFilterItem();
        if (filter != null) {
            ItemStack displayStack = new ItemStack(filter.itemId, 1, filter.damageValue);
            if (displayStack.getItem() != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) x, (float) y, 0.0F);
                GL11.glPushMatrix();
                GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
                RenderHelper.enableStandardItemLighting();
                GL11.glPopMatrix();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);

                itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, displayStack, FILTER_X, FILTER_Y);

                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.disableStandardItemLighting();
                GL11.glPopMatrix();
            }
        }

        // Player inventory slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + 83 + row * 18;
                drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFFFFFFFF);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
                drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = y + 141;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFFFFFFFF);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
            drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
        }
    }

    private void draw3DButton(int bx, int by, int bw, int bh, String label) {
        drawRect(bx, by, bx + bw, by + bh, 0xFF555555);
        drawRect(bx, by, bx + bw - 1, by + 1, 0xFFFFFFFF);
        drawRect(bx, by, bx + 1, by + bh - 1, 0xFFFFFFFF);
        drawRect(bx + 1, by + 1, bx + bw - 1, by + bh - 1, 0xFFA0A0A0);
        int labelW = this.fontRenderer.getStringWidth(label);
        this.fontRenderer.drawString(label, bx + (bw - labelW) / 2, by + (bh - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;

        // Ghost filter slot click — copy item from cursor
        if (relX >= FILTER_X - 1 && relX < FILTER_X + 17
            && relY >= FILTER_Y - 1 && relY < FILTER_Y + 17) {
            ItemStack held = this.mc.thePlayer.inventory.getItemStack();
            if (held != null) {
                tile.setFilterItem(new BE_ItemKey(held.itemID, held.getItemDamage()));
            } else {
                tile.setFilterItem(null);
            }
            return;
        }

        // Mode button
        if (relX >= MODE_X && relX < MODE_X + MODE_W
            && relY >= MODE_Y && relY < MODE_Y + MODE_H) {
            tile.cycleMode();
            return;
        }

        // Threshold -10
        if (relX >= THR_MINUS10_X && relX < THR_MINUS10_X + THR_BTN_W
            && relY >= THR_BTN_Y && relY < THR_BTN_Y + THR_BTN_H) {
            tile.setThreshold(tile.getThreshold() - 10);
            return;
        }

        // Threshold -1
        if (relX >= THR_MINUS_X && relX < THR_MINUS_X + THR_BTN_W
            && relY >= THR_BTN_Y && relY < THR_BTN_Y + THR_BTN_H) {
            tile.setThreshold(tile.getThreshold() - 1);
            return;
        }

        // Threshold +1
        if (relX >= THR_PLUS10_X && relX < THR_PLUS10_X + THR_BTN_W
            && relY >= THR_BTN_Y && relY < THR_BTN_Y + THR_BTN_H) {
            tile.setThreshold(tile.getThreshold() + 1);
            return;
        }

        // Threshold +10
        if (relX >= THR_PLUS_X && relX < THR_PLUS_X + THR_BTN_W
            && relY >= THR_BTN_Y && relY < THR_BTN_Y + THR_BTN_H) {
            tile.setThreshold(tile.getThreshold() + 10);
            return;
        }


        super.mouseClicked(mouseX, mouseY, button);
    }
}
