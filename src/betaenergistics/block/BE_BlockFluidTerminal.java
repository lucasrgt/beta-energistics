package betaenergistics.block;

import betaenergistics.mod_BetaEnergistics;
import betaenergistics.tile.BE_TileController;
import betaenergistics.tile.BE_TileFluidTerminal;

import net.minecraft.src.*;

public class BE_BlockFluidTerminal extends BlockContainer {
    public BE_BlockFluidTerminal(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.5F);
        setStepSound(soundMetalFootstep);
        setBlockName("beFluidTerminal");
    }

    @Override
    public TileEntity getBlockEntity() { return new BE_TileFluidTerminal(); }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.multiplayerWorld) {
            mod_BetaEnergistics.openGui(player, world, x, y, z);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileFluidTerminal) {
            BE_TileFluidTerminal terminal = (BE_TileFluidTerminal) te;
            if (terminal.getNetwork() != null) {
                for (Object node : terminal.getNetwork().getNodes()) {
                    if (node instanceof BE_TileController) {
                        ((BE_TileController) node).onNeighborChanged();
                        break;
                    }
                }
            }
        }
    }
}
