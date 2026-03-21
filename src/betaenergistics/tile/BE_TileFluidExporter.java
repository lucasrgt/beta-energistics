package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_FluidKey;

import aero.machineapi.Aero_IFluidHandler;

import net.minecraft.src.*;

import java.util.Map;

/**
 * Fluid Export Bus — pushes fluids from the network into adjacent Aero_IFluidHandler.
 * Configurable filter: stores the fluid type to export (via NBT).
 * Pushes up to 100 mB per operation every 10 ticks.
 */
public class BE_TileFluidExporter extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 2;
    private static final int MB_PER_TICK = 100;
    private static final int TICK_INTERVAL = 10;

    private BE_StorageNetwork network;
    private int tickCounter = 0;
    private int filterFluidType = 0; // 0 = no filter (export any)

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld || network == null || !network.isActive()) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        Aero_IFluidHandler adjacent = findAdjacentFluidHandler();
        if (adjacent == null) return;

        if (filterFluidType != 0) {
            // Export specific fluid type
            exportFluid(adjacent, filterFluidType);
        } else {
            // Export any fluid available in network
            Map<BE_FluidKey, Integer> fluids = network.getFluidStorage().getAllFluids();
            for (BE_FluidKey key : fluids.keySet()) {
                if (exportFluid(adjacent, key.fluidType) > 0) break;
            }
        }
    }

    private int exportFluid(Aero_IFluidHandler target, int fluidType) {
        BE_FluidKey key = new BE_FluidKey(fluidType);

        // Check adjacent can receive this fluid type
        int targetType = target.getFluidType();
        if (targetType != 0 && targetType != fluidType) return 0;

        int space = target.getFluidCapacity() - target.getFluidAmount();
        int toExport = Math.min(space, MB_PER_TICK);
        if (toExport <= 0) return 0;

        // Simulate extraction from network
        int canExtract = network.getFluidStorage().extractFluid(key, toExport, true);
        if (canExtract <= 0) return 0;

        // Push to adjacent handler
        int received = target.receiveFluid(fluidType, canExtract);
        if (received > 0) {
            network.getFluidStorage().extractFluid(key, received, false);
        }
        return received;
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

    public int getFilterFluidType() { return filterFluidType; }
    public void setFilterFluidType(int type) { this.filterFluidType = type; }

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
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        filterFluidType = tag.getInteger("filterFluid");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("filterFluid", filterFluidType);
    }
}
