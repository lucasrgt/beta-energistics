package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileEnergyAcceptor;

import net.minecraft.src.*;

public class BE_BlockEnergyAcceptor extends BlockContainer {
    public BE_BlockEnergyAcceptor(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beEnergyAcceptor");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileEnergyAcceptor(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileEnergyAcceptor) {
            BE_TileEnergyAcceptor acceptor = (BE_TileEnergyAcceptor) te;
            if (acceptor.getNetwork() != null) {
                for (Object node : acceptor.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }
}
