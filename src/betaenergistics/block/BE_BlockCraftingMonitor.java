package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileCraftingMonitor;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BE_BlockCraftingMonitor extends BlockContainer {
    public BE_BlockCraftingMonitor(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
        setBlockName("beCraftingMonitor");
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileCraftingMonitor();
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (player.isSneaking()) return false;
        if (world.multiplayerWorld) return true;
        mod_BetaEnergistics.openGui(player, world, x, y, z);
        return true;
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return this.blockIndexInTexture;
    }
}
