package betaenergistics.tile;

import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_IStorageProvider;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_DiskRegistry;
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
 * Uses BE_DiskRegistry for persistent disk data.
 *
 * When a blank disk (damage 0-3) is inserted, it gets registered
 * and its damage value changes to the unique disk ID.
 * When a registered disk (damage >= 10) is inserted, its existing
 * data is loaded from the registry.
 */
public class BE_TileDiskDrive extends TileEntity implements BE_INetworkNode, BE_IStorageProvider, IInventory {
    public static final int DISK_SLOTS = 8;
    private static final int ENERGY_USAGE = 4;

    private ItemStack[] diskSlots = new ItemStack[DISK_SLOTS];
    private BE_DiskStorage[] loadedStorages = new BE_DiskStorage[DISK_SLOTS];
    private BE_StorageNetwork network;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld) return;

        boolean changed = false;
        for (int i = 0; i < DISK_SLOTS; i++) {
            if (diskSlots[i] != null && diskSlots[i].getItem() instanceof BE_ItemStorageDisk) {
                int dmg = diskSlots[i].getItemDamage();

                if (BE_DiskRegistry.isBlank(dmg)) {
                    // Blank disk — register it and assign an ID
                    int tier = dmg;
                    int capacity = BE_ItemStorageDisk.getCapacity(tier);
                    int newId = BE_DiskRegistry.createDisk(tier, capacity);
                    diskSlots[i].setItemDamage(newId);
                    loadedStorages[i] = BE_DiskRegistry.getDisk(newId);
                    changed = true;
                } else if (BE_DiskRegistry.isRegistered(dmg)) {
                    // Registered disk — load from registry if not loaded
                    if (loadedStorages[i] == null) {
                        loadedStorages[i] = BE_DiskRegistry.getDisk(dmg);
                        changed = true;
                    }
                } else {
                    // Unknown damage value — ignore
                    if (loadedStorages[i] != null) {
                        loadedStorages[i] = null;
                        changed = true;
                    }
                }
            } else {
                if (loadedStorages[i] != null) {
                    loadedStorages[i] = null;
                    changed = true;
                }
            }
        }

        if (changed && network != null) {
            network.rebuildStorage();
        }
    }

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
        ItemStack stack = diskSlots[slot];
        diskSlots[slot] = null;
        loadedStorages[slot] = null;
        onInventoryChanged();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        diskSlots[slot] = stack;
        loadedStorages[slot] = null; // will be loaded on next updateEntity tick
        onInventoryChanged();
    }

    @Override
    public String getInvName() { return "BE Disk Drive"; }
    @Override
    public int getInventoryStackLimit() { return 1; }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void onInventoryChanged() {
        super.onInventoryChanged();
        // Immediately sync loaded storages when inventory changes
        for (int i = 0; i < DISK_SLOTS; i++) {
            if (diskSlots[i] == null || !(diskSlots[i].getItem() instanceof BE_ItemStorageDisk)) {
                loadedStorages[i] = null;
            }
        }
        if (network != null) {
            network.rebuildStorage();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList diskList = tag.getTagList("disks");
        if (diskList != null) {
            for (int i = 0; i < diskList.tagCount() && i < DISK_SLOTS; i++) {
                NBTTagCompound slotTag = (NBTTagCompound) diskList.tagAt(i);
                if (slotTag.hasKey("id")) {
                    diskSlots[i] = new ItemStack(slotTag);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList diskList = new NBTTagList();
        for (int i = 0; i < DISK_SLOTS; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (diskSlots[i] != null) {
                diskSlots[i].writeToNBT(slotTag);
            }
            diskList.setTag(slotTag);
        }
        tag.setTag("disks", diskList);
        // Mark registry dirty so it saves on next Controller tick
        BE_DiskRegistry.markDirty();
    }
}
