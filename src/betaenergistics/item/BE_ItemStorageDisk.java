package betaenergistics.item;

import betaenergistics.storage.BE_DiskStorage;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

/**
 * Storage disk item — holds digital items when inserted into a Disk Drive.
 * 4 tiers: 1K, 4K, 16K, 64K (damage values 0-3).
 */
public class BE_ItemStorageDisk extends Item {
    public static final int TIER_1K = 0;
    public static final int TIER_4K = 1;
    public static final int TIER_16K = 2;
    public static final int TIER_64K = 3;

    private static final int[] CAPACITIES = {1024, 4096, 16384, 65536};
    private static final String[] TIER_NAMES = {"1K", "4K", "16K", "64K"};

    public BE_ItemStorageDisk(int itemId) {
        super(itemId);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
    }

    public static int getCapacity(int tier) {
        if (tier < 0 || tier >= CAPACITIES.length) return CAPACITIES[0];
        return CAPACITIES[tier];
    }

    public static String getTierName(int tier) {
        if (tier < 0 || tier >= TIER_NAMES.length) return TIER_NAMES[0];
        return TIER_NAMES[tier];
    }

    /**
     * Create a DiskStorage from this item stack's NBT data.
     * If no NBT exists, creates an empty disk.
     */
    public static BE_DiskStorage createStorage(ItemStack stack) {
        int tier = stack.getItemDamage();
        BE_DiskStorage storage = new BE_DiskStorage(getCapacity(tier));

        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("disk")) {
            storage.readFromNBT(stack.stackTagCompound.getCompoundTag("disk"));
        }
        return storage;
    }

    /**
     * Save a DiskStorage back to an item stack's NBT.
     */
    public static void saveStorage(ItemStack stack, BE_DiskStorage storage) {
        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        }
        NBTTagCompound diskTag = new NBTTagCompound();
        storage.writeToNBT(diskTag);
        stack.stackTagCompound.setCompoundTag("disk", diskTag);
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        return "BE Storage Disk " + getTierName(stack.getItemDamage());
    }

    @Override
    public int getIconFromDamage(int damage) {
        // TODO: different icons per tier
        return 0;
    }
}
