package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

/**
 * Crafting Coprocessor — passive network node that increases the network's
 * maximum concurrent crafting tasks by 1 per coprocessor.
 *
 * Base network allows 1 concurrent craft. Each coprocessor adds +1.
 */
public class BE_TileCoprocessor extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 2;
    private BE_StorageNetwork network;

    // --- BE_INetworkNode ---

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

    // --- NBT (no custom data needed) ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
    }
}
