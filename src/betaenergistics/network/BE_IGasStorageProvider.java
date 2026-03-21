package betaenergistics.network;

import betaenergistics.storage.BE_IGasStorage;
import java.util.List;

public interface BE_IGasStorageProvider {
    List<BE_IGasStorage> getGasStorages();
}
