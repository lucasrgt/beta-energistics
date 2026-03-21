package betaenergistics.block;

import betaenergistics.tile.BE_TileAdvancedInterface;
import betaenergistics.tile.BE_TileController;

import net.minecraft.src.*;

public class BE_BlockAdvancedInterface extends BlockContainer {
    public BE_BlockAdvancedInterface(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beAdvancedInterface");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileAdvancedInterface(); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileAdvancedInterface) {
            BE_TileAdvancedInterface iface = (BE_TileAdvancedInterface) te;
            iface.onAdjacentChanged();
            if (iface.getNetwork() != null) {
                for (Object node : iface.getNetwork().getNodes()) {
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
        if (te instanceof BE_TileAdvancedInterface) {
            BE_TileAdvancedInterface iface = (BE_TileAdvancedInterface) te;
            // Drop buffered items
            for (int i = 0; i < iface.getSizeInventory(); i++) {
                ItemStack stack = iface.getStackInSlot(i);
                if (stack != null) {
                    float rx = world.rand.nextFloat() * 0.8F + 0.1F;
                    float ry = world.rand.nextFloat() * 0.8F + 0.1F;
                    float rz = world.rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entity = new EntityItem(world, x + rx, y + ry, z + rz, stack);
                    world.entityJoinedWorld(entity);
                }
            }
        }
        super.onBlockRemoval(world, x, y, z);
    }
}
