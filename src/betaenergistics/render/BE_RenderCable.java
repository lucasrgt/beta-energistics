package betaenergistics.render;

import betaenergistics.tile.BE_TileCable;
import betaenergistics.network.BE_INetworkNode;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * Glass cable renderer — 4x4 tube with 1px grey edges and transparent center.
 * Each of the 12 edges extends only along its principal axis when connected.
 */
public class BE_RenderCable {
    private static final float A = 6.0F / 16.0F;   // tube min
    private static final float B = 10.0F / 16.0F;   // tube max
    private static final float E = 1.0F / 16.0F;    // edge thickness
    private static final float FACADE_THICK = 2.0F / 16.0F;

    public static boolean renderWorld(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        BE_TileCable cable = (te instanceof BE_TileCable) ? (BE_TileCable) te : null;

        int mask = cable != null ? cable.getConnectionMask() : 0;

        boolean down  = (mask & 1) != 0;
        boolean up    = (mask & 2) != 0;
        boolean north = (mask & 4) != 0;
        boolean south = (mask & 8) != 0;
        boolean west  = (mask & 16) != 0;
        boolean east  = (mask & 32) != 0;

        // X-axis edges extend with west/east connections
        float xL = west ? 0 : A;
        float xR = east ? 1 : B;

        // Y-axis edges extend with down/up connections
        float yL = down ? 0 : A;
        float yR = up   ? 1 : B;

        // Z-axis edges extend with north/south connections
        float zL = north ? 0 : A;
        float zR = south ? 1 : B;

        // 4 edges along X axis (extend with west/east)
        // bottom-north
        block.setBlockBounds(xL, A, A, xR, A + E, A + E);
        renderer.renderStandardBlock(block, x, y, z);
        // bottom-south
        block.setBlockBounds(xL, A, B - E, xR, A + E, B);
        renderer.renderStandardBlock(block, x, y, z);
        // top-north
        block.setBlockBounds(xL, B - E, A, xR, B, A + E);
        renderer.renderStandardBlock(block, x, y, z);
        // top-south
        block.setBlockBounds(xL, B - E, B - E, xR, B, B);
        renderer.renderStandardBlock(block, x, y, z);

        // 4 edges along Y axis (extend with up/down)
        // north-west
        block.setBlockBounds(A, yL, A, A + E, yR, A + E);
        renderer.renderStandardBlock(block, x, y, z);
        // north-east
        block.setBlockBounds(B - E, yL, A, B, yR, A + E);
        renderer.renderStandardBlock(block, x, y, z);
        // south-west
        block.setBlockBounds(A, yL, B - E, A + E, yR, B);
        renderer.renderStandardBlock(block, x, y, z);
        // south-east
        block.setBlockBounds(B - E, yL, B - E, B, yR, B);
        renderer.renderStandardBlock(block, x, y, z);

        // 4 edges along Z axis (extend with north/south)
        // bottom-west
        block.setBlockBounds(A, A, zL, A + E, A + E, zR);
        renderer.renderStandardBlock(block, x, y, z);
        // bottom-east
        block.setBlockBounds(B - E, A, zL, B, A + E, zR);
        renderer.renderStandardBlock(block, x, y, z);
        // top-west
        block.setBlockBounds(A, B - E, zL, A + E, B, zR);
        renderer.renderStandardBlock(block, x, y, z);
        // top-east
        block.setBlockBounds(B - E, B - E, zL, B, B, zR);
        renderer.renderStandardBlock(block, x, y, z);

        // Facades
        if (cable != null && cable.hasFacades()) {
            renderFacades(renderer, world, x, y, z, block, cable);
        }

        block.setBlockBounds(0, 0, 0, 1, 1, 1);
        return true;
    }

    private static void renderFacades(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, BE_TileCable cable) {
        int savedTex = block.blockIndexInTexture;

        for (int face = 0; face < 6; face++) {
            int facadeBlockId = cable.getFacade(face);
            if (facadeBlockId == 0) continue;

            Block facadeBlock = Block.blocksList[facadeBlockId];
            if (facadeBlock == null) continue;

            block.blockIndexInTexture = facadeBlock.getBlockTextureFromSide(face);

            switch (face) {
                case 0: block.setBlockBounds(0, 0, 0, 1, FACADE_THICK, 1); break;
                case 1: block.setBlockBounds(0, 1 - FACADE_THICK, 0, 1, 1, 1); break;
                case 2: block.setBlockBounds(0, 0, 0, 1, 1, FACADE_THICK); break;
                case 3: block.setBlockBounds(0, 0, 1 - FACADE_THICK, 1, 1, 1); break;
                case 4: block.setBlockBounds(0, 0, 0, FACADE_THICK, 1, 1); break;
                case 5: block.setBlockBounds(1 - FACADE_THICK, 0, 0, 1, 1, 1); break;
            }

            renderer.renderStandardBlock(block, x, y, z);
        }

        block.blockIndexInTexture = savedTex;
    }

    public static void renderInventory(RenderBlocks renderer, Block block, int metadata) {
        Tessellator t = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        // 4 edges along Z for inventory display
        float[][] edges = {
            {A, A, 0, A + E, A + E, 1},
            {B - E, A, 0, B, A + E, 1},
            {A, B - E, 0, A + E, B, 1},
            {B - E, B - E, 0, B, B, 1}
        };

        for (float[] edge : edges) {
            block.setBlockBounds(edge[0], edge[1], edge[2], edge[3], edge[4], edge[5]);
            t.startDrawingQuads();
            t.setNormal(0, -1, 0); renderer.renderBottomFace(block, 0, 0, 0, block.getBlockTextureFromSide(0));
            t.setNormal(0, 1, 0);  renderer.renderTopFace(block, 0, 0, 0, block.getBlockTextureFromSide(1));
            t.setNormal(0, 0, -1); renderer.renderEastFace(block, 0, 0, 0, block.getBlockTextureFromSide(2));
            t.setNormal(0, 0, 1);  renderer.renderWestFace(block, 0, 0, 0, block.getBlockTextureFromSide(3));
            t.setNormal(-1, 0, 0); renderer.renderNorthFace(block, 0, 0, 0, block.getBlockTextureFromSide(4));
            t.setNormal(1, 0, 0);  renderer.renderSouthFace(block, 0, 0, 0, block.getBlockTextureFromSide(5));
            t.draw();
        }

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        block.setBlockBounds(0, 0, 0, 1, 1, 1);
    }
}
