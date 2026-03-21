package betaenergistics.gui;

import betaenergistics.container.BE_ContainerDiskDrive;
import betaenergistics.item.BE_IDisk;
import betaenergistics.tile.BE_TileDiskDrive;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class BE_GuiDiskDrive extends GuiContainer {
    private static final String TEXTURE = "/gui/be_disk_drive.png";
    private BE_TileDiskDrive drive;
    private int screenMouseX, screenMouseY;

    public BE_GuiDiskDrive(InventoryPlayer playerInv, BE_TileDiskDrive drive) {
        super(new BE_ContainerDiskDrive(playerInv, drive));
        this.drive = drive;
        this.ySize = 196;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.screenMouseX = mouseX;
        this.screenMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.controlList.add(new GuiButton(0, x + 116, y + 58, 12, 12, "-"));
        this.controlList.add(new GuiButton(1, x + 152, y + 58, 12, 12, "+"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            drive.setPriority(drive.getPriority() - 1);
        } else if (button.id == 1) {
            drive.setPriority(drive.getPriority() + 1);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Disk Drive", 8, 6, 4210752);
        String priorityText = "P:" + drive.getPriority();
        int textWidth = this.fontRenderer.getStringWidth(priorityText);
        this.fontRenderer.drawString(priorityText, 128 + (24 - textWidth) / 2, 60, 4210752);
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
