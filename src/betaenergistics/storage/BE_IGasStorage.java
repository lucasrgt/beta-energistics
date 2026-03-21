package betaenergistics.storage;

import java.util.Map;

public interface BE_IGasStorage {
    int insertGas(BE_GasKey key, int amountMB, boolean simulate);
    int extractGas(BE_GasKey key, int amountMB, boolean simulate);
    Map<BE_GasKey, Integer> getAllGases();
    int getGasAmount(BE_GasKey key);
    int getPriority();
    BE_AccessMode getAccessMode();
}
