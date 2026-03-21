package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

/**
 * Abstract base for all terminal tile entities (Grid, Fluid, Gas).
 * Provides shared BE_INetworkNode implementation and network field management.
 */
public abstract class BE_TileTerminalBase extends TileEntity implements BE_INetworkNode {
    protected BE_StorageNetwork network;

    /** Subclasses define their own energy usage. */
    public abstract int getEnergyUsage();

    @Override
    public void onNetworkJoin(BE_StorageNetwork network) {
        this.network = network;
    }

    @Override
    public void onNetworkLeave() {
        this.network = null;
    }

    @Override
    public BE_StorageNetwork getNetwork() { return network; }

    @Override
    public TileEntity getTileEntity() { return this; }

    @Override
    public boolean canConnectOnSide(int side) { return true; }

    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); }

    @Override
    public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); }
}
