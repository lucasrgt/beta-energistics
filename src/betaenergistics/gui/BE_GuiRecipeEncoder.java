package betaenergistics.gui;

import betaenergistics.container.BE_ContainerRecipeEncoder;
import betaenergistics.tile.BE_TileRecipeEncoder;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * GUI for Recipe Encoder.
 *
 * Layout (176x166):
 *   3x3 ghost input grid at (30, 17)
 *   Arrow at (90, 35)
 *   Output preview at (124, 35)
 *   Pattern slot at (124, 53)
 *   Encode button at (93, 55) — drawRect-based
 *   Player inventory at bottom
 */
public class BE_GuiRecipeEncoder extends GuiContainer {
    private BE_TileRecipeEncoder encoder;
    private BE_ContainerRecipeEncoder containerEncoder;

    // Encode button bounds (relative to GUI top-left)
    private static final int BTN_X = 93;
    private static final int BTN_Y = 55;
    private static final int BTN_W = 26;
    private static final int BTN_H = 14;

    public BE_GuiRecipeEncoder(InventoryPlayer playerInv, BE_TileRecipeEncoder encoder) {
        super(new BE_ContainerRecipeEncoder(playerInv, encoder));
        this.encoder = encoder;
        this.containerEncoder = (BE_ContainerRecipeEncoder) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Recipe Encoder", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);

        // Draw encode button text
        String btnLabel = "Set";
        int textWidth = this.fontRenderer.getStringWidth(btnLabel);
        this.fontRenderer.drawString(btnLabel, BTN_X + (BTN_W - textWidth) / 2, BTN_Y + 3, 4210752);

        // Draw arrow between grid and output
        this.fontRenderer.drawString("\u2192", 96, 38, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw background (dark gray)
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);

        // Draw title bar area
        drawRect(x + 4, y + 4, x + xSize - 4, y + 14, 0xFF8B8B8B);

        // Draw ghost input grid (3x3) — slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = x + 29 + col * 18;
                int sy = y + 16 + row * 18;
                drawSlotBackground(sx, sy);
            }
        }

        // Draw output preview slot background
        drawSlotBackground(x + 123, y + 34);

        // Draw pattern slot background
        drawSlotBackground(x + 123, y + 52);

        // Draw encode button (3D look)
        int bx = x + BTN_X;
        int by = y + BTN_Y;
        drawRect(bx, by, bx + BTN_W, by + BTN_H, 0xFFAAAAAA);        // fill
        drawRect(bx, by, bx + BTN_W, by + 1, 0xFFFFFFFF);            // top edge
        drawRect(bx, by, bx + 1, by + BTN_H, 0xFFFFFFFF);            // left edge
        drawRect(bx + BTN_W - 1, by, bx + BTN_W, by + BTN_H, 0xFF555555); // right edge
        drawRect(bx, by + BTN_H - 1, bx + BTN_W, by + BTN_H, 0xFF555555); // bottom edge

        // Draw player inventory area
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBackground(x + 7 + col * 18, y + 83 + row * 18);
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            drawSlotBackground(x + 7 + col * 18, y + 141);
        }

        // Draw border
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);
    }

    private void drawSlotBackground(int sx, int sy) {
        drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);              // border
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);      // inner dark
        drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);       // top inner
        drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);       // left inner
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);      // fill
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Check encode button click
        int relX = mouseX - x;
        int relY = mouseY - y;
        if (relX >= BTN_X && relX < BTN_X + BTN_W && relY >= BTN_Y && relY < BTN_Y + BTN_H) {
            containerEncoder.encode();
            return;
        }

        // Check ghost slot clicks — handle before super to intercept
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = x + 30 + col * 18;
                int sy = y + 17 + row * 18;
                if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16) {
                    int slotIndex = row * 3 + col;
                    ItemStack cursor = this.mc.thePlayer.inventory.getItemStack();
                    if (button == 1 || cursor == null) {
                        // Right-click or empty cursor: clear ghost slot
                        containerEncoder.handleGhostClick(slotIndex, null);
                    } else {
                        // Left-click with item: set ghost slot
                        containerEncoder.handleGhostClick(slotIndex, cursor);
                    }
                    return;
                }
            }
        }

        // Default handling for pattern slot and player inventory
        super.mouseClicked(mouseX, mouseY, button);
    }
}
