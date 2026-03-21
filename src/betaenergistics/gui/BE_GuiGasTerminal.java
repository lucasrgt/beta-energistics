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
public class BE_GuiGasTerminal extends BE_GuiTerminalBase {
    private static final String TEXTURE = "/gui/be_grid_terminal.png";

    private BE_ContainerGasTerminal containerGas;
    private BE_TileGasTerminal tileTerminal;

    public BE_GuiGasTerminal(InventoryPlayer playerInv, BE_TileGasTerminal terminal) {
        super(new BE_ContainerGasTerminal(playerInv, terminal));
        this.containerGas = (BE_ContainerGasTerminal) this.inventorySlots;
        this.tileTerminal = terminal;
    }

    // ====== Abstract implementations ======

    @Override
    protected String getTerminalTitle() { return "Gas Terminal"; }

    @Override
    protected String getTexturePath() { return TEXTURE; }

    @Override
    protected void refreshData() {
        containerGas.refreshGases();
    }

    @Override
    protected int getEntryCount() {
        return containerGas.getGases().size();
    }

    @Override
    protected void renderEntryAt(int x, int y, int index) {
        List<BE_ContainerGasTerminal.BE_GasEntry> gases = containerGas.getGases();
        if (index >= gases.size()) return;
        BE_ContainerGasTerminal.BE_GasEntry entry = gases.get(index);

        // Gas rendered as colored square with gradient (gas-like appearance)
        int color = entry.key.getColor();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int colorTop = (0xCC << 24) | (Math.min(255, r + 40) << 16) | (Math.min(255, g + 40) << 8) | Math.min(255, b + 40);
        int colorBot = (0x88 << 24) | (r << 16) | (g << 8) | b;
        drawGradientRect(x + 1, y + 1, x + 15, y + 15, colorTop, colorBot);
    }

    @Override
    protected void renderEntryAmount(int index, int x, int y) {
        List<BE_ContainerGasTerminal.BE_GasEntry> gases = containerGas.getGases();
        if (index >= gases.size()) return;
        int mB = gases.get(index).amountMB;
        if (mB <= 0) return;
        renderScaledAmount(formatFluidAmount(mB), x, y);
    }

    @Override
    protected String getEntryTooltip(int index) {
        List<BE_ContainerGasTerminal.BE_GasEntry> gases = containerGas.getGases();
        if (index >= gases.size()) return null;
        BE_ContainerGasTerminal.BE_GasEntry entry = gases.get(index);
        return entry.key.getName() + " - " + formatFluidAmountFull(entry.amountMB);
    }

    @Override
    protected void handleGridClick(int index, int mouseButton) {
        // Gas terminal currently read-only
    }
}
