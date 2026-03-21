package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileFluidImporter;

import net.minecraft.src.*;

public class BE_BlockFluidImporter extends BlockContainer {
    public BE_BlockFluidImporter(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beFluidImporter");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileFluidImporter(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileFluidImporter) {
            BE_TileFluidImporter importer = (BE_TileFluidImporter) te;
            if (importer.getNetwork() != null) {
                for (Object node : importer.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }
}
