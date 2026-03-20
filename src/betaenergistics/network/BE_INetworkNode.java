package betaenergistics.network;

import net.minecraft.src.TileEntity;

/**
 * Interface for any tile entity that participates in a storage network.
 * Implemented by Controller, DiskDrive, Grid, Cable, Importer, Exporter, etc.
 */
public interface BE_INetworkNode {

    /** Energy consumed per tick by this node (in EU). */
    int getEnergyUsage();

    /** Called when the node joins a network. */
    void onNetworkJoin(BE_StorageNetwork network);

    /** Called when the node leaves a network (cable broken, etc.). */
    void onNetworkLeave();

    /** Get the network this node belongs to. May be null. */
    BE_StorageNetwork getNetwork();

    /** Get the tile entity for this node. */
    TileEntity getTileEntity();

    /** Whether this node can connect to adjacent nodes on the given side (0-5). */
    boolean canConnectOnSide(int side);
}
