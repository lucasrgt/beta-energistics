package betaenergistics.tile;

import betaenergistics.item.BE_ItemFluidDisk;
import betaenergistics.item.BE_ItemGasDisk;
import betaenergistics.item.BE_ItemStorageDisk;
import betaenergistics.network.BE_IFluidStorageProvider;
import betaenergistics.network.BE_IGasStorageProvider;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_IStorageProvider;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_DiskStorage;
import betaenergistics.storage.BE_FluidDiskRegistry;
import betaenergistics.storage.BE_FluidDiskStorage;
import betaenergistics.storage.BE_GasDiskRegistry;
import betaenergistics.storage.BE_GasDiskStorage;
import betaenergistics.storage.BE_IFluidStorage;
import betaenergistics.storage.BE_IGasStorage;
import betaenergistics.storage.BE_IStorage;
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
 * When a blank disk (damage 0-5) is inserted, it gets registered
 * and its damage value changes to the unique disk ID.
 * When a registered disk (damage >= 10) is inserted, its existing
 * data is loaded from the registry.
 */
public class BE_TileDiskDrive extends TileEntity implements BE_INetworkNode, BE_IStorageProvider, BE_IFluidStorageProvider, BE_IGasStorageProvider, IInventory {
    public static final int DISK_SLOTS = 8;
    private static final int ENERGY_USAGE = 4;

