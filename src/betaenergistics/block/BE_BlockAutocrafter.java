package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileAutocrafter;
import betaenergistics.tile.BE_TileController;

import net.minecraft.src.*;

public class BE_BlockAutocrafter extends BlockContainer {
    public BE_BlockAutocrafter(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
        setBlockName("beAutocrafter");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileAutocrafter(); }

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
        if (te instanceof BE_TileAutocrafter) {
            BE_TileAutocrafter ac = (BE_TileAutocrafter) te;
            if (ac.getNetwork() != null) {
                for (Object node : ac.getNetwork().getNodes()) {
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
        if (te instanceof BE_TileAutocrafter) {
            BE_TileAutocrafter ac = (BE_TileAutocrafter) te;
            for (int i = 0; i < ac.getSizeInventory(); i++) {
                ItemStack stack = ac.getStackInSlot(i);
                if (stack != null) {
                    EntityItem entityItem = new EntityItem(world,
                        x + world.rand.nextFloat() * 0.6F + 0.1F,
                        y + world.rand.nextFloat() * 0.6F + 0.1F,
                        z + world.rand.nextFloat() * 0.6F + 0.1F, stack);
                    world.entityJoinedWorld(entityItem);
                }
            }
        }
        super.onBlockRemoval(world, x, y, z);
    }

    @Override
    public int getBlockTextureFromSide(int side) { return 0; }
}
