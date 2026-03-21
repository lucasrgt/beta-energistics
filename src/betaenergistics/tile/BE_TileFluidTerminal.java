package betaenergistics.tile;

import betaenergistics.storage.BE_FluidKey;

import java.util.Map;

/**
 * Fluid Terminal — displays all fluids stored in the network.
 * No inventory — GUI-only interaction like Grid Terminal.
 */
public class BE_TileFluidTerminal extends BE_TileTerminalBase {
    private static final int ENERGY_USAGE = 3;

    @Override
    public int getEnergyUsage() { return ENERGY_USAGE; }

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
}
