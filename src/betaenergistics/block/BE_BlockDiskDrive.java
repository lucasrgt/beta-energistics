package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileDiskDrive;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BE_BlockDiskDrive extends BlockContainer {
    public BE_BlockDiskDrive(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setBlockName("beDiskDrive");
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileDiskDrive();
    }

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
        if (te instanceof BE_TileDiskDrive) {
            BE_TileDiskDrive drive = (BE_TileDiskDrive) te;
            if (drive.getNetwork() != null) {
                for (Object node : drive.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onBlockRemoval(World world, int x, int y, int z) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileDiskDrive) {
            BE_TileDiskDrive drive = (BE_TileDiskDrive) te;
            // Drop disk items
            for (int i = 0; i < drive.getSizeInventory(); i++) {
                net.minecraft.src.ItemStack stack = drive.getStackInSlot(i);
                if (stack != null) {
                    float rx = world.rand.nextFloat() * 0.6F + 0.1F;
                    float ry = world.rand.nextFloat() * 0.6F + 0.1F;
                    float rz = world.rand.nextFloat() * 0.6F + 0.1F;
                    net.minecraft.src.EntityItem entityItem = new net.minecraft.src.EntityItem(
                        world, x + rx, y + ry, z + rz, stack);
                    world.entityJoinedWorld(entityItem);
                }
            }
        }
        super.onBlockRemoval(world, x, y, z);
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return this.blockIndexInTexture;
    }
}
