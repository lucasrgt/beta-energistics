package betaenergistics.tile;

import betaenergistics.crafting.BE_CraftingCalculator;
import betaenergistics.crafting.BE_CraftingPlan;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

import java.util.List;
import java.util.Map;

/**
 * Request Terminal tile entity — shows craftable items and lets players request crafts.
 * Similar to Grid Terminal but focused on auto-crafting requests.
 */
public class BE_TileRequestTerminal extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 2;

    private BE_StorageNetwork network;

    /** Get all craftable items from all autocrafters in the network. */
    public Map<BE_ItemKey, List<BE_TileAutocrafter>> getCraftableItems() {
        if (network == null || !network.isActive()) return null;
        BE_CraftingCalculator calc = new BE_CraftingCalculator(network);
        return calc.getCraftableItems();
    }

    /** Calculate a crafting plan for the given item and quantity. */
    public BE_CraftingPlan calculatePlan(BE_ItemKey output, int quantity) {
        if (network == null || !network.isActive()) return null;
        BE_CraftingCalculator calc = new BE_CraftingCalculator(network);
        return calc.calculate(output, quantity);
    }

    /** Request a craft: find an autocrafter with a matching pattern and queue the craft. */
    public boolean requestCraft(BE_ItemKey output) {
        if (network == null || !network.isActive()) return false;

        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (!(te instanceof BE_TileAutocrafter)) continue;

            BE_TileAutocrafter crafter = (BE_TileAutocrafter) te;
            int slot = crafter.findPattern(output);
            if (slot >= 0) {
                crafter.requestCraft(slot, 1);
                return true;
            }
        }
        return false;
    }

    // BE_INetworkNode
    @Override
    public int getEnergyUsage() { return ENERGY_USAGE; }

    @Override
    public void onNetworkJoin(BE_StorageNetwork network) { this.network = network; }

    @Override
    public void onNetworkLeave() { this.network = null; }

    @Override
    public BE_StorageNetwork getNetwork() { return network; }

    @Override
    public TileEntity getTileEntity() { return this; }

    @Override
    public boolean canConnectOnSide(int side) { return true; }

    @Override
    public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); }

    @Override
    public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); }
}
