package betaenergistics.gui;

import betaenergistics.container.BE_ContainerAutocrafter;
import betaenergistics.tile.BE_TileAutocrafter;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * GUI for the Autocrafter.
 *
 * Layout (176x166):
 *   3x3 pattern slot grid centered at (62, 17)
 *   Progress bar below grid (62, 73) — 54px wide
 *   Status text showing current craft
 *   Player inventory at bottom
 */
public class BE_GuiAutocrafter extends GuiContainer {
    private BE_TileAutocrafter crafter;

    // Progress bar
    private static final int BAR_X = 62;
    private static final int BAR_Y = 73;
    private static final int BAR_W = 54;
    private static final int BAR_H = 5;

    public BE_GuiAutocrafter(InventoryPlayer playerInv, BE_TileAutocrafter crafter) {
        super(new BE_ContainerAutocrafter(playerInv, crafter));
        this.crafter = crafter;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Autocrafter", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);

        // Show crafting status
        if (crafter.isCrafting()) {
            int idx = crafter.getActiveCraftIndex();
            ItemStack output = crafter.getPatternOutput(idx);
            if (output != null) {
                String name = StringTranslate.getInstance().translateNamedKey(output.getItem().getItemName());
                String status = "Crafting: " + name;
                if (status.length() > 24) status = status.substring(0, 22) + "..";
                this.fontRenderer.drawString(status, 8, BAR_Y + BAR_H + 2, 4210752);
            }
        } else {
            this.fontRenderer.drawString("Idle", 8, BAR_Y + BAR_H + 2, 0xFF888888);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Background
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);

        // Title bar
        drawRect(x + 4, y + 4, x + xSize - 4, y + 14, 0xFF8B8B8B);

        // Pattern slot grid (3x3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = x + 61 + col * 18;
                int sy = y + 16 + row * 18;
                drawSlotBackground(sx, sy);
            }
        }

        // Progress bar background
        int bx = x + BAR_X;
        int by = y + BAR_Y;
        drawRect(bx, by, bx + BAR_W, by + BAR_H, 0xFF555555);

        // Progress bar fill
        if (crafter.isCrafting()) {
            int fillWidth = crafter.getCraftProgressScaled(BAR_W);
            if (fillWidth > 0) {
                drawRect(bx, by, bx + fillWidth, by + BAR_H, 0xFF44CC44);
            }
        }

        // Player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBackground(x + 7 + col * 18, y + 83 + row * 18);
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            drawSlotBackground(x + 7 + col * 18, y + 141);
        }

        // Border
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);
    }

    private void drawSlotBackground(int sx, int sy) {
        drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
        drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
        drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
    }
}
