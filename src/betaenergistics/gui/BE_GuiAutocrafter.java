package betaenergistics.gui;

import betaenergistics.tile.BE_TileAutocrafter;
import betaenergistics.container.BE_ContainerAutocrafter;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class BE_GuiAutocrafter extends GuiContainer {

    private BE_TileAutocrafter tile;

    public BE_GuiAutocrafter(InventoryPlayer playerInv, BE_TileAutocrafter tile) {
        super(new BE_ContainerAutocrafter(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 200;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        fontRenderer.drawString("Autocrafter", (xSize - fontRenderer.getStringWidth("Autocrafter")) / 2, 6, 4210752);
        fontRenderer.drawString("Patterns", 8, 74, 4210752);
        fontRenderer.drawString("Inventory", 7, ySize - 96 + 2, 4210752);

        // Crafting status
        if (tile.isCrafting()) {
            int idx = tile.getActiveCraftIndex();
            ItemStack output = tile.getPatternOutput(idx);
            if (output != null) {
                String name = StringTranslate.getInstance().translateNamedKey(output.getItem().getItemName());
                String status = "Crafting: " + name;
                if (status.length() > 20) status = status.substring(0, 18) + "..";
                fontRenderer.drawString(status, xSize - 8 - fontRenderer.getStringWidth(status), 6, 0xFF44CC44);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int texId = this.mc.renderEngine.getTexture("/gui/be_autocrafter.png");
        this.mc.renderEngine.bindTexture(texId);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // Progress arrow fill
        if (tile.isCrafting()) {
            int progress = tile.getCraftProgressScaled(24);
            this.drawTexturedModalRect(x + 90, y + 35, 176, 0, progress, 17);
        }
    }
}
