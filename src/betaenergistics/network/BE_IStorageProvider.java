package betaenergistics.network;

import betaenergistics.storage.BE_DiskStorage;
import java.util.List;

/**
 * Implemented by network nodes that provide storage to the network.
 * (DiskDrive, StorageBlock, ExternalStorage, etc.)
 */
public interface BE_IStorageProvider {

    /** Return all storages this provider contributes to the network. */
    List<BE_DiskStorage> getStorages();
}
