package betaenergistics.render;

import betaenergistics.tile.BE_TileCable;
import betaenergistics.network.BE_INetworkNode;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * Cable renderer — center piece + arms extending to connected neighbors.
 * Supports facade rendering: full-face quads using the facade block's texture.
 */
public class BE_RenderCable {
    private static final float MIN = 5.0F / 16.0F;
    private static final float MAX = 11.0F / 16.0F;
    /** Facade thickness — thin cover on the outside of the block. */
    private static final float FACADE_THICK = 2.0F / 16.0F;

    public static boolean renderWorld(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        BE_TileCable cable = (te instanceof BE_TileCable) ? (BE_TileCable) te : null;

        // Center piece
        block.setBlockBounds(MIN, MIN, MIN, MAX, MAX, MAX);
        renderer.renderStandardBlock(block, x, y, z);

        if (cable == null) {
            block.setBlockBounds(0, 0, 0, 1, 1, 1);
            return true;
        }

        int mask = cable.getConnectionMask();

        // Connection arms
        if ((mask & 1) != 0) {
            block.setBlockBounds(MIN, 0, MIN, MAX, MIN, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if ((mask & 2) != 0) {
            block.setBlockBounds(MIN, MAX, MIN, MAX, 1, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if ((mask & 4) != 0) {
            block.setBlockBounds(MIN, MIN, 0, MAX, MAX, MIN);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if ((mask & 8) != 0) {
            block.setBlockBounds(MIN, MIN, MAX, MAX, MAX, 1);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if ((mask & 16) != 0) {
            block.setBlockBounds(0, MIN, MIN, MIN, MAX, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if ((mask & 32) != 0) {
            block.setBlockBounds(MAX, MIN, MIN, 1, MAX, MAX);
            renderer.renderStandardBlock(block, x, y, z);
        }

        // Render facades — thin covers on each face using the facade block's texture
        if (cable.hasFacades()) {
            renderFacades(renderer, world, x, y, z, block, cable);
        }

        block.setBlockBounds(0, 0, 0, 1, 1, 1);
        return true;
    }

    /**
     * Render facade covers on each face.
     * Each facade is a thin slab on the outer edge of the block,
     * textured with the facade block's texture on all sides.
     */
    private static void renderFacades(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, BE_TileCable cable) {
        int savedTex = block.blockIndexInTexture;

        for (int face = 0; face < 6; face++) {
            int facadeBlockId = cable.getFacade(face);
            if (facadeBlockId == 0) continue;

            Block facadeBlock = Block.blocksList[facadeBlockId];
            if (facadeBlock == null) continue;

            // Use the facade block's texture
            block.blockIndexInTexture = facadeBlock.getBlockTextureFromSide(face);

            // Set bounds for a thin slab on this face
            switch (face) {
                case 0: // Down
                    block.setBlockBounds(0, 0, 0, 1, FACADE_THICK, 1);
                    break;
                case 1: // Up
                    block.setBlockBounds(0, 1 - FACADE_THICK, 0, 1, 1, 1);
                    break;
                case 2: // North
                    block.setBlockBounds(0, 0, 0, 1, 1, FACADE_THICK);
                    break;
                case 3: // South
                    block.setBlockBounds(0, 0, 1 - FACADE_THICK, 1, 1, 1);
                    break;
                case 4: // West
                    block.setBlockBounds(0, 0, 0, FACADE_THICK, 1, 1);
                    break;
                case 5: // East
                    block.setBlockBounds(1 - FACADE_THICK, 0, 0, 1, 1, 1);
                    break;
            }

            renderer.renderStandardBlock(block, x, y, z);
        }

        // Restore original texture
        block.blockIndexInTexture = savedTex;
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