    private ItemStack[] diskSlots = new ItemStack[DISK_SLOTS];
    private BE_DiskStorage[] loadedStorages = new BE_DiskStorage[DISK_SLOTS];
    private BE_FluidDiskStorage[] loadedFluidStorages = new BE_FluidDiskStorage[DISK_SLOTS];
    private BE_GasDiskStorage[] loadedGasStorages = new BE_GasDiskStorage[DISK_SLOTS];
    private BE_StorageNetwork network;
    private int priority = 0;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld) return;

        boolean changed = false;
        for (int i = 0; i < DISK_SLOTS; i++) {
            if (diskSlots[i] != null && diskSlots[i].getItem() instanceof BE_ItemStorageDisk) {
                int dmg = diskSlots[i].getItemDamage();

                if (BE_DiskRegistry.isBlank(dmg)) {
                    int tier = dmg;
                    int capacity = BE_ItemStorageDisk.getCapacity(tier);
                    int newId = BE_DiskRegistry.createDisk(tier, capacity);
                    diskSlots[i].setItemDamage(newId);
                    loadedStorages[i] = BE_DiskRegistry.getDisk(newId);
                    loadedFluidStorages[i] = null;
                    changed = true;
                } else if (BE_DiskRegistry.isRegistered(dmg)) {
                    if (loadedStorages[i] == null) {
                        loadedStorages[i] = BE_DiskRegistry.getDisk(dmg);
                        loadedFluidStorages[i] = null;
                        changed = true;
                    }
                } else {
                    if (loadedStorages[i] != null) {
                        loadedStorages[i] = null;
                        changed = true;
                    }
                }
            } else if (diskSlots[i] != null && diskSlots[i].getItem() instanceof BE_ItemFluidDisk) {
                int dmg = diskSlots[i].getItemDamage();

                if (BE_FluidDiskRegistry.isBlank(dmg)) {
                    int tier = dmg;
                    int capacity = BE_ItemFluidDisk.getCapacity(tier);
                    int newId = BE_FluidDiskRegistry.createDisk(tier, capacity);
                    diskSlots[i].setItemDamage(newId);
                    loadedFluidStorages[i] = BE_FluidDiskRegistry.getDisk(newId);
                    loadedStorages[i] = null;
                    changed = true;
                } else if (BE_FluidDiskRegistry.isRegistered(dmg)) {
                    if (loadedFluidStorages[i] == null) {
                        loadedFluidStorages[i] = BE_FluidDiskRegistry.getDisk(dmg);
                        loadedStorages[i] = null;
                        changed = true;
                    }
                } else {
                    if (loadedFluidStorages[i] != null) {
                        loadedFluidStorages[i] = null;
                        changed = true;
                    }
                }
            } else if (diskSlots[i] != null && diskSlots[i].getItem() instanceof BE_ItemGasDisk) {
                int dmg = diskSlots[i].getItemDamage();

                if (dmg < 4) { // blank gas disk
                    int tier = dmg;
                    int newId = BE_GasDiskRegistry.assignId(tier);
                    diskSlots[i].setItemDamage(newId);
                    loadedGasStorages[i] = BE_GasDiskRegistry.getStorage(newId);
                    loadedStorages[i] = null;
                    loadedFluidStorages[i] = null;
                    changed = true;
                } else if (BE_GasDiskRegistry.isRegistered(dmg)) {
                    if (loadedGasStorages[i] == null) {
                        loadedGasStorages[i] = BE_GasDiskRegistry.getStorage(dmg);
                        loadedStorages[i] = null;
                        loadedFluidStorages[i] = null;
                        changed = true;
                    }
                } else {
                    if (loadedGasStorages[i] != null) {
                        loadedGasStorages[i] = null;
                        changed = true;
                    }
                }
            } else {
                if (loadedStorages[i] != null || loadedFluidStorages[i] != null || loadedGasStorages[i] != null) {
                    loadedStorages[i] = null;
                    loadedFluidStorages[i] = null;
                    loadedGasStorages[i] = null;
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

    public int getPriority() { return priority; }

    public void setPriority(int priority) {
        this.priority = priority;
        for (BE_DiskStorage s : loadedStorages) {
            if (s != null) s.setPriority(priority);
        }
        for (BE_FluidDiskStorage s : loadedFluidStorages) {
            if (s != null) s.setPriority(priority);
        }
        for (BE_GasDiskStorage s : loadedGasStorages) {
            if (s != null) s.setPriority(priority);
        }
        if (network != null) {
            network.rebuildStorage();
        }
    }

    // BE_IStorageProvider
    @Override
    public List<BE_IStorage> getStorages() {
        List<BE_IStorage> list = new ArrayList<BE_IStorage>();
        for (BE_DiskStorage s : loadedStorages) {
            if (s != null) {
                s.setPriority(priority);
                list.add(s);
            }
        }
        return list;
    }

    // BE_IFluidStorageProvider
    @Override
    public List<BE_IFluidStorage> getFluidStorages() {
        List<BE_IFluidStorage> list = new ArrayList<BE_IFluidStorage>();
        for (BE_FluidDiskStorage s : loadedFluidStorages) {
            if (s != null) {
                s.setPriority(priority);
                list.add(s);
            }
        }
        return list;
    }

    // BE_IGasStorageProvider
    @Override
    public List<BE_IGasStorage> getGasStorages() {
        List<BE_IGasStorage> list = new ArrayList<BE_IGasStorage>();
        for (BE_GasDiskStorage s : loadedGasStorages) {
            if (s != null) {
                s.setPriority(priority);
                list.add(s);
            }
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
        for (int i = 0; i < DISK_SLOTS; i++) {
            if (diskSlots[i] == null) {
                loadedStorages[i] = null;
                loadedFluidStorages[i] = null;
            } else if (!(diskSlots[i].getItem() instanceof BE_ItemStorageDisk)) {
                loadedStorages[i] = null;
            } else if (!(diskSlots[i].getItem() instanceof BE_ItemFluidDisk)) {
                loadedFluidStorages[i] = null;
            }
        }
        if (network != null) {
            network.rebuildStorage();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        priority = tag.getInteger("priority");
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
        tag.setInteger("priority", priority);
        NBTTagList diskList = new NBTTagList();
        for (int i = 0; i < DISK_SLOTS; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (diskSlots[i] != null) {
                diskSlots[i].writeToNBT(slotTag);
            }
            diskList.setTag(slotTag);
        }
        tag.setTag("disks", diskList);
        // Mark registries dirty so they save on next Controller tick
        BE_DiskRegistry.markDirty();
        BE_FluidDiskRegistry.markDirty();
    }
}
