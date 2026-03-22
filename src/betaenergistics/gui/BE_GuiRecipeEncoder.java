package betaenergistics.gui;

import betaenergistics.container.BE_ContainerRecipeEncoder;
import betaenergistics.tile.BE_TileRecipeEncoder;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * GUI for Recipe Encoder (176x196).
 *
 * Layout (from Machine Maker texture):
 *   3x3 ghost input grid: slots at (14,29) spacing 18px
 *   Progress arrow at (75,45) 24x17
 *   Big ghost output slot at (107,40) 26x26, item at (112,45)
 *   Encode label at (139,44)
 *   Blank pattern input at (146,29)
 *   Encoded pattern output at (146,65)
 *   Craft/Process icon tabs on left side (24x24)
 *   Player inventory at (8,114) / hotbar at (8,172)
 */
public class BE_GuiRecipeEncoder extends GuiContainer {
    private BE_TileRecipeEncoder encoder;
    private BE_ContainerRecipeEncoder containerEncoder;

    private int mouseX;
    private int mouseY;

    // Square icon tabs (left side, outside panel)
    private static final int TAB_SIZE = 24;
    private static final int TAB_GAP = 2;
    private static final int TAB_Y_START = 28;

    // Terrain.png tile indices for tab icons
    private static final int ICON_CRAFT = 43;    // crafting table top
    private static final int ICON_PROCESS = 44;  // furnace front

    public BE_GuiRecipeEncoder(InventoryPlayer playerInv, BE_TileRecipeEncoder encoder) {
        super(new BE_ContainerRecipeEncoder(playerInv, encoder));
        this.encoder = encoder;
        this.containerEncoder = (BE_ContainerRecipeEncoder) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 196;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        String title = encoder.isProcessingMode() ? "Recipe Encoder - Process" : "Recipe Encoder - Craft";
        this.fontRenderer.drawString(title, 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Craft/Process icon tabs (drawn first so panel overlaps the right edge)
        int activeTab = encoder.isProcessingMode() ? 1 : 0;
        int[] icons = { ICON_CRAFT, ICON_PROCESS };
        for (int i = 0; i < 2; i++) {
            int tabX = x - TAB_SIZE + 2;
            int tabY = y + TAB_Y_START + i * (TAB_SIZE + TAB_GAP);

            BE_GuiUtils.drawTabLeft(tabX, tabY, TAB_SIZE, TAB_SIZE, i == activeTab);

            int iconU = (icons[i] % 16) * 16;
            int iconV = (icons[i] / 16) * 16;
            int terrainId = this.mc.renderEngine.getTexture("/terrain.png");
            this.mc.renderEngine.bindTexture(terrainId);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(tabX + 4, tabY + 4, iconU, iconV, 16, 16);
        }

        // Draw GUI texture (on top of tabs, hiding their right edge)
        int texId = this.mc.renderEngine.getTexture("/gui/be_recipe_encoder.png");
        this.mc.renderEngine.bindTexture(texId);
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Down arrow between pattern input and output slots (centered in 18px gap)
        int arrowColor = 0xFF808080;
        int ac = x + 153;
        // Shaft (3px wide, 9px tall)
        drawRect(ac - 1, y + 48, ac + 2, y + 56, arrowColor);
        // Arrowhead (wide to 1px point)
        drawRect(ac - 4, y + 56, ac + 5, y + 57, arrowColor);
        drawRect(ac - 3, y + 57, ac + 4, y + 58, arrowColor);
        drawRect(ac - 2, y + 58, ac + 3, y + 59, arrowColor);
        drawRect(ac - 1, y + 59, ac + 2, y + 60, arrowColor);
        drawRect(ac,     y + 60, ac + 1, y + 61, arrowColor);

        // Clear recipe mini button (top-right of 3x3 grid)
        int cx = x + 69, cy = y + 28;
        boolean clearHover = mouseX >= cx && mouseX < cx + 9 && mouseY >= cy && mouseY < cy + 9;
        int clearFill = clearHover ? 0xFF7B7B7B : 0xFF6C6C6C;
        // Box
        drawRect(cx, cy, cx + 9, cy + 1, 0xFF000000);
        drawRect(cx, cy + 8, cx + 9, cy + 9, 0xFF000000);
        drawRect(cx, cy, cx + 1, cy + 9, 0xFF000000);
        drawRect(cx + 8, cy, cx + 9, cy + 9, 0xFF000000);
        drawRect(cx + 1, cy + 1, cx + 8, cy + 8, clearFill);
        drawRect(cx + 1, cy + 1, cx + 8, cy + 2, clearHover ? 0xFFAAAAAA : 0xFF9A9A9A);
        drawRect(cx + 1, cy + 1, cx + 2, cy + 8, clearHover ? 0xFFAAAAAA : 0xFF9A9A9A);
        drawRect(cx + 1, cy + 7, cx + 8, cy + 8, 0xFF555555);
        drawRect(cx + 7, cy + 1, cx + 8, cy + 8, 0xFF555555);
        // X mark (5x5 centered in 7x7 inner area)
        int xc = clearHover ? 0xFFFFA0A0 : 0xFFE0E0E0;
        drawRect(cx + 2, cy + 2, cx + 3, cy + 3, xc);
        drawRect(cx + 6, cy + 2, cx + 7, cy + 3, xc);
        drawRect(cx + 3, cy + 3, cx + 4, cy + 4, xc);
        drawRect(cx + 5, cy + 3, cx + 6, cy + 4, xc);
        drawRect(cx + 4, cy + 4, cx + 5, cy + 5, xc);
        drawRect(cx + 3, cy + 5, cx + 4, cy + 6, xc);
        drawRect(cx + 5, cy + 5, cx + 6, cy + 6, xc);
        drawRect(cx + 2, cy + 6, cx + 3, cy + 7, xc);
        drawRect(cx + 6, cy + 6, cx + 7, cy + 7, xc);

        // Encode button (below pattern output slot)
        BE_GuiUtils.drawButton(this.fontRenderer, x + 119, y + 85, 44, 16, "Encode", mouseX, mouseY);
    }

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
            int tabX = -TAB_SIZE + 2;
            int tabY = TAB_Y_START + i * (TAB_SIZE + TAB_GAP);
            if (relX >= tabX && relX < tabX + TAB_SIZE && relY >= tabY && relY < tabY + TAB_SIZE) {
                int activeTab = encoder.isProcessingMode() ? 1 : 0;
                if (i != activeTab) {
                    containerEncoder.toggleMode();
                }
                return;
            }
        }

        // Check clear recipe button click (9x9 at 68,19)
        if (relX >= 69 && relX < 78 && relY >= 28 && relY < 37) {
            containerEncoder.clearRecipe();
            return;
        }

        // Check Encode button click
        if (relX >= 119 && relX < 163 && relY >= 85 && relY < 101) {
            containerEncoder.encode();
            return;
        }

        // Default handling (Container.func_27280_a handles ghost slot clicks)
        super.mouseClicked(mouseX, mouseY, button);
    }
}
