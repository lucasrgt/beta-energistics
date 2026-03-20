package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileCraftingTerminal;
import betaenergistics.tile.BE_TileController;

import net.minecraft.src.*;

public class BE_BlockCraftingTerminal extends BlockContainer {
    public BE_BlockCraftingTerminal(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileCraftingTerminal();
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
        if (te instanceof BE_TileCraftingTerminal) {
            BE_TileCraftingTerminal ct = (BE_TileCraftingTerminal) te;
            if (ct.getNetwork() != null) {
                for (Object node : ct.getNetwork().getNodes()) {
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
        if (te instanceof BE_TileCraftingTerminal) {
            BE_TileCraftingTerminal ct = (BE_TileCraftingTerminal) te;
            for (int i = 0; i < ct.getSizeInventory(); i++) {
                ItemStack stack = ct.getStackInSlot(i);
                if (stack != null) {
                    float rx = world.rand.nextFloat() * 0.6F + 0.1F;
                    float ry = world.rand.nextFloat() * 0.6F + 0.1F;
                    float rz = world.rand.nextFloat() * 0.6F + 0.1F;
                    EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz, stack);
                    world.entityJoinedWorld(entityItem);
                }
            }
        }
        super.onBlockRemoval(world, x, y, z);
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return 0;
    }
}
