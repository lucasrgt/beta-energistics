package betaenergistics.storage;

import java.util.Map;

/**
 * Common interface for all fluid storage types (FluidDiskStorage, ExternalFluidStorage).
 * Used by CompositeFluidStorage to aggregate different fluid storage backends.
 * Amounts are in millibuckets (mB).
 */
public interface BE_IFluidStorage {
    int insertFluid(BE_FluidKey key, int amountMB, boolean simulate);
    int extractFluid(BE_FluidKey key, int amountMB, boolean simulate);
    int getFluidCount(BE_FluidKey key);
    Map<BE_FluidKey, Integer> getAllFluids();
    int getStored();
    int getCapacity();
    int getPriority();
}
