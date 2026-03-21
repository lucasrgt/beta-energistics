package betaenergistics.tile;

import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_ItemKey;

import java.util.Map;

/**
 * Grid Terminal tile entity — provides access to the network's item storage.
 * The GUI reads from the network's RootStorage (CompositeStorage).
 */
public class BE_TileGrid extends BE_TileTerminalBase {
    private static final int ENERGY_USAGE = 2; // EU/tick

    @Override
    public int getEnergyUsage() { return ENERGY_USAGE; }

    /** Get all items in the network for display in the Grid GUI. */
    public Map<BE_ItemKey, Integer> getNetworkItems() {
        if (network == null || !network.isActive()) return null;
        return network.getRootStorage().getAll();
    }

    /** Insert an item into the network. Returns amount actually inserted. */
    public int insertItem(BE_ItemKey key, int amount) {
        if (network == null || !network.isActive()) return 0;
        int inserted = network.getRootStorage().insert(key, amount, false);
        if (inserted > 0) BE_DiskRegistry.updateAllDiskNames();
        return inserted;
    }

    /** Extract an item from the network. Returns amount actually extracted. */
    public int extractItem(BE_ItemKey key, int amount) {
        if (network == null || !network.isActive()) return 0;
        int extracted = network.getRootStorage().extract(key, amount, false);
        if (extracted > 0) BE_DiskRegistry.updateAllDiskNames();
        return extracted;
    }
}
