package betaenergistics.network;

import betaenergistics.storage.BE_IStorage;
import java.util.List;

/**
 * Implemented by network nodes that provide storage to the network.
 * (DiskDrive, StorageBus, etc.)
 */
public interface BE_IStorageProvider {

    /** Return all storages this provider contributes to the network. */
    List<BE_IStorage> getStorages();
}
