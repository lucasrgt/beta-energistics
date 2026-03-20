package betaenergistics.tile;

import aero.machineapi.Aero_IEnergyReceiver;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

/**
 * Accepts EU from external sources (IC2/RetroNism cables) and feeds it into
 * the ME network energy pool.
 */
public class BE_TileEnergyAcceptor extends TileEntity implements BE_INetworkNode, Aero_IEnergyReceiver {
    private BE_StorageNetwork network;
    private int storedEnergy = 0;

    private static final int MAX_ENERGY = 1600;
    private static final int ENERGY_USAGE = 0; // passive block, no drain

    // --- Aero_IEnergyReceiver ---

    @Override
    public int receiveEnergy(int amount) {
        int space = MAX_ENERGY - storedEnergy;
        int accepted = Math.min(amount, space);
        storedEnergy += accepted;
        return accepted;
    }

    @Override
    public int getStoredEnergy() { return storedEnergy; }

    @Override
    public int getMaxEnergy() { return MAX_ENERGY; }

    // --- Tick: push buffered energy into network ---

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld) return;
        if (network != null && storedEnergy > 0) {
            int before = network.getEnergyStored();
            network.addEnergy(storedEnergy);
            int actuallyAdded = network.getEnergyStored() - before;
            storedEnergy -= actuallyAdded;
        }
    }

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

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        storedEnergy = tag.getInteger("StoredEnergy");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("StoredEnergy", storedEnergy);
    }
}
