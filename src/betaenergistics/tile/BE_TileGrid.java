package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_CompositeStorage;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

import java.util.Map;

/**
 * Grid Terminal tile entity — provides access to the network's storage.
 * The GUI reads from the network's RootStorage (CompositeStorage).
 */
public class BE_TileGrid extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 2; // EU/tick

    private BE_StorageNetwork network;

    /** Get all items in the network for display in the Grid GUI. */
    public Map<BE_ItemKey, Integer> getNetworkItems() {
        if (network == null || !network.isActive()) return null;
        return network.getRootStorage().getAll();
    }

    /** Insert an item into the network. Returns amount actually inserted. */
    public int insertItem(BE_ItemKey key, int amount) {
        if (network == null || !network.isActive()) return 0;
        return network.getRootStorage().insert(key, amount, false);
    }

    /** Extract an item from the network. Returns amount actually extracted. */
    public int extractItem(BE_ItemKey key, int amount) {
        if (network == null || !network.isActive()) return 0;
        return network.getRootStorage().extract(key, amount, false);
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
