package betaenergistics.tile;

import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_IStorageProvider;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_DiskStorage;
import betaenergistics.storage.BE_StorageState;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Disk Drive — accepts up to 6 storage disks.
 * Provides their storage to the network.
 */
public class BE_TileDiskDrive extends TileEntity implements BE_INetworkNode, BE_IStorageProvider, IInventory {
    public static final int DISK_SLOTS = 6;
    private static final int ENERGY_USAGE = 4; // EU/tick

    private ItemStack[] diskSlots = new ItemStack[DISK_SLOTS];
    private BE_DiskStorage[] loadedStorages = new BE_DiskStorage[DISK_SLOTS];
    private BE_StorageNetwork network;

    @Override
    public void updateEntity() {
        // Sync disk items to loaded storages
        for (int i = 0; i < DISK_SLOTS; i++) {
            if (diskSlots[i] != null && diskSlots[i].getItem() instanceof BE_ItemStorageDisk) {
                if (loadedStorages[i] == null) {
                    loadedStorages[i] = BE_ItemStorageDisk.createStorage(diskSlots[i]);
                    if (network != null) network.rebuildStorage();
                }
            } else {
                if (loadedStorages[i] != null) {
                    // Save back to item before unloading
                    if (diskSlots[i] != null) {
                        BE_ItemStorageDisk.saveStorage(diskSlots[i], loadedStorages[i]);
                    }
                    loadedStorages[i] = null;
                    if (network != null) network.rebuildStorage();
                }
            }
        }
    }

    /** Save all loaded storages back to their disk items. */
    public void saveDisksToDiskItems() {
        for (int i = 0; i < DISK_SLOTS; i++) {
            if (loadedStorages[i] != null && diskSlots[i] != null) {
                BE_ItemStorageDisk.saveStorage(diskSlots[i], loadedStorages[i]);
            }
        }
    }

    /** Get the visual state of a disk slot (for rendering LED colors). */
    public BE_StorageState getDiskState(int slot) {
        if (network == null || !network.isActive()) return BE_StorageState.INACTIVE;
        if (loadedStorages[slot] == null) return BE_StorageState.INACTIVE;
        return loadedStorages[slot].getState();
    }

    // BE_IStorageProvider
    @Override
    public List<BE_DiskStorage> getStorages() {
        List<BE_DiskStorage> list = new ArrayList<BE_DiskStorage>();
        for (BE_DiskStorage s : loadedStorages) {
            if (s != null) list.add(s);
        }
        return list;
    }

    // BE_INetworkNode
    @Override
    public int getEnergyUsage() { return ENERGY_USAGE; }

    @Override
    public void onNetworkJoin(BE_StorageNetwork network) { this.network = network; }

    @Override
    public void onNetworkLeave() { this.network = null; }

    @Override
    public BE_StorageNetwork getNetwork() { return network; }

    @Override
    public TileEntity getTileEntity() { return this; }

    @Override
    public boolean canConnectOnSide(int side) { return true; }

    // IInventory
    @Override
    public int getSizeInventory() { return DISK_SLOTS; }

    @Override
    public ItemStack getStackInSlot(int slot) { return diskSlots[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (diskSlots[slot] == null) return null;
        if (diskSlots[slot].stackSize <= amount) {
            ItemStack stack = diskSlots[slot];
            diskSlots[slot] = null;
            onInventoryChanged();
            return stack;
        }
        ItemStack split = diskSlots[slot].splitStack(amount);
        onInventoryChanged();
        return split;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = diskSlots[slot];
        diskSlots[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        diskSlots[slot] = stack;
        onInventoryChanged();
    }

    @Override
    public String getInvName() { return "BE Disk Drive"; }

    @Override
    public int getInventoryStackLimit() { return 1; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void openChest() {}

    @Override
    public void closeChest() { saveDisksToDiskItems(); }

    // NBT
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("disks");
        for (int i = 0; i < list.tagCount() && i < DISK_SLOTS; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) list.tagAt(i);
            if (slotTag.hasKey("id")) {
                diskSlots[i] = ItemStack.loadItemStackFromNBT(slotTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        saveDisksToDiskItems();
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < DISK_SLOTS; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (diskSlots[i] != null) {
                diskSlots[i].writeToNBT(slotTag);
            }
            list.tagList.add(slotTag);
        }
        tag.setTag("disks", list);
    }
}
