package betaenergistics.block;

import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BE_BlockGrid extends BlockContainer {
    public BE_BlockGrid(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileGrid();
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) return true;

        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileGrid) {
            // TODO: open grid GUI
            // player.openGui(mod_BetaEnergistics.instance, GUI_GRID, world, x, y, z);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileGrid) {
            BE_TileGrid grid = (BE_TileGrid) te;
            if (grid.getNetwork() != null) {
                for (Object node : grid.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return 0; // TODO: grid terminal texture
    }
}
