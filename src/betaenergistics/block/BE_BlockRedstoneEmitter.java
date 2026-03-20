package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileRedstoneEmitter;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BE_BlockRedstoneEmitter extends BlockContainer {
    public BE_BlockRedstoneEmitter(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
        setBlockName("beRedstoneEmitter");
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileRedstoneEmitter();
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
        if (te instanceof BE_TileRedstoneEmitter) {
            BE_TileRedstoneEmitter emitter = (BE_TileRedstoneEmitter) te;
            if (emitter.getNetwork() != null) {
                for (Object node : emitter.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean isPoweringTo(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileRedstoneEmitter) {
            return ((BE_TileRedstoneEmitter) te).getRedstoneOutput() > 0;
        }
        return false;
    }

    @Override
    public boolean isIndirectlyPoweringTo(World world, int x, int y, int z, int side) {
        return isPoweringTo(world, x, y, z, side);
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return this.blockIndexInTexture;
    }
}
