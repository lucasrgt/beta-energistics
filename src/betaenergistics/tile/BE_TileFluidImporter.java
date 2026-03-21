package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_FluidKey;

import aero.machineapi.Aero_IFluidHandler;

import net.minecraft.src.*;

/**
 * Fluid Import Bus — pulls fluid from adjacent Aero_IFluidHandler into the network.
 * Pulls up to 100 mB per operation every 10 ticks.
 */
public class BE_TileFluidImporter extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 2;
    private static final int MB_PER_TICK = 100;
    private static final int TICK_INTERVAL = 10;

    private BE_StorageNetwork network;
    private int tickCounter = 0;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld || network == null || !network.isActive()) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        Aero_IFluidHandler adjacent = findAdjacentFluidHandler();
        if (adjacent == null) return;

        int fluidType = adjacent.getFluidType();
        int fluidAmount = adjacent.getFluidAmount();
        if (fluidType == 0 || fluidAmount <= 0) return;

        BE_FluidKey key = new BE_FluidKey(fluidType);
        int toImport = Math.min(fluidAmount, MB_PER_TICK);

        // Simulate insert into network
        int canInsert = network.getFluidStorage().insertFluid(key, toImport, true);
        if (canInsert <= 0) return;

        // Extract from adjacent handler
        int extracted = adjacent.extractFluid(fluidType, canInsert);
        if (extracted > 0) {
            network.getFluidStorage().insertFluid(key, extracted, false);
        }
    }

    private Aero_IFluidHandler findAdjacentFluidHandler() {
        int[][] offsets = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
        for (int[] off : offsets) {
            TileEntity te = worldObj.getBlockTileEntity(xCoord + off[0], yCoord + off[1], zCoord + off[2]);
            if (te instanceof Aero_IFluidHandler && !(te instanceof BE_INetworkNode)) {
                return (Aero_IFluidHandler) te;
            }
        }
        return null;
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
