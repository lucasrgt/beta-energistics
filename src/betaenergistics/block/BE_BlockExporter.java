package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileExporter;

import net.minecraft.src.*;

public class BE_BlockExporter extends BlockContainer {
    public BE_BlockExporter(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileExporter(); }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (player.isSneaking()) return false;
        if (world.multiplayerWorld) return true;
        mod_BetaEnergistics.openGui(player, world, x, y, z);
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileExporter) {
            BE_TileExporter exp = (BE_TileExporter) te;
            if (exp.getNetwork() != null) {
                for (Object node : exp.getNetwork().getNodes()) {
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
