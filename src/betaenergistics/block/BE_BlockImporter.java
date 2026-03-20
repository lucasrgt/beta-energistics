package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileImporter;

import net.minecraft.src.*;

public class BE_BlockImporter extends BlockContainer {
    public BE_BlockImporter(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileImporter(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileImporter) {
            BE_TileImporter imp = (BE_TileImporter) te;
            if (imp.getNetwork() != null) {
                for (Object node : imp.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public int getBlockTextureFromSide(int side) { return 0; }
}
