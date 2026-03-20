package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileRecipeEncoder;

import net.minecraft.src.*;

public class BE_BlockRecipeEncoder extends BlockContainer {
    public BE_BlockRecipeEncoder(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beRecipeEncoder");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileRecipeEncoder(); }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (world.multiplayerWorld) return true;
        mod_BetaEnergistics.openGui(player, world, x, y, z);
        return true;
    }

    @Override
    public void onBlockRemoval(World world, int x, int y, int z) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileRecipeEncoder) {
            BE_TileRecipeEncoder encoder = (BE_TileRecipeEncoder) te;
            // Only drop the pattern slot item (ghost slots are not real items)
            ItemStack pattern = encoder.getStackInSlot(BE_TileRecipeEncoder.SLOT_PATTERN);
            if (pattern != null) {
                float rx = world.rand.nextFloat() * 0.8F + 0.1F;
                float ry = world.rand.nextFloat() * 0.8F + 0.1F;
                float rz = world.rand.nextFloat() * 0.8F + 0.1F;
                EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz, pattern);
                world.entityJoinedWorld(entityItem);
            }
        }
        super.onBlockRemoval(world, x, y, z);
    }
}
