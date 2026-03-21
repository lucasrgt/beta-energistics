package betaenergistics.storage;

import aero.machineapi.Aero_FluidType;

/**
 * Immutable key representing a unique fluid type in the storage system.
 * Wraps the fluid type integer from Aero_FluidType.
 */
public class BE_FluidKey {
    public final int fluidType;

    public BE_FluidKey(int fluidType) {
        this.fluidType = fluidType;
    }

    public String getName() {
        return Aero_FluidType.getName(fluidType);
    }

    public int getColor() {
        return Aero_FluidType.getColor(fluidType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BE_FluidKey)) return false;
        return fluidType == ((BE_FluidKey) o).fluidType;
    }

    @Override
    public int hashCode() {
        return fluidType;
    }

    @Override
    public String toString() {
        return "FluidKey(" + fluidType + ":" + getName() + ")";
    }
}
