package betaenergistics.tile;

/**
 * Gas Terminal — displays all gases stored in the network.
 * No inventory — GUI-only interaction like Grid/Fluid Terminal.
 */
public class BE_TileGasTerminal extends BE_TileTerminalBase {

    @Override
    public int getEnergyUsage() { return 2; }
}
