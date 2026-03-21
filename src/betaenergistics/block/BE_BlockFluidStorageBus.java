package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileFluidStorageBus;

import net.minecraft.src.*;

public class BE_BlockFluidStorageBus extends BlockContainer {
    public BE_BlockFluidStorageBus(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beFluidStorageBus");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileFluidStorageBus(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileFluidStorageBus) {
            BE_TileFluidStorageBus bus = (BE_TileFluidStorageBus) te;
            bus.onAdjacentChanged();
            if (bus.getNetwork() != null) {
                for (Object node : bus.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }
}
