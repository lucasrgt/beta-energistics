package betaenergistics.network;

import betaenergistics.storage.BE_IFluidStorage;
import java.util.List;

/**
 * Implemented by network nodes that provide fluid storage to the network.
 * (DiskDrive with fluid disks, FluidStorageBus, etc.)
 */
public interface BE_IFluidStorageProvider {

    /** Return all fluid storages this provider contributes to the network. */
    List<BE_IFluidStorage> getFluidStorages();
}
