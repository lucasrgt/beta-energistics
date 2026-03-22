package betaenergistics.gui;

import betaenergistics.container.BE_ContainerRecipeEncoder;
import betaenergistics.tile.BE_TileRecipeEncoder;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * GUI for Recipe Encoder (176x196).
 *
 * Layout:
 *   3x3 ghost input grid at (30, 17)
 *   Arrow between grid and output
 *   Ghost output at (124, 35)
 *   Encode button at (138, 43) size 28x14
 *   Blank pattern input at (124, 71)
 *   Encoded pattern output at (124, 93)
 *   Craft/Process tabs on left side
 *   Player inventory at (8, 114) / hotbar at (8, 172)
 */
public class BE_GuiRecipeEncoder extends GuiContainer {
    private BE_TileRecipeEncoder encoder;
    private BE_ContainerRecipeEncoder containerEncoder;

    private int mouseX;
    private int mouseY;

    // Encode button bounds (relative to GUI top-left)
    private static final int BTN_X = 138;
    private static final int BTN_Y = 43;
    private static final int BTN_W = 28;
    private static final int BTN_H = 14;

    // Tab dimensions (left side, outside panel)
    private static final int TAB_W = 26;
    private static final int TAB_H = 16;
    private static final int TAB_GAP = 2;

    private static final RenderItem ghostItemRenderer = new RenderItem();

    public BE_GuiRecipeEncoder(InventoryPlayer playerInv, BE_TileRecipeEncoder encoder) {
        super(new BE_ContainerRecipeEncoder(playerInv, encoder));
        this.encoder = encoder;
        this.containerEncoder = (BE_ContainerRecipeEncoder) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 196;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        // Title
        this.fontRenderer.drawString("Recipe Encoder", 8, 6, 4210752);
        // Inventory label
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        // Arrow and encode button rendered in backgroundLayer via BE_GuiUtils
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw texture from Machine Maker
        int texId = this.mc.renderEngine.getTexture("/gui/be_recipe_encoder.png");
        this.mc.renderEngine.bindTexture(texId);
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Encode button (3D beveled via utils)
        BE_GuiUtils.drawButton(this.fontRenderer, x + BTN_X, y + BTN_Y, BTN_W, BTN_H, "Encode", mouseX, mouseY);

        // Craft/Process tabs on left side (same style as Grid Terminal)
        String[] tabLabels = new String[]{"Craft", "Process"};
        int activeTab = encoder.isProcessingMode() ? 1 : 0;
        for (int i = 0; i < tabLabels.length; i++) {
            int tabX = x - TAB_W;
            int tabY = y + 28 + i * (TAB_H + TAB_GAP);
            BE_GuiUtils.drawTabLeft(this.fontRenderer, tabX, tabY, TAB_W, TAB_H, tabLabels[i], i == activeTab);
        }

        // Render ghost slot items with translucent overlay
        renderGhostSlots(x, y);
    }

    /**
     * Render ghost items with 50% alpha to indicate they are references.
     */
    private void renderGhostSlots(int guiX, int guiY) {
        // Ghost inputs (0-8)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int idx = row * 3 + col;
                ItemStack ghost = encoder.getStackInSlot(idx);
                if (ghost != null) {
                    int sx = guiX + 30 + col * 18;
                    int sy = guiY + 17 + row * 18;
                    renderGhostItem(ghost, sx, sy);
                }
            }
        }
        // Ghost output (slot 9)
        ItemStack ghostOut = encoder.getGhostOutput();
        if (ghostOut != null) {
            renderGhostItem(ghostOut, guiX + 124, guiY + 35);
        }
    }

    private void renderGhostItem(ItemStack stack, int x, int y) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
        ghostItemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, x, y);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawSlotBackground(int sx, int sy) {
        drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);              // border
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);      // inner dark
        drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);       // top inner
        drawRect(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);       // left inner
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);      // fill
    }

    /**
     * Draw a tab on the left side of the GUI (same style as Grid Terminal sort tabs).
     */
    // Uses BE_GuiUtils.drawTabLeft for consistent tab rendering

    @Override
    public void drawScreen(int mx, int my, float partialTick) {
        this.mouseX = mx;
        this.mouseY = my;
        super.drawScreen(mx, my, partialTick);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        int relX = mouseX - x;
        int relY = mouseY - y;

        // Check Craft/Process tab clicks (left side, outside panel)
        for (int i = 0; i < 2; i++) {
            int tabX = -TAB_W;
            int tabY = 18 + i * (TAB_H + TAB_GAP);
            if (relX >= tabX && relX < tabX + TAB_W && relY >= tabY && relY < tabY + TAB_H) {
                // Only toggle if clicking the non-active tab
                int activeTab = encoder.isProcessingMode() ? 1 : 0;
                if (i != activeTab) {
                    containerEncoder.toggleMode();
                }
                return;
            }
        }

        // Check encode button click
        if (relX >= BTN_X && relX < BTN_X + BTN_W && relY >= BTN_Y && relY < BTN_Y + BTN_H) {
            containerEncoder.encode();
            return;
        }

        // Default handling (Container.func_27280_a handles ghost slot clicks)
        super.mouseClicked(mouseX, mouseY, button);
    }
}
