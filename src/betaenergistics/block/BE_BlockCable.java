package betaenergistics.block;

import betaenergistics.tile.BE_TileCable;
import betaenergistics.tile.BE_TileController;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BE_BlockCable extends BlockContainer {
    private static final float MIN = 0.3125F; // 5/16
    private static final float MAX = 0.6875F; // 11/16

    public BE_BlockCable(int blockId) {
        super(blockId, Material.glass);
        setHardness(0.5F);
        setBlockBounds(MIN, MIN, MIN, MAX, MAX, MAX);
        setBlockName("beCable");
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileCable();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        // Custom render type — will be assigned in mod registration
        return -1; // placeholder
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        // Notify the controller to rediscover the network
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileCable) {
            BE_TileCable cable = (BE_TileCable) te;
            if (cable.getNetwork() != null) {
                // Find controller in the network and trigger rediscovery
                for (Object node : cable.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (!(te instanceof BE_TileCable)) {
            setBlockBounds(MIN, MIN, MIN, MAX, MAX, MAX);
        setBlockName("beCable");
            return;
        }

        int mask = ((BE_TileCable) te).getConnectionMask();
        float minX = MIN, minY = MIN, minZ = MIN;
        float maxX = MAX, maxY = MAX, maxZ = MAX;

        if ((mask & 1) != 0) minY = 0.0F;  // down
        if ((mask & 2) != 0) maxY = 1.0F;  // up
        if ((mask & 4) != 0) minZ = 0.0F;  // north
        if ((mask & 8) != 0) maxZ = 1.0F;  // south
        if ((mask & 16) != 0) minX = 0.0F; // west
        if ((mask & 32) != 0) maxX = 1.0F; // east

        setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return 0; // TODO: cable texture
    }
}
