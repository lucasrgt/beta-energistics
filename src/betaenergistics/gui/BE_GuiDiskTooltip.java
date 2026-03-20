package betaenergistics.gui;

import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_DiskStorage;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.Gui;
import net.minecraft.src.ItemStack;

/**
 * Shared disk tooltip renderer — multiline tooltip for storage disks.
 * Can be called from any GUI's drawGuiContainerForegroundLayer.
 */
public class BE_GuiDiskTooltip {

    public static void draw(Gui gui, FontRenderer font, ItemStack disk, int tx, int ty) {
        if (disk == null || !(disk.getItem() instanceof BE_ItemStorageDisk)) return;
        int dmg = disk.getItemDamage();

        String line1;
        String line2;
        String line3;

        if (BE_DiskRegistry.isRegistered(dmg)) {
            int tier = BE_DiskRegistry.getTier(dmg);
            BE_DiskStorage storage = BE_DiskRegistry.getDisk(dmg);
            if (storage == null) return;
            line1 = BE_ItemStorageDisk.getTierName(tier) + " Storage Disk";
            line2 = storage.getStored() + " / " + storage.getCapacity() + " items";
            line3 = storage.getTypeCount() + " / " + BE_DiskStorage.MAX_TYPES + " types";
        } else {
            int tier = dmg;
            line1 = BE_ItemStorageDisk.getTierName(tier) + " Storage Disk";
            line2 = "Empty";
            line3 = null;
        }

        int maxW = Math.max(font.getStringWidth(line1), font.getStringWidth(line2));
        if (line3 != null) maxW = Math.max(maxW, font.getStringWidth(line3));
        int h = line3 != null ? 35 : 24;

        // Background
        gui.drawGradientRect(tx - 3, ty - 4, tx + maxW + 3, ty + h, 0xF0100010, 0xF0100010);
        // Border
        gui.drawGradientRect(tx - 3, ty - 3, tx + maxW + 3, ty - 2, 0xFF5000AA, 0xFF28007F);
        gui.drawGradientRect(tx - 3, ty + h - 1, tx + maxW + 3, ty + h, 0xFF28007F, 0xFF5000AA);
        gui.drawGradientRect(tx - 4, ty - 2, tx - 3, ty + h - 1, 0xFF5000AA, 0xFF28007F);
        gui.drawGradientRect(tx + maxW + 3, ty - 2, tx + maxW + 4, ty + h - 1, 0xFF5000AA, 0xFF28007F);

        font.drawStringWithShadow(line1, tx, ty, 0xFFFFFF);
        font.drawStringWithShadow(line2, tx, ty + 12, 0xAAAAAA);
        if (line3 != null) {
            font.drawStringWithShadow(line3, tx, ty + 23, 0xAAAAAA);
        }
    }
}
