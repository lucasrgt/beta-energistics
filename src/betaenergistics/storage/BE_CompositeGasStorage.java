package betaenergistics.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BE_CompositeGasStorage implements BE_IGasStorage {
    private final List<BE_IGasStorage> storages = new ArrayList<BE_IGasStorage>();

    public void addStorage(BE_IGasStorage storage) {
        storages.add(storage);
        Collections.sort(storages, new Comparator<BE_IGasStorage>() {
            public int compare(BE_IGasStorage a, BE_IGasStorage b) {
                return b.getPriority() - a.getPriority();
            }
        });
    }

    public void removeStorage(BE_IGasStorage storage) {
        storages.remove(storage);
    }

    public void clear() {
        storages.clear();
    }

    public int insertGas(BE_GasKey key, int amountMB, boolean simulate) {
        int remaining = amountMB;
        for (BE_IGasStorage s : storages) {
            if (remaining <= 0) break;
            remaining -= s.insertGas(key, remaining, simulate);
        }
        return amountMB - remaining;
    }

    public int extractGas(BE_GasKey key, int amountMB, boolean simulate) {
        int remaining = amountMB;
        for (BE_IGasStorage s : storages) {
            if (remaining <= 0) break;
            remaining -= s.extractGas(key, remaining, simulate);
        }
        return amountMB - remaining;
    }

    public Map<BE_GasKey, Integer> getAllGases() {
        Map<BE_GasKey, Integer> all = new HashMap<BE_GasKey, Integer>();
        for (BE_IGasStorage s : storages) {
            for (Map.Entry<BE_GasKey, Integer> e : s.getAllGases().entrySet()) {
                Integer cur = all.get(e.getKey());
                all.put(e.getKey(), (cur != null ? cur : 0) + e.getValue());
            }
        }
        return all;
    }

    public int getGasAmount(BE_GasKey key) {
        int total = 0;
        for (BE_IGasStorage s : storages) {
            total += s.getGasAmount(key);
        }
        return total;
    }

    public int getPriority() { return 0; }
    public BE_AccessMode getAccessMode() { return BE_AccessMode.INSERT_EXTRACT; }
}
