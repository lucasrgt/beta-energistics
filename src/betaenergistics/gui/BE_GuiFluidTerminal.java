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
public class BE_GuiFluidTerminal extends GuiContainer {
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 7;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 19;
    private static final int CELL_SIZE = 18;
    private static final String TEXTURE = "/gui/be_grid_terminal.png";

    private BE_ContainerFluidTerminal containerFluid;
    private BE_TileFluidTerminal tileTerminal;
    private int scrollOffset = 0;
    private String searchText = "";
    private boolean searchFocused = false;

    public BE_GuiFluidTerminal(InventoryPlayer playerInv, BE_TileFluidTerminal terminal) {
        super(new BE_ContainerFluidTerminal(playerInv, terminal));
        this.containerFluid = (BE_ContainerFluidTerminal) this.inventorySlots;
        this.tileTerminal = terminal;
        this.xSize = 176;
        this.ySize = 240;
    }

    private int screenMouseX, screenMouseY;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.screenMouseX = mouseX;
        this.screenMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Fluid Terminal", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        // Search box text
        String displayText = searchFocused ? searchText + "_" : (searchText.isEmpty() ? "Search..." : searchText);
        int textColor = searchFocused ? 0xFFFFFF : 0xA0A0A0;
        this.fontRenderer.drawString(displayText, 100, 6, textColor);

