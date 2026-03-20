package betaenergistics.gui;

import betaenergistics.container.BE_ContainerDiskDrive;
import betaenergistics.storage.BE_StorageState;
import betaenergistics.tile.BE_TileDiskDrive;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class BE_GuiDiskDrive extends GuiContainer {
    private BE_TileDiskDrive drive;

    public BE_GuiDiskDrive(InventoryPlayer playerInv, BE_TileDiskDrive drive) {
        super(new BE_ContainerDiskDrive(playerInv, drive));
        this.drive = drive;
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Disk Drive", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Simple background (TODO: proper texture)
        drawRect(x, y, x + this.xSize, y + this.ySize, 0xFFC6C6C6);
        drawRect(x + 7, y + 83, x + 169, y + 159, 0xFF8B8B8B);

        // Draw disk slot backgrounds with LED indicators
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int slotX = x + 61 + col * 18;
                int slotY = y + 16 + row * 18;
                drawRect(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
                drawRect(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF8B8B8B);

                // LED indicator
                int slotIndex = row * 2 + col;
                BE_StorageState state = drive.getDiskState(slotIndex);
                int ledColor = getLedColor(state);
                int ledX = slotX - 4;
                int ledY = slotY + 5;
                drawRect(ledX, ledY, ledX + 3, ledY + 7, ledColor);
            }
        }
    }

    private int getLedColor(BE_StorageState state) {
        switch (state) {
            case EMPTY:         return 0xFF00CC00; // green
            case NORMAL:        return 0xFF00CC00; // green
            case NEAR_CAPACITY: return 0xFFFFAA00; // orange
            case FULL:          return 0xFFFF0000; // red
            default:            return 0xFF444444; // dark gray (inactive)
        }
    }
}
