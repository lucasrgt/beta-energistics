package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileCoprocessor;

import net.minecraft.src.*;

public class BE_BlockCoprocessor extends BlockContainer {
    public BE_BlockCoprocessor(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beCoprocessor");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileCoprocessor(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileCoprocessor) {
            BE_TileCoprocessor coproc = (BE_TileCoprocessor) te;
            if (coproc.getNetwork() != null) {
                for (Object node : coproc.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }
}
