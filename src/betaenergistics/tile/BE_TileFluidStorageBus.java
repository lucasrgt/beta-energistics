package betaenergistics.tile;

import betaenergistics.network.BE_IFluidStorageProvider;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_AccessMode;
import betaenergistics.storage.BE_ExternalFluidStorage;
import betaenergistics.storage.BE_IFluidStorage;

import aero.machineapi.Aero_IFluidHandler;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fluid Storage Bus — exposes an adjacent Aero_IFluidHandler (tank, etc.) to the network.
 * Uses BE_ExternalFluidStorage to wrap the adjacent handler.
 * Configurable priority and access mode.
 */
public class BE_TileFluidStorageBus extends TileEntity implements BE_INetworkNode, BE_IFluidStorageProvider {
    private static final int ENERGY_USAGE = 3;

    private BE_StorageNetwork network;
    private int priority = 0;
    private BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;
    private BE_ExternalFluidStorage cachedStorage;

    public void onAdjacentChanged() {
        cachedStorage = null;
        if (network != null) {
            network.rebuildFluidStorage();
        }
    }

    private Aero_IFluidHandler findAdjacentFluidHandler() {
        int[][] offsets = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
        for (int[] off : offsets) {
            TileEntity te = worldObj.getBlockTileEntity(xCoord + off[0], yCoord + off[1], zCoord + off[2]);
            if (te instanceof Aero_IFluidHandler && !(te instanceof BE_INetworkNode)) {
                return (Aero_IFluidHandler) te;
            }
        }
        return null;
    }

    private BE_ExternalFluidStorage getOrCreateStorage() {
        if (cachedStorage != null) return cachedStorage;
        Aero_IFluidHandler handler = findAdjacentFluidHandler();
        if (handler == null) return null;
        cachedStorage = new BE_ExternalFluidStorage(handler);
        cachedStorage.setPriority(priority);
        cachedStorage.setAccessMode(accessMode);
        return cachedStorage;
    }

    @Override
    public List<BE_IFluidStorage> getFluidStorages() {
        BE_ExternalFluidStorage storage = getOrCreateStorage();
        if (storage == null) return Collections.emptyList();
        storage.setPriority(priority);
        storage.setAccessMode(accessMode);
        List<BE_IFluidStorage> list = new ArrayList<BE_IFluidStorage>();
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
            network.rebuildFluidStorage();
        }
    }

    public BE_AccessMode getAccessMode() { return accessMode; }

    public void setAccessMode(BE_AccessMode mode) {
        this.accessMode = mode;
        if (cachedStorage != null) {
            cachedStorage.setAccessMode(mode);
        }
        if (network != null) {
            network.rebuildFluidStorage();
        }
    }

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
