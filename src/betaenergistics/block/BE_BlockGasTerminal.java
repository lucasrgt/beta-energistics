package betaenergistics.block;

import betaenergistics.tile.BE_TileGasTerminal;
import betaenergistics.gui.BE_GuiGasTerminal;

import net.minecraft.src.*;

public class BE_BlockGasTerminal extends BlockContainer {
    public BE_BlockGasTerminal(int blockId) {
        super(blockId, Material.iron);
        setHardness(3.0F);
        setBlockName("beGasTerminal");
    }

    @Override
    public TileEntity getBlockEntity() {
        return new BE_TileGasTerminal();
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (world.multiplayerWorld) return true;
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof BE_TileGasTerminal) {
            ModLoader.OpenGUI(player, new BE_GuiGasTerminal(
                player.inventory, (BE_TileGasTerminal) te));
        }
        return true;
    }
}
