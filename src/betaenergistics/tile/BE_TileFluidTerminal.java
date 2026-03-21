package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_FluidKey;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

import java.util.Map;

/**
 * Fluid Terminal — displays all fluids stored in the network.
 * No inventory — GUI-only interaction like Grid Terminal.
 */
public class BE_TileFluidTerminal extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 3;
    private BE_StorageNetwork network;

    public Map<BE_FluidKey, Integer> getNetworkFluids() {
        if (network == null || !network.isActive()) return null;
        return network.getFluidStorage().getAllFluids();
    }

    public int insertFluid(BE_FluidKey key, int amount) {
        if (network == null || !network.isActive()) return 0;
        return network.getFluidStorage().insertFluid(key, amount, false);
    }

    public int extractFluid(BE_FluidKey key, int amount) {
        if (network == null || !network.isActive()) return 0;
        return network.getFluidStorage().extractFluid(key, amount, false);
    }

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
