package betaenergistics.tile;

import betaenergistics.crafting.BE_CraftingCalculator;
import betaenergistics.crafting.BE_CraftingPlan;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.storage.BE_DiskRegistry;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.TileEntity;

import java.util.List;
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

    /** Get all craftable items from Autocrafters in the network. */
    public Map<BE_ItemKey, List<BE_TileAutocrafter>> getCraftableItems() {
        if (network == null || !network.isActive()) return null;
        return new BE_CraftingCalculator(network).getCraftableItems();
    }

    /** Calculate a crafting plan for the given item and quantity. */
    public BE_CraftingPlan calculatePlan(BE_ItemKey output, int quantity) {
        if (network == null || !network.isActive()) return null;
        return new BE_CraftingCalculator(network).calculate(output, quantity);
    }

    /** Request a craft: find an autocrafter with a matching pattern and queue. */
    public boolean requestCraft(BE_ItemKey output, int quantity) {
        if (network == null || !network.isActive()) return false;
        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (!(te instanceof BE_TileAutocrafter)) continue;
            BE_TileAutocrafter crafter = (BE_TileAutocrafter) te;
            int slot = crafter.findPattern(output);
            if (slot >= 0) {
                crafter.requestCraft(slot, quantity);
                return true;
            }
        }
        return false;
    }

    /** Get total pending crafts across all autocrafters in the network. */
    public int getTotalPendingCrafts() {
        if (network == null || !network.isActive()) return 0;
        int total = 0;
        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (te instanceof BE_TileAutocrafter) {
                total += ((BE_TileAutocrafter) te).getTotalPendingCrafts();
            }
        }
        return total;
    }
}
