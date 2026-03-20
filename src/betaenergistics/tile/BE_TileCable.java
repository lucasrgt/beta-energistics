package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

/**
 * Cable tile entity — passthrough node that connects network blocks.
 * No energy cost, connects on all 6 sides.
 */
public class BE_TileCable extends TileEntity implements BE_INetworkNode {
    private BE_StorageNetwork network;

    @Override
    public int getEnergyUsage() { return 0; }

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

    /**
     * Get connection state for rendering.
     * Returns bitmask: bit 0=down, 1=up, 2=north, 3=south, 4=west, 5=east
     */
    public int getConnectionMask() {
        if (worldObj == null) return 0;
        int mask = 0;
        int[][] offsets = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
        for (int side = 0; side < 6; side++) {
            TileEntity neighbor = worldObj.getBlockTileEntity(
                xCoord + offsets[side][0],
                yCoord + offsets[side][1],
                zCoord + offsets[side][2]
            );
            if (neighbor instanceof BE_INetworkNode) {
                mask |= (1 << side);
            }
        }
        return mask;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
    }
}
