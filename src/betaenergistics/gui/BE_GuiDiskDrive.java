package betaenergistics.gui;

import betaenergistics.container.BE_ContainerDiskDrive;
import betaenergistics.tile.BE_TileDiskDrive;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class BE_GuiDiskDrive extends GuiContainer {
    private static final String TEXTURE = "/gui/be_disk_drive.png";
    private BE_TileDiskDrive drive;

    public BE_GuiDiskDrive(InventoryPlayer playerInv, BE_TileDiskDrive drive) {
        super(new BE_ContainerDiskDrive(playerInv, drive));
        this.drive = drive;
        this.ySize = 196;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Disk Drive", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int texId = this.mc.renderEngine.getTexture(TEXTURE);
        this.mc.renderEngine.bindTexture(texId);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }
}
