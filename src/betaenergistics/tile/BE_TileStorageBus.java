package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_IStorageProvider;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_AccessMode;
import betaenergistics.storage.BE_ExternalStorage;
import betaenergistics.storage.BE_IStorage;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Storage Bus — exposes an adjacent IInventory (chest, furnace, etc.) to the network.
 * Uses BE_ExternalStorage to wrap the adjacent inventory.
 * Configurable priority and access mode.
 */
public class BE_TileStorageBus extends TileEntity implements BE_INetworkNode, BE_IStorageProvider {
    private static final int ENERGY_USAGE = 3;

    private BE_StorageNetwork network;
    private int priority = 0;
    private BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;
    private BE_ExternalStorage cachedStorage;

    @Override
    public void updateEntity() {
        // Nothing to do per-tick — storage is rebuilt on demand
    }

    /**
     * Called when an adjacent block changes. Re-cache the external storage.
     */
    public void onAdjacentChanged() {
        cachedStorage = null;
        if (network != null) {
            network.rebuildStorage();
        }
    }

    private IInventory findAdjacentInventory() {
        int[][] offsets = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
        for (int[] off : offsets) {
            TileEntity te = worldObj.getBlockTileEntity(xCoord + off[0], yCoord + off[1], zCoord + off[2]);
            if (te instanceof IInventory && !(te instanceof BE_INetworkNode)) {
                return (IInventory) te;
            }
        }
        return null;
    }

    private BE_ExternalStorage getOrCreateStorage() {
        if (cachedStorage != null) return cachedStorage;
        IInventory inv = findAdjacentInventory();
        if (inv == null) return null;
        cachedStorage = new BE_ExternalStorage(inv);
        cachedStorage.setPriority(priority);
        cachedStorage.setAccessMode(accessMode);
        return cachedStorage;
    }

    // BE_IStorageProvider
    @Override
    public List<BE_IStorage> getStorages() {
        BE_ExternalStorage storage = getOrCreateStorage();
        if (storage == null) return Collections.emptyList();
        storage.setPriority(priority);
        storage.setAccessMode(accessMode);
        List<BE_IStorage> list = new ArrayList<BE_IStorage>();
        list.add(storage);
        return list;
    }

    public int getPriority() { return priority; }

    public void setPriority(int priority) {
        this.priority = priority;
        if (cachedStorage != null) {
            cachedStorage.setPriority(priority);
        }
        if (network != null) {
            network.rebuildStorage();
        }
    }

    public BE_AccessMode getAccessMode() { return accessMode; }

    public void setAccessMode(BE_AccessMode mode) {
        this.accessMode = mode;
        if (cachedStorage != null) {
            cachedStorage.setAccessMode(mode);
        }
        if (network != null) {
            network.rebuildStorage();
        }
    }

    // BE_INetworkNode
    @Override
    public int getEnergyUsage() { return ENERGY_USAGE; }
    @Override
    public void onNetworkJoin(BE_StorageNetwork network) { this.network = network; }
    @Override
    public void onNetworkLeave() { this.network = null; cachedStorage = null; }
    @Override
    public BE_StorageNetwork getNetwork() { return network; }
    @Override
    public TileEntity getTileEntity() { return this; }
    @Override
    public boolean canConnectOnSide(int side) { return true; }

    // NBT
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        priority = tag.getInteger("priority");
        int modeOrd = tag.getInteger("accessMode");
        BE_AccessMode[] modes = BE_AccessMode.values();
        if (modeOrd >= 0 && modeOrd < modes.length) {
            accessMode = modes[modeOrd];
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("priority", priority);
        tag.setInteger("accessMode", accessMode.ordinal());
    }
}
