package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileStorageBus;

import net.minecraft.src.*;

public class BE_BlockStorageBus extends BlockContainer {
    public BE_BlockStorageBus(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beStorageBus");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileStorageBus(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileStorageBus) {
            BE_TileStorageBus bus = (BE_TileStorageBus) te;
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
