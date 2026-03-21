package betaenergistics.gui;

import betaenergistics.container.BE_ContainerFluidTerminal;
import betaenergistics.storage.BE_FluidKey;
import betaenergistics.tile.BE_TileFluidTerminal;

import aero.machineapi.Aero_FluidType;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Fluid Terminal GUI — shows all fluids in the network as colored bars with amounts.
 * Rendered via drawRect (no texture file needed).
 */
public class BE_GuiFluidTerminal extends GuiContainer {
    private static final int LIST_X = 8;
    private static final int LIST_Y = 20;
    private static final int LIST_W = 160;
    private static final int ROW_H = 20;
    private static final int MAX_ROWS = 6;

    private BE_ContainerFluidTerminal containerFluid;
    private BE_TileFluidTerminal tileTerminal;
    private int scrollOffset = 0;

    public BE_GuiFluidTerminal(InventoryPlayer playerInv, BE_TileFluidTerminal terminal) {
        super(new BE_ContainerFluidTerminal(playerInv, terminal));
        this.containerFluid = (BE_ContainerFluidTerminal) this.inventorySlots;
        this.tileTerminal = terminal;
        this.xSize = 176;
        this.ySize = 222;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Fluid Terminal", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        containerFluid.refreshFluids();
        List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();

        if (fluids.isEmpty()) {
            this.fontRenderer.drawString("No fluids stored", LIST_X + 4, LIST_Y + 4, 0x808080);
            return;
        }

        // Draw fluid entries
        for (int i = 0; i < MAX_ROWS; i++) {
            int index = scrollOffset + i;
            if (index >= fluids.size()) break;

            BE_ContainerFluidTerminal.BE_FluidEntry entry = fluids.get(index);
            int y = LIST_Y + i * ROW_H;

            // Fluid name
            String name = Aero_FluidType.getName(entry.key.fluidType);
            this.fontRenderer.drawString(name, LIST_X + 4, y + 2, 0xFFFFFF);

            // Amount
            String amount = formatAmount(entry.amountMB) + " mB";
            int amountW = this.fontRenderer.getStringWidth(amount);
            this.fontRenderer.drawString(amount, LIST_X + LIST_W - amountW - 4, y + 2, 0xC0C0C0);

            // Fluid bar
            int color = Aero_FluidType.getColor(entry.key.fluidType);
            int barY = y + 12;
            int barW = LIST_W - 8;
            // Background
            drawRect(LIST_X + 4, barY, LIST_X + 4 + barW, barY + 4, 0xFF333333);
            // Fill (proportional, but we don't know total capacity so just show it)
            int fillW = Math.max(2, barW);
            drawRect(LIST_X + 4, barY, LIST_X + 4 + fillW, barY + 4, color);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw background via drawRect (dark gray panel)
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);
        // Border
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);

        // Title bar
        drawRect(x + 3, y + 3, x + xSize - 3, y + 16, 0xFF373737);

        // Fluid list area
        drawRect(x + LIST_X, y + LIST_Y, x + LIST_X + LIST_W, y + LIST_Y + MAX_ROWS * ROW_H, 0xFF2A2A2A);
        // List border
        drawRect(x + LIST_X, y + LIST_Y, x + LIST_X + LIST_W, y + LIST_Y + 1, 0xFF555555);
        drawRect(x + LIST_X, y + LIST_Y, x + LIST_X + 1, y + LIST_Y + MAX_ROWS * ROW_H, 0xFF555555);
        drawRect(x + LIST_X + LIST_W - 1, y + LIST_Y, x + LIST_X + LIST_W, y + LIST_Y + MAX_ROWS * ROW_H, 0xFFFFFFFF);
        drawRect(x + LIST_X, y + LIST_Y + MAX_ROWS * ROW_H - 1, x + LIST_X + LIST_W, y + LIST_Y + MAX_ROWS * ROW_H, 0xFFFFFFFF);

        // Player inventory area (standard background)
        int invY = y + ySize - 96 - 10;
        drawRect(x + 7, invY, x + xSize - 7, y + ySize - 7, 0xFF8B8B8B);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0) {
            List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();
            int maxScroll = Math.max(0, fluids.size() - MAX_ROWS);
            if (scroll < 0) {
                scrollOffset = Math.min(scrollOffset + 1, maxScroll);
            } else {
                scrollOffset = Math.max(scrollOffset - 1, 0);
            }
        }
    }

    private String formatAmount(int mB) {
        if (mB >= 1000000) {
            int buckets = mB / 1000;
            return buckets + "K";
        }
        if (mB >= 1000) {
            int buckets = mB / 1000;
            int frac = (mB % 1000) / 100;
            if (frac > 0) return buckets + "." + frac + "B";
            return buckets + "B";
        }
        return String.valueOf(mB);
    }
}
