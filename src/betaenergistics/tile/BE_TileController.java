package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_DiskRegistry;

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
    private boolean registryLoaded = false;
    private int saveCounter = 0;

    private static final int BASE_ENERGY_GEN = 10;  // EU/tick passive generation
    private static final int ENERGY_CAPACITY = 3200;
    private static final int SAVE_INTERVAL = 200;    // save every 10 seconds

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld) return;

        // Load disk registry from world file on first tick
        if (!registryLoaded) {
            BE_DiskRegistry.load(worldObj);
            registryLoaded = true;
        }

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

        // Periodically save disk registry and update disk names
        if (++saveCounter >= SAVE_INTERVAL) {
            BE_DiskRegistry.save(worldObj);
            BE_DiskRegistry.updateAllDiskNames();
            saveCounter = 0;
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
        // Force save registry when world saves
        if (worldObj != null) {
            BE_DiskRegistry.save(worldObj);
        }
    }
}
