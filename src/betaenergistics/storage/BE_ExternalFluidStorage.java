package betaenergistics.storage;

import aero.machineapi.Aero_IFluidHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps an external Aero_IFluidHandler (tank, pipe, etc.) as a network fluid storage.
 * Reads/writes directly to the external handler — no buffer.
 * Used by Fluid Storage Bus.
 */
public class BE_ExternalFluidStorage implements BE_IFluidStorage {
    private final Aero_IFluidHandler handler;
    private int priority = 0;
    private BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;

    public BE_ExternalFluidStorage(Aero_IFluidHandler handler) {
        this.handler = handler;
    }

    public int insertFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        if (!accessMode.allowsInsert() || amountMB <= 0) return 0;
        if (simulate) {
            // Estimate: check capacity minus current amount
            int space = handler.getFluidCapacity() - handler.getFluidAmount();
            // Can only insert if handler has no fluid or same fluid type
            int currentType = handler.getFluidType();
            if (currentType != 0 && currentType != key.fluidType) return 0;
            return Math.min(amountMB, Math.max(0, space));
        }
        return handler.receiveFluid(key.fluidType, amountMB);
    }

    public int extractFluid(BE_FluidKey key, int amountMB, boolean simulate) {
        if (!accessMode.allowsExtract() || amountMB <= 0) return 0;
        if (handler.getFluidType() != key.fluidType) return 0;
        if (simulate) {
            return Math.min(amountMB, handler.getFluidAmount());
        }
        return handler.extractFluid(key.fluidType, amountMB);
    }

    public int getFluidCount(BE_FluidKey key) {
        if (handler.getFluidType() != key.fluidType) return 0;
        return handler.getFluidAmount();
    }

    public Map<BE_FluidKey, Integer> getAllFluids() {
        int type = handler.getFluidType();
        int amount = handler.getFluidAmount();
        if (type == 0 || amount <= 0) return Collections.emptyMap();
        Map<BE_FluidKey, Integer> map = new HashMap<BE_FluidKey, Integer>();
        map.put(new BE_FluidKey(type), amount);
        return map;
    }

    public int getStored() {
        return handler.getFluidAmount();
    }

    public int getCapacity() {
        return handler.getFluidCapacity();
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public BE_AccessMode getAccessMode() { return accessMode; }
    public void setAccessMode(BE_AccessMode mode) { this.accessMode = mode; }
    public Aero_IFluidHandler getHandler() { return handler; }
}
