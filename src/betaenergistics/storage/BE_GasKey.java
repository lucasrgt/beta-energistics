package betaenergistics.storage;

import aero.machineapi.Aero_GasType;

public class BE_GasKey {
    public final int gasType;

    public BE_GasKey(int gasType) {
        this.gasType = gasType;
    }

    public String getName() {
        return Aero_GasType.getName(gasType);
    }

    public int getColor() {
        return Aero_GasType.getColor(gasType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BE_GasKey)) return false;
        return gasType == ((BE_GasKey) o).gasType;
    }

    @Override
    public int hashCode() {
        return gasType;
    }
}
