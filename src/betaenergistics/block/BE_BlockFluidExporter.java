package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileFluidExporter;

import net.minecraft.src.*;

public class BE_BlockFluidExporter extends BlockContainer {
    public BE_BlockFluidExporter(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beFluidExporter");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileFluidExporter(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileFluidExporter) {
            BE_TileFluidExporter exporter = (BE_TileFluidExporter) te;
            if (exporter.getNetwork() != null) {
                for (Object node : exporter.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }
}
