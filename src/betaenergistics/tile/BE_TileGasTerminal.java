package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;

import net.minecraft.src.TileEntity;

public class BE_TileGasTerminal extends TileEntity implements BE_INetworkNode {
    private BE_StorageNetwork network;

    @Override
    public int getEnergyUsage() { return 2; }

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

    public boolean canInteractWith(net.minecraft.src.EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }
}
