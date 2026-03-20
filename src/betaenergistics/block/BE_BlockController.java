package betaenergistics.block;

import betaenergistics.tile.BE_TileController;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BE_BlockController extends BlockContainer {
    public BE_BlockController(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
        setBlockName("beController");
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileController();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileController) {
            ((BE_TileController) te).onNeighborChanged();
        }
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        // TODO: proper texture indices from atlas
        return this.blockIndexInTexture;
    }
}
