package betaenergistics.render;

import betaenergistics.tile.BE_TileCable;
import betaenergistics.network.BE_INetworkNode;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * Cable renderer — center piece + arms extending to connected neighbors.
 */
public class BE_RenderCable {
    private static final float MIN = 5.0F / 16.0F;
    private static final float MAX = 11.0F / 16.0F;

    public static boolean renderWorld(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block) {
        // Center piece
        block.setBlockBounds(MIN, MIN, MIN, MAX, MAX, MAX);
        renderer.renderStandardBlock(block, x, y, z);

        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (!(te instanceof BE_TileCable)) {
            block.setBlockBounds(0, 0, 0, 1, 1, 1);
            return true;
        }

        int mask = ((BE_TileCable) te).getConnectionMask();

        // Down (bit 0)
        if ((mask & 1) != 0) {
            block.setBlockBounds(MIN, 0, MIN, MAX, MIN, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }
        // Up (bit 1)
        if ((mask & 2) != 0) {
            block.setBlockBounds(MIN, MAX, MIN, MAX, 1, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }
        // North (bit 2)
        if ((mask & 4) != 0) {
            block.setBlockBounds(MIN, MIN, 0, MAX, MAX, MIN);
            renderer.renderStandardBlock(block, x, y, z);
        }
        // South (bit 3)
        if ((mask & 8) != 0) {
            block.setBlockBounds(MIN, MIN, MAX, MAX, MAX, 1);
            renderer.renderStandardBlock(block, x, y, z);
        }
        // West (bit 4)
        if ((mask & 16) != 0) {
            block.setBlockBounds(0, MIN, MIN, MIN, MAX, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }
        // East (bit 5)
        if ((mask & 32) != 0) {
            block.setBlockBounds(MAX, MIN, MIN, 1, MAX, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }

        block.setBlockBounds(0, 0, 0, 1, 1, 1);
        return true;
    }

    public static void renderInventory(RenderBlocks renderer, Block block, int metadata) {
        block.setBlockBounds(MIN, MIN, 0, MAX, MAX, 1);
        Tessellator t = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads();
        t.setNormal(0, -1, 0); renderer.renderBottomFace(block, 0, 0, 0, block.getBlockTextureFromSide(0));
        t.setNormal(0, 1, 0);  renderer.renderTopFace(block, 0, 0, 0, block.getBlockTextureFromSide(1));
        t.setNormal(0, 0, -1); renderer.renderEastFace(block, 0, 0, 0, block.getBlockTextureFromSide(2));
        t.setNormal(0, 0, 1);  renderer.renderWestFace(block, 0, 0, 0, block.getBlockTextureFromSide(3));
        t.setNormal(-1, 0, 0); renderer.renderNorthFace(block, 0, 0, 0, block.getBlockTextureFromSide(4));
        t.setNormal(1, 0, 0);  renderer.renderSouthFace(block, 0, 0, 0, block.getBlockTextureFromSide(5));
        t.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        block.setBlockBounds(0, 0, 0, 1, 1, 1);
    }
}
