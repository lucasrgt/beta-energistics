package betaenergistics.gui;

import betaenergistics.container.BE_ContainerDiskDrive;
import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_DiskStorage;
import betaenergistics.storage.BE_StorageState;
import betaenergistics.tile.BE_TileDiskDrive;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class BE_GuiDiskDrive extends GuiContainer {
    private BE_TileDiskDrive drive;
    private int mouseX;
    private int mouseY;

    public BE_GuiDiskDrive(InventoryPlayer playerInv, BE_TileDiskDrive drive) {
        super(new BE_ContainerDiskDrive(playerInv, drive));
        this.drive = drive;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        this.fontRenderer.drawString("Disk Drive", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);

        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int relX = this.mouseX - guiLeft;
        int relY = this.mouseY - guiTop;

        // Draw custom tooltip over disk slots (renders ON TOP of native tooltip)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int slotX = 61 + col * 18;
                int slotY = 16 + row * 18;
                if (relX >= slotX && relX < slotX + 18 && relY >= slotY && relY < slotY + 18) {
                    int slotIndex = row * 2 + col;
                    ItemStack disk = drive.getStackInSlot(slotIndex);
                    if (disk != null && disk.getItem() instanceof BE_ItemStorageDisk) {
                        drawDiskTooltip(disk, relX + 12, relY - 12);
                    }
                }
            }
        }
    }

    private void drawDiskTooltip(ItemStack disk, int tx, int ty) {
        int dmg = disk.getItemDamage();
        String line1;
        String line2;
        String line3;

        if (BE_DiskRegistry.isRegistered(dmg)) {
            int tier = BE_DiskRegistry.getTier(dmg);
            BE_DiskStorage storage = BE_DiskRegistry.getDisk(dmg);
            if (storage == null) return;
            line1 = BE_ItemStorageDisk.getTierName(tier) + " Storage Disk";
            line2 = storage.getStored() + " / " + storage.getCapacity() + " items stored";
            line3 = storage.getTypeCount() + " / " + BE_DiskStorage.MAX_TYPES + " types used";
        } else {
            line1 = BE_ItemStorageDisk.getTierName(dmg) + " Storage Disk";
            line2 = "Empty — insert into drive to initialize";
            line3 = null;
        }

        int maxW = Math.max(this.fontRenderer.getStringWidth(line1),
            this.fontRenderer.getStringWidth(line2));
        if (line3 != null) maxW = Math.max(maxW, this.fontRenderer.getStringWidth(line3));
        int height = line3 != null ? 33 : 22;

        // Background (overwrites the native tooltip beneath)
        this.drawGradientRect(tx - 3, ty - 3, tx + maxW + 3, ty + height, 0xF0100010, 0xF0100010);
        // Purple border like vanilla tooltip
        this.drawGradientRect(tx - 3, ty - 3, tx + maxW + 3, ty - 2, 0xFF5000AA, 0xFF5000AA);
        this.drawGradientRect(tx - 3, ty + height - 1, tx + maxW + 3, ty + height, 0xFF5000AA, 0xFF5000AA);
        this.drawGradientRect(tx - 4, ty - 2, tx - 3, ty + height - 1, 0xFF5000AA, 0xFF5000AA);
        this.drawGradientRect(tx + maxW + 3, ty - 2, tx + maxW + 4, ty + height - 1, 0xFF5000AA, 0xFF5000AA);

        this.fontRenderer.drawStringWithShadow(line1, tx, ty, 0xFFFFFF);
        this.fontRenderer.drawStringWithShadow(line2, tx, ty + 11, 0xAAAAAA);
        if (line3 != null) {
            this.fontRenderer.drawStringWithShadow(line3, tx, ty + 22, 0xAAAAAA);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        drawRect(x, y, x + this.xSize, y + this.ySize, 0xFFC6C6C6);
        drawRect(x + 7, y + 83, x + 169, y + 159, 0xFF8B8B8B);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int slotX = x + 61 + col * 18;
                int slotY = y + 16 + row * 18;
                drawRect(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
                drawRect(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF8B8B8B);

                int slotIndex = row * 2 + col;
                BE_StorageState state = drive.getDiskState(slotIndex);
                drawRect(slotX - 4, slotY + 5, slotX - 1, slotY + 12, getLedColor(state));
            }
        }
    }

    private int getLedColor(BE_StorageState state) {
        switch (state) {
            case EMPTY:         return 0xFF00CC00;
            case NORMAL:        return 0xFF00CC00;
            case NEAR_CAPACITY: return 0xFFFFAA00;
            case FULL:          return 0xFFFF0000;
            default:            return 0xFF444444;
        }
    }
}
