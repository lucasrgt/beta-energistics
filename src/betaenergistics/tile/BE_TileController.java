package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

/**
 * Heart of the storage network. Generates/stores energy and manages the network graph.
 * Only one Controller per network.
 * Accepts EU from IC2 (future) or generates a base amount passively.
 */
public class BE_TileController extends TileEntity implements BE_INetworkNode {
    private BE_StorageNetwork network;
    private boolean needsDiscovery = true;

    private static final int BASE_ENERGY_GEN = 10;  // EU/tick passive generation
    private static final int ENERGY_CAPACITY = 3200;

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        if (needsDiscovery) {
            network = new BE_StorageNetwork();
            network.setEnergyCapacity(ENERGY_CAPACITY);
            network.discover(worldObj, xCoord, yCoord, zCoord);
            needsDiscovery = false;
        }

        if (network != null) {
            network.addEnergy(BASE_ENERGY_GEN);
            network.tick();
        }
    }

    /** Called when an adjacent block changes — triggers network rediscovery. */
    public void onNeighborChanged() {
        needsDiscovery = true;
    }

    // BE_INetworkNode implementation
    @Override
    public int getEnergyUsage() { return 0; } // controller itself is free

    @Override
    public void onNetworkJoin(BE_StorageNetwork network) {
        this.network = network;
    }

    @Override
    public void onNetworkLeave() {
        this.network = null;
    }

    @Override
    public BE_StorageNetwork getNetwork() { return network; }

    @Override
    public TileEntity getTileEntity() { return this; }

    @Override
    public boolean canConnectOnSide(int side) { return true; }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
    }
}
