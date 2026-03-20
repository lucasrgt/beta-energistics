package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileRequestTerminal;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BE_BlockRequestTerminal extends BlockContainer {
    public BE_BlockRequestTerminal(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
        setBlockName("beRequestTerminal");
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileRequestTerminal();
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
        if (te instanceof BE_TileRequestTerminal) {
            BE_TileRequestTerminal rt = (BE_TileRequestTerminal) te;
            if (rt.getNetwork() != null) {
                for (Object node : rt.getNetwork().getNodes()) {
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
        return this.blockIndexInTexture;
    }
}
