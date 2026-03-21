package betaenergistics.item;

import betaenergistics.storage.BE_GasDiskRegistry;

import net.minecraft.src.*;

public class BE_ItemGasDisk extends Item {
    public BE_ItemGasDisk(int id) {
        super(id);
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        setItemName("beGasDisk");
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        int dmg = stack.getItemDamage();
        // Blank tiers: damage 0-3
        if (dmg < 4) {
            return "beGasDisk" + dmg;
        }
        // Registered disk: show tier name from capacity
        if (BE_GasDiskRegistry.isRegistered(dmg)) {
            int cap = BE_GasDiskRegistry.getStorage(dmg).getCapacity();
            if (cap <= 8000) return "beGasDisk0";
            if (cap <= 32000) return "beGasDisk1";
            if (cap <= 128000) return "beGasDisk2";
            return "beGasDisk3";
        }
        return "beGasDisk0";
    }

    public static int getTier(ItemStack stack) {
        int dmg = stack.getItemDamage();
        if (dmg < 4) return dmg;
        if (BE_GasDiskRegistry.isRegistered(dmg)) {
            int cap = BE_GasDiskRegistry.getStorage(dmg).getCapacity();
            if (cap <= 8000) return 0;
            if (cap <= 32000) return 1;
            if (cap <= 128000) return 2;
            return 3;
        }
        return 0;
    }
}
