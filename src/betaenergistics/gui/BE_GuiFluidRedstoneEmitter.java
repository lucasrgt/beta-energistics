package betaenergistics.gui;

import betaenergistics.container.BE_ContainerFluidRedstoneEmitter;
import betaenergistics.storage.BE_FluidKey;
import betaenergistics.tile.BE_TileFluidRedstoneEmitter;

import aero.machineapi.Aero_FluidType;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * Fluid Redstone Emitter GUI — fluid type filter (ghost), threshold +/- buttons, mode cycle.
 * Similar layout to item Redstone Emitter but with fluid-specific display.
 */
public class BE_GuiFluidRedstoneEmitter extends GuiContainer {
    private static final int GUI_W = 176;
    private static final int GUI_H = 166;

    // Known fluid types for selection
    private static final int[] KNOWN_FLUID_TYPES = {Aero_FluidType.WATER, Aero_FluidType.HEAVY_WATER};

    // Ghost filter slot position (shows fluid color swatch)
    private static final int FILTER_X = 8;
    private static final int FILTER_Y = 20;
    private static final int FILTER_W = 16;
    private static final int FILTER_H = 16;

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

    // Fluid selector area
    private static final int FLUID_LIST_Y = 58;
    private static final int FLUID_ROW_H = 12;

    private BE_ContainerFluidRedstoneEmitter containerFRE;
    private BE_TileFluidRedstoneEmitter tile;

    public BE_GuiFluidRedstoneEmitter(InventoryPlayer playerInv, BE_TileFluidRedstoneEmitter tile) {
        super(new BE_ContainerFluidRedstoneEmitter(playerInv, tile));
        this.containerFRE = (BE_ContainerFluidRedstoneEmitter) this.inventorySlots;
        this.tile = tile;
        this.xSize = GUI_W;
        this.ySize = GUI_H;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Fluid Redstone Emitter", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        // Mode button
        draw3DButton(MODE_X, MODE_Y, MODE_W, MODE_H, tile.getModeLabel());

        // Threshold buttons: -10, -1, value, +1, +10
        draw3DButton(THR_MINUS10_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "-10");
        draw3DButton(THR_MINUS_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "-1");
        draw3DButton(THR_PLUS10_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "+1");
        draw3DButton(THR_PLUS_X, THR_BTN_Y, THR_BTN_W, THR_BTN_H, "+10");

        // Threshold value (in mB)
        String thrStr = "T:" + tile.getThreshold() + " mB";
        this.fontRenderer.drawString(thrStr, THR_LABEL_X, THR_LABEL_Y, 4210752);

        // Status line: current signal state
        BE_FluidKey filter = tile.getFilterFluid();
        if (filter != null) {
            String status = "Signal: " + (tile.getRedstoneOutput() > 0 ? "ON" : "OFF");
            int statusColor = tile.getRedstoneOutput() > 0 ? 0x00AA00 : 0xAA0000;
            this.fontRenderer.drawString(status, 8, STATUS_Y, statusColor);

            // Show fluid name
            String fluidName = filter.getName();
            this.fontRenderer.drawString(fluidName, 8, STATUS_Y + 12, 4210752);
        } else {
            this.fontRenderer.drawString("No fluid filter set", 8, STATUS_Y, 0x808080);
        }

        // Fluid type quick-select labels
        this.fontRenderer.drawString("Fluids:", 8, FLUID_LIST_Y - 10, 4210752);
        for (int i = 0; i < KNOWN_FLUID_TYPES.length; i++) {
            int fType = KNOWN_FLUID_TYPES[i];
            String name = Aero_FluidType.getName(fType);
            int color = Aero_FluidType.getColor(fType);
            int fy = FLUID_LIST_Y + i * FLUID_ROW_H;
            // Color swatch
            drawRect(8, fy, 18, fy + 10, 0xFF000000 | color);
            // Name
            this.fontRenderer.drawString(name, 22, fy + 1, 4210752);
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

        // Ghost filter slot background (shows fluid color swatch)
        int fsx = x + FILTER_X - 1;
        int fsy = y + FILTER_Y - 1;
        drawRect(fsx, fsy, fsx + 18, fsy + 18, 0xFF8B8B8B);
        drawRect(fsx + 1, fsy + 1, fsx + 17, fsy + 17, 0xFFFFFFFF);
        drawRect(fsx + 1, fsy + 1, fsx + 17, fsy + 2, 0xFF373737);
        drawRect(fsx + 1, fsy + 1, fsx + 2, fsy + 17, 0xFF373737);

        // Render fluid color in filter slot
        BE_FluidKey filter = tile.getFilterFluid();
        if (filter != null) {
            int fluidColor = filter.getColor();
            drawRect(x + FILTER_X, y + FILTER_Y, x + FILTER_X + FILTER_W, y + FILTER_Y + FILTER_H, 0xFF000000 | fluidColor);
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

        // Ghost filter slot click — cycle through available fluid types
        if (relX >= FILTER_X - 1 && relX < FILTER_X + 17
            && relY >= FILTER_Y - 1 && relY < FILTER_Y + 17) {
            BE_FluidKey current = tile.getFilterFluid();
            if (current == null) {
                tile.setFilterFluid(new BE_FluidKey(KNOWN_FLUID_TYPES[0]));
            } else {
                // Find current index and cycle to next
                int nextIdx = 0;
                for (int i = 0; i < KNOWN_FLUID_TYPES.length; i++) {
                    if (KNOWN_FLUID_TYPES[i] == current.fluidType) {
                        nextIdx = (i + 1) % (KNOWN_FLUID_TYPES.length + 1); // +1 to allow clearing
                        break;
                    }
                }
                if (nextIdx >= KNOWN_FLUID_TYPES.length) {
                    tile.setFilterFluid(null); // Clear filter
                } else {
                    tile.setFilterFluid(new BE_FluidKey(KNOWN_FLUID_TYPES[nextIdx]));
                }
            }
            return;
        }

        // Fluid list quick-select
        for (int i = 0; i < KNOWN_FLUID_TYPES.length; i++) {
            int fy = FLUID_LIST_Y + i * FLUID_ROW_H;
            if (relX >= 8 && relX < 100 && relY >= fy && relY < fy + FLUID_ROW_H) {
                tile.setFilterFluid(new BE_FluidKey(KNOWN_FLUID_TYPES[i]));
                return;
            }
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
