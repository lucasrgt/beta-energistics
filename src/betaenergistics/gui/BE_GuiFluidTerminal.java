package betaenergistics.gui;

import betaenergistics.container.BE_ContainerFluidTerminal;
import betaenergistics.storage.BE_FluidKey;
import betaenergistics.tile.BE_TileFluidTerminal;

import aero.machineapi.Aero_FluidType;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

/**
 * Fluid Terminal GUI — same layout as Grid Terminal but for fluids.
 * Each slot shows a colored fluid icon with amount in mB.
 */
public class BE_GuiFluidTerminal extends BE_GuiTerminalBase {
    private static final String TEXTURE = "/gui/be_grid_terminal.png";

    private BE_ContainerFluidTerminal containerFluid;
    private BE_TileFluidTerminal tileTerminal;

    public BE_GuiFluidTerminal(InventoryPlayer playerInv, BE_TileFluidTerminal terminal) {
        super(new BE_ContainerFluidTerminal(playerInv, terminal));
        this.containerFluid = (BE_ContainerFluidTerminal) this.inventorySlots;
        this.tileTerminal = terminal;
    }

    // ====== Abstract implementations ======

    @Override
    protected String getTerminalTitle() { return "Fluid Terminal"; }

    @Override
    protected String getTexturePath() { return TEXTURE; }

    @Override
    protected boolean needsItemLighting() { return true; }

    @Override
    protected void refreshData() {
        containerFluid.refreshFluids();
    }

    @Override
    protected int getEntryCount() {
        return containerFluid.getFluids().size();
    }

    @Override
    protected void renderEntryAt(int x, int y, int index) {
        List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();
        if (index >= fluids.size()) return;
        BE_ContainerFluidTerminal.BE_FluidEntry entry = fluids.get(index);

        int fluidBlockId = getFluidBlockId(entry.key.fluidType);
        if (fluidBlockId > 0 && Block.blocksList[fluidBlockId] != null) {
            ItemStack fluidStack = new ItemStack(fluidBlockId, 1, 0);
            RenderItem ri = new RenderItem();
            ri.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, fluidStack, x, y);
        } else {
            GL11.glDisable(GL11.GL_LIGHTING);
            drawRect(x + 1, y + 1, x + 15, y + 15, entry.key.getColor());
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    @Override
    protected void renderEntryAmount(int index, int x, int y) {
        List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();
        if (index >= fluids.size()) return;
        int mB = fluids.get(index).amountMB;
        if (mB <= 0) return;
        renderScaledAmount(formatFluidAmount(mB), x, y);
    }

    @Override
    protected String getEntryTooltip(int index) {
        List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();
        if (index >= fluids.size()) return null;
        BE_ContainerFluidTerminal.BE_FluidEntry entry = fluids.get(index);
        return entry.key.getName() + " - " + formatFluidAmountFull(entry.amountMB);
    }

    @Override
    protected void handleGridClick(int index, int mouseButton) {
        ItemStack held = this.mc.thePlayer.inventory.getItemStack();
        if (held != null) {
            containerFluid.handleBucketClick(held, this.mc.thePlayer);
        }
        // Future: click on fluid with empty hand to extract
    }

    private int getFluidBlockId(int fluidType) {
        switch (fluidType) {
            case Aero_FluidType.WATER: return 9;  // waterStill
            case Aero_FluidType.LAVA: return 11;   // lavaStill
            default: return 0; // colored square fallback
        }
    }
}