        // Tooltip for hovered fluid
        containerFluid.refreshFluids();
        List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (this.mc.thePlayer.inventory.getItemStack() == null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int relX = this.screenMouseX - guiLeft;
            int relY = this.screenMouseY - guiTop;

            if (relX >= GRID_X && relX < GRID_X + GRID_COLS * CELL_SIZE
                && relY >= GRID_Y && relY < GRID_Y + GRID_ROWS * CELL_SIZE) {
                int col = (relX - GRID_X) / CELL_SIZE;
                int row = (relY - GRID_Y) / CELL_SIZE;
                int index = (scrollOffset + row) * GRID_COLS + col;
                if (index >= 0 && index < fluids.size()) {
                    BE_ContainerFluidTerminal.BE_FluidEntry entry = fluids.get(index);
                    String name = entry.key.getName() + " - " + formatAmount(entry.amountMB);
                    int tx = relX + 12;
                    int ty = relY - 12;
                    int tw = this.fontRenderer.getStringWidth(name);
                    this.drawGradientRect(tx - 3, ty - 3, tx + tw + 3, ty + 8 + 3, -1073741824, -1073741824);
                    this.fontRenderer.drawStringWithShadow(name, tx, ty, -1);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int texId = this.mc.renderEngine.getTexture(TEXTURE);
        this.mc.renderEngine.bindTexture(texId);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw GUI background (reuse grid terminal texture)
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Scrollbar tab (right)
        drawScrollbarTab(x + 172, y + 14, 22, 134);

        // Render fluid icons (exact same pattern as BE_GuiGrid)
        containerFluid.refreshFluids();
        List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)x, (float)y, 0.0F);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            RenderItem ri = new RenderItem();
            int startIndex = scrollOffset * GRID_COLS;
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    int index = startIndex + row * GRID_COLS + col;
                    if (index < fluids.size()) {
                        BE_ContainerFluidTerminal.BE_FluidEntry entry = fluids.get(index);
                        int ix = GRID_X + col * CELL_SIZE;
                        int iy = GRID_Y + row * CELL_SIZE;

                        int fluidBlockId = getFluidBlockId(entry.key.fluidType);
                        if (fluidBlockId > 0 && Block.blocksList[fluidBlockId] != null) {
                            ItemStack fluidStack = new ItemStack(fluidBlockId, 1, 0);
                            ri.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, fluidStack, ix, iy);
                        } else {
                            GL11.glDisable(GL11.GL_LIGHTING);
                            drawRect(ix + 1, iy + 1, ix + 15, iy + 15, entry.key.getColor());
                            GL11.glEnable(GL11.GL_LIGHTING);
                        }
                        renderFluidAmount(entry.amountMB, ix, iy);
                    }
                }
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();

            // Hover highlight (inside glTranslate)
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

        // Scrollbar thumb
        int totalRows = (fluids.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, totalRows - GRID_ROWS);
        if (maxScroll > 0) {
            int trackX = x + 172 + 3, trackY = y + 14 + 4;
            int trackW = 22 - 7, trackH = 134 - 8;
            int thumbH = Math.max(10, trackH * GRID_ROWS / totalRows);
            int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;
            drawRect(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xFFC6C6C6);
            drawRect(trackX, thumbY, trackX + trackW, thumbY + 1, 0xFFFFFFFF);
            drawRect(trackX, thumbY, trackX + 1, thumbY + thumbH, 0xFFFFFFFF);
            drawRect(trackX, thumbY + thumbH - 1, trackX + trackW, thumbY + thumbH, 0xFF555555);
            drawRect(trackX + trackW - 1, thumbY, trackX + trackW, thumbY + thumbH, 0xFF555555);
        }
    }

    private int getFluidBlockId(int fluidType) {
        switch (fluidType) {
            case Aero_FluidType.WATER: return 9; // waterStill
            default: return 0;
        }
    }

    private void renderFluidAmount(int mB, int x, int y) {
        if (mB <= 0) return;
        String text = formatAmount(mB);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        float scale = 0.60F;
        int textWidth = this.fontRenderer.getStringWidth(text);
        int tx = (int)((x + 16 - textWidth * scale) / scale);
        int ty = (int)((y + 11) / scale);
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);
        this.fontRenderer.drawStringWithShadow(text, tx, ty, 0xFFFFFF);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private String formatAmount(int mB) {
        if (mB >= 1000000) return (mB / 1000000) + "MB";
        if (mB >= 1000) {
            int b = mB / 1000;
            int frac = (mB % 1000) / 100;
            if (frac > 0 && b < 100) return b + "." + frac + "B";
            return b + "B";
        }
        return mB + "mB";
    }

    private void drawScrollbarTab(int tx, int ty, int tw, int th) {
        int BK = 0xFF000000, WH = 0xFFFFFFFF, BG = 0xFFC6C6C6, DK = 0xFF555555;
        int tr = tx + tw - 1, tb = ty + th - 1;
        for (int px = tx + 2; px <= tr - 3; px++) drawRect(px, ty, px + 1, ty + 1, BK);
        drawRect(tr - 2, ty + 1, tr - 1, ty + 2, BK);
        drawRect(tx + 1, ty + 1, tr - 2, ty + 2, WH);
        drawRect(tx + 1, ty + 2, tr - 2, ty + 3, WH);
        drawRect(tr - 2, ty + 2, tr - 1, ty + 3, BG);
        drawRect(tr - 1, ty + 2, tr, ty + 3, BK);
        drawRect(tx, ty + 3, tr - 2, ty + 4, BG);
        drawRect(tr - 2, ty + 3, tr, ty + 4, DK);
        drawRect(tr, ty + 3, tr + 1, ty + 4, BK);
        for (int py = ty + 4; py <= tb - 4; py++) {
            drawRect(tx, py, tr - 2, py + 1, BG);
            drawRect(tr - 2, py, tr, py + 1, DK);
            drawRect(tr, py, tr + 1, py + 1, BK);
        }
        drawRect(tx, tb - 3, tr - 3, tb - 2, BG);
        drawRect(tr - 3, tb - 3, tr, tb - 2, DK);
        drawRect(tr, tb - 3, tr + 1, tb - 2, BK);
        for (int px = tx + 1; px <= tr - 1; px++) drawRect(px, tb - 2, px + 1, tb - 1, DK);
        drawRect(tr, tb - 2, tr + 1, tb - 1, BK);
        for (int px = tx + 2; px <= tr - 2; px++) drawRect(px, tb - 1, px + 1, tb, DK);
        drawRect(tr - 1, tb - 1, tr, tb, BK);
        for (int px = tx + 3; px <= tr - 2; px++) drawRect(px, tb, px + 1, tb + 1, BK);
        int itx = tx + 3, ity = ty + 4, itw = tw - 7, ith = th - 8;
        drawRect(itx, ity, itx + itw - 1, ity + 1, 0xFF373737);
        drawRect(itx, ity, itx + 1, ity + ith - 1, 0xFF373737);
        drawRect(itx, ity + ith - 1, itx + itw, ity + ith, WH);
        drawRect(itx + itw - 1, ity, itx + itw, ity + ith, WH);
        drawRect(itx + 1, ity + 1, itx + itw - 1, ity + ith - 1, 0xFF8B8B8B);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // Search box click
        int searchBoxX = guiLeft + 97;
        int searchBoxY = guiTop + 4;
        if (mouseX >= searchBoxX && mouseX < searchBoxX + 72 && mouseY >= searchBoxY && mouseY < searchBoxY + 12) {
            searchFocused = true;
            return;
        } else {
            searchFocused = false;
        }

        // Bucket interaction on grid area
        int gridLeft = guiLeft + GRID_X;
        int gridTop = guiTop + GRID_Y;
        int gridRight = gridLeft + GRID_COLS * CELL_SIZE;
        int gridBottom = gridTop + GRID_ROWS * CELL_SIZE;

        if (mouseX >= gridLeft && mouseX < gridRight && mouseY >= gridTop && mouseY < gridBottom) {
            ItemStack held = this.mc.thePlayer.inventory.getItemStack();
            if (held != null) {
                containerFluid.handleBucketClick(held, this.mc.thePlayer);
                return;
            } else {
                // Click on fluid with empty hand — try to extract with clicked fluid
                int col = (mouseX - gridLeft) / CELL_SIZE;
                int row = (mouseY - gridTop) / CELL_SIZE;
                int index = (scrollOffset + row) * GRID_COLS + col;
                containerFluid.refreshFluids();
                java.util.List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();
                if (index >= 0 && index < fluids.size()) {
                    // Could implement extract-to-bucket here in future
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (searchFocused) {
            if (keyCode == 14 && searchText.length() > 0) {
                searchText = searchText.substring(0, searchText.length() - 1);
                scrollOffset = 0;
                return;
            } else if (keyCode == 1) {
                searchFocused = false;
                return;
            } else if (keyCode == 28) {
                searchFocused = false;
                return;
            } else if (c >= ' ' && c < 127) {
                searchText += c;
                scrollOffset = 0;
                return;
            }
        }
        super.keyTyped(c, keyCode);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0) {
            List<BE_ContainerFluidTerminal.BE_FluidEntry> fluids = containerFluid.getFluids();
            int totalRows = (fluids.size() + GRID_COLS - 1) / GRID_COLS;
            int maxScroll = Math.max(0, totalRows - GRID_ROWS);
            if (scroll < 0) {
                scrollOffset = Math.min(scrollOffset + 1, maxScroll);
            } else {
                scrollOffset = Math.max(scrollOffset - 1, 0);
            }
        }
    }
}
