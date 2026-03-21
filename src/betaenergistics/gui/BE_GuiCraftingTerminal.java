package betaenergistics.gui;

import betaenergistics.container.BE_ContainerCraftingTerminal;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileCraftingTerminal;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

/**
 * GUI for the Crafting Terminal.
 *
 * Layout (176x240):
 *   Network grid (4 rows x 9 cols) at y=19
 *   3x3 crafting grid at (12, 96) + arrow + result at (94, 114)
 *   Fill/Clear/CraftToNet buttons at (120, 96-142)
 *   Player inventory at y=158, hotbar at y=216
 */
public class BE_GuiCraftingTerminal extends GuiContainer {
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 4;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 19;
    private static final int CELL_SIZE = 18;
    private static final RenderItem gridItemRenderer = new RenderItem();

    // Sort button
    private static final int SORT_BTN_X = 8;
    private static final int SORT_BTN_Y = 6;
    private static final int SORT_BTN_W = 30;
    private static final int SORT_BTN_H = 10;

    // Action buttons (relative to GUI top-left)
    private static final int BTN_X = 120;
    private static final int BTN_W = 48;
    private static final int BTN_H = 12;
    private static final int FILL_BTN_Y = 98;
    private static final int CLEAR_BTN_Y = 114;
    private static final int NET_BTN_Y = 130;

    private BE_ContainerCraftingTerminal containerCraft;
    private BE_TileCraftingTerminal tile;
    private int scrollOffset = 0;
    private String searchText = "";
    private boolean searchFocused = false;
    private int screenMouseX, screenMouseY;

    public BE_GuiCraftingTerminal(InventoryPlayer playerInv, BE_TileCraftingTerminal tile) {
        super(new BE_ContainerCraftingTerminal(playerInv, tile));
        this.containerCraft = (BE_ContainerCraftingTerminal) this.inventorySlots;
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 240;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.screenMouseX = mouseX;
        this.screenMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        // Title
        this.fontRenderer.drawString("Crafting Terminal", 44, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);

        // Sort button
        {
            String sortLabel = BE_ContainerCraftingTerminal.SORT_NAMES[containerCraft.getSortMode()];
            drawRect(SORT_BTN_X, SORT_BTN_Y, SORT_BTN_X + SORT_BTN_W, SORT_BTN_Y + SORT_BTN_H, 0xFF555555);
            drawRect(SORT_BTN_X, SORT_BTN_Y, SORT_BTN_X + SORT_BTN_W - 1, SORT_BTN_Y + 1, 0xFFFFFFFF);
            drawRect(SORT_BTN_X, SORT_BTN_Y, SORT_BTN_X + 1, SORT_BTN_Y + SORT_BTN_H - 1, 0xFFFFFFFF);
            drawRect(SORT_BTN_X + 1, SORT_BTN_Y + 1, SORT_BTN_X + SORT_BTN_W - 1, SORT_BTN_Y + SORT_BTN_H - 1, 0xFFA0A0A0);
            int labelW = this.fontRenderer.getStringWidth(sortLabel);
            this.fontRenderer.drawString(sortLabel, SORT_BTN_X + (SORT_BTN_W - labelW) / 2, SORT_BTN_Y + 1, 0xFFFFFF);
        }

        // Search box text
        String displayText = searchFocused ? searchText + "_" : (searchText.isEmpty() ? "Search..." : searchText);
        int textColor = searchFocused ? 0xFFFFFF : 0xA0A0A0;
        this.fontRenderer.drawString(displayText, 100, 6, textColor);

        // Arrow between craft grid and result
        this.fontRenderer.drawString("\u2192", 76, 118, 4210752);

        // Button labels
        drawButtonLabel("Fill", BTN_X, FILL_BTN_Y);
        drawButtonLabel("Clear", BTN_X, CLEAR_BTN_Y);
        drawButtonLabel("\u2192Net", BTN_X, NET_BTN_Y);

        // Status text (craftable indicator / request status)
        String status = containerCraft.getStatusText();
        if (status != null && !status.isEmpty()) {
            // Truncate if too long
            if (status.length() > 28) status = status.substring(0, 25) + "...";
            this.fontRenderer.drawString(status, 8, 92, 0xFFAA00);
        }

        // Tooltip for hovered network grid item
        containerCraft.refreshItems();
        List<BE_ContainerCraftingTerminal.BE_GridEntry> items = getFilteredItems();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (this.mc.thePlayer.inventory.getItemStack() == null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int relX = this.screenMouseX - guiLeft;
            int relY = this.screenMouseY - guiTop;

            if (relX >= GRID_X && relX < GRID_X + GRID_COLS * CELL_SIZE
                && relY >= GRID_Y && relY < GRID_Y + GRID_ROWS * CELL_SIZE) {
                int col = (relX - GRID_X) / CELL_SIZE;
                int row = (relY - GRID_Y) / CELL_SIZE;
                int index = (scrollOffset + row) * GRID_COLS + col;
                if (index >= 0 && index < items.size()) {
                    BE_ContainerCraftingTerminal.BE_GridEntry entry = items.get(index);
                    Item item = Item.itemsList[entry.key.itemId];
                    if (item != null) {
                        String name = ("" + StringTranslate.getInstance().translateNamedKey(
                            item.getItemNameIS(new ItemStack(entry.key.itemId, 1, entry.key.damageValue)))).trim();
                        if (name.length() > 0) {
                            int tx = relX + 12;
                            int ty = relY - 12;
                            int tw = this.fontRenderer.getStringWidth(name);
                            this.drawGradientRect(tx - 3, ty - 3, tx + tw + 3, ty + 8 + 3, -1073741824, -1073741824);
                            this.fontRenderer.drawStringWithShadow(name, tx, ty, -1);
                        }
                    }
                }
            }
        }
    }

    private void drawButtonLabel(String label, int bx, int by) {
        int textWidth = this.fontRenderer.getStringWidth(label);
        this.fontRenderer.drawString(label, bx + (BTN_W - textWidth) / 2, by + 2, 4210752);
    }

    private void renderItemCount(int count, int x, int y) {
        if (count <= 1) return;
        String text = formatCount(count);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        float scale = 0.60F;
        int textWidth = this.fontRenderer.getStringWidth(text);
        int tx = (int)((x + 16 - textWidth * scale) / scale);
        int ty = (int)((y + 11) / scale);
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);
        this.fontRenderer.drawStringWithShadow(text, tx, ty, 0xFFFFFF);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private String formatCount(int count) {
        if (count >= 1000000) {
            int m = count / 1000000;
            if (m >= 10) return m + "M";
            int frac = (count / 100000) % 10;
            return m + "." + frac + "M";
        }
        if (count >= 1000) {
            int k = count / 1000;
            if (k >= 10) return k + "K";
            int frac = (count / 100) % 10;
            return k + "." + frac + "K";
        }
        return String.valueOf(count);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Background fill
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);

        // Border
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);

        // Title bar area
        drawRect(x + 4, y + 4, x + xSize - 4, y + 16, 0xFF8B8B8B);

        // Search box background
        drawRect(x + 96, y + 3, x + 170, y + 16, 0xFF373737);
        drawRect(x + 97, y + 4, x + 169, y + 15, 0xFF000000);

        // Network grid area (4 rows x 9 cols)
        drawRect(x + GRID_X - 1, y + GRID_Y - 1,
                 x + GRID_X + GRID_COLS * CELL_SIZE + 1, y + GRID_Y + GRID_ROWS * CELL_SIZE + 1,
                 0xFF373737);
        drawRect(x + GRID_X, y + GRID_Y,
                 x + GRID_X + GRID_COLS * CELL_SIZE, y + GRID_Y + GRID_ROWS * CELL_SIZE,
                 0xFF8B8B8B);

        // Crafting area separator line
        drawRect(x + 4, y + 94, x + xSize - 4, y + 95, 0xFF8B8B8B);

        // 3x3 craft grid slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawSlotBackground(x + 11 + col * 18, y + 95 + row * 18);
            }
        }

        // Result slot background
        drawSlotBackground(x + 93, y + 113);

        // Action buttons (3D look)
        drawButton(x + BTN_X, y + FILL_BTN_Y, BTN_W, BTN_H);
        drawButton(x + BTN_X, y + CLEAR_BTN_Y, BTN_W, BTN_H);
        drawButton(x + BTN_X, y + NET_BTN_Y, BTN_W, BTN_H);

        // Player inventory slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBackground(x + 7 + col * 18, y + 157 + row * 18);
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            drawSlotBackground(x + 7 + col * 18, y + 215);
        }

        // Render network grid items
        containerCraft.refreshItems();
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)x, (float)y, 0.0F);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            List<BE_ContainerCraftingTerminal.BE_GridEntry> bgItems = getFilteredItems();
            int startIndex = scrollOffset * GRID_COLS;
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    int index = startIndex + row * GRID_COLS + col;
                    if (index < bgItems.size()) {
                        BE_ContainerCraftingTerminal.BE_GridEntry entry = bgItems.get(index);
                        int ix = GRID_X + col * CELL_SIZE;
                        int iy = GRID_Y + row * CELL_SIZE;
                        ItemStack stack = new ItemStack(entry.key.itemId, entry.count, entry.key.damageValue);
                        if (stack.getItem() != null) {
                            gridItemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, ix, iy);
                            renderItemCount(entry.count, ix, iy);
                        }
                    }
                }
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();

            // Hover highlight on network grid
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int relX = screenMouseX - x;
            int relY = screenMouseY - y;
            if (relX >= GRID_X && relX < GRID_X + GRID_COLS * CELL_SIZE
                && relY >= GRID_Y && relY < GRID_Y + GRID_ROWS * CELL_SIZE) {
                int hcol = (relX - GRID_X) / CELL_SIZE;
                int hrow = (relY - GRID_Y) / CELL_SIZE;
                int hx = GRID_X + hcol * CELL_SIZE;
                int hy = GRID_Y + hrow * CELL_SIZE;
                drawGradientRect(hx, hy, hx + 16, hy + 16, -2130706433, -2130706433);
            }
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            GL11.glPopMatrix();
        }

        // Scrollbar (minimal, right side of grid area)
        List<BE_ContainerCraftingTerminal.BE_GridEntry> items = getFilteredItems();
        int totalRows = (items.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, totalRows - GRID_ROWS);
        if (maxScroll > 0) {
            int trackX = x + GRID_X + GRID_COLS * CELL_SIZE + 2;
            int trackY = y + GRID_Y;
            int trackW = 4;
            int trackH = GRID_ROWS * CELL_SIZE;
            drawRect(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF373737);
            int thumbH = Math.max(6, trackH * GRID_ROWS / totalRows);
            int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;
            drawRect(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xFFC6C6C6);
        }
    }

    private void drawSlotBackground(int sx, int sy) {
        drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
    }

    private void drawButton(int bx, int by, int bw, int bh) {
        drawRect(bx, by, bx + bw, by + bh, 0xFFAAAAAA);
        drawRect(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        drawRect(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        drawRect(bx + bw - 1, by, bx + bw, by + bh, 0xFF555555);
        drawRect(bx, by + bh - 1, bx + bw, by + bh, 0xFF555555);
    }

    private List<BE_ContainerCraftingTerminal.BE_GridEntry> getFilteredItems() {
        if (searchText.isEmpty()) return containerCraft.getItems();
        List<BE_ContainerCraftingTerminal.BE_GridEntry> filtered =
            new java.util.ArrayList<BE_ContainerCraftingTerminal.BE_GridEntry>();
        String query = searchText.toLowerCase();
        for (BE_ContainerCraftingTerminal.BE_GridEntry entry : containerCraft.getItems()) {
            Item item = Item.itemsList[entry.key.itemId];
            if (item != null) {
                String name = item.getItemName();
                if (name != null && name.toLowerCase().contains(query)) {
                    filtered.add(entry);
                }
            }
        }
        return filtered;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;

        // Sort button
        if (relX >= SORT_BTN_X && relX < SORT_BTN_X + SORT_BTN_W
            && relY >= SORT_BTN_Y && relY < SORT_BTN_Y + SORT_BTN_H) {
            containerCraft.cycleSortMode();
            containerCraft.refreshItems();
            return;
        }

        // Search box
        int searchBoxX = 97;
        int searchBoxY = 4;
        if (relX >= searchBoxX && relX < searchBoxX + 72 && relY >= searchBoxY && relY < searchBoxY + 12) {
            searchFocused = true;
            return;
        } else if (relX >= 0 && relX < xSize && relY >= 0 && relY < ySize) {
            searchFocused = false;
        }

        // Fill button
        if (relX >= BTN_X && relX < BTN_X + BTN_W && relY >= FILL_BTN_Y && relY < FILL_BTN_Y + BTN_H) {
            containerCraft.sendAction(BE_ContainerCraftingTerminal.ACTION_FILL);
            return;
        }

        // Clear button
        if (relX >= BTN_X && relX < BTN_X + BTN_W && relY >= CLEAR_BTN_Y && relY < CLEAR_BTN_Y + BTN_H) {
            containerCraft.sendAction(BE_ContainerCraftingTerminal.ACTION_CLEAR);
            return;
        }

        // Craft-to-Network button
        if (relX >= BTN_X && relX < BTN_X + BTN_W && relY >= NET_BTN_Y && relY < NET_BTN_Y + BTN_H) {
            containerCraft.sendAction(BE_ContainerCraftingTerminal.ACTION_CRAFT_TO_NET);
            return;
        }

        // Network grid click
        int gridLeft = GRID_X;
        int gridTop = GRID_Y;
        int gridRight = gridLeft + GRID_COLS * CELL_SIZE;
        int gridBottom = gridTop + GRID_ROWS * CELL_SIZE;

        if (relX >= gridLeft && relX < gridRight && relY >= gridTop && relY < gridBottom) {
            int col = (relX - gridLeft) / CELL_SIZE;
            int row = (relY - gridTop) / CELL_SIZE;
            int index = (scrollOffset + row) * GRID_COLS + col;

            List<BE_ContainerCraftingTerminal.BE_GridEntry> items = getFilteredItems();
            BE_ItemKey key = null;
            if (index >= 0 && index < items.size()) {
                key = items.get(index).key;
            }

            boolean shiftHeld = isShiftKeyDown();
            containerCraft.handleGridClick(key, button, shiftHeld, this.mc.thePlayer);
            containerCraft.refreshItems();
            return;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (searchFocused) {
            if (keyCode == 14 && searchText.length() > 0) {
                searchText = searchText.substring(0, searchText.length() - 1);
                scrollOffset = 0;
                return;
            } else if (keyCode == 1) {
                searchFocused = false;
                return;
            } else if (keyCode == 28) {
                searchFocused = false;
                return;
            } else if (c >= ' ' && c < 127) {
                searchText += c;
                scrollOffset = 0;
                return;
            }
        }
        super.keyTyped(c, keyCode);
    }

    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0) {
            List<BE_ContainerCraftingTerminal.BE_GridEntry> items = getFilteredItems();
            int totalRows = (items.size() + GRID_COLS - 1) / GRID_COLS;
            int maxScroll = Math.max(0, totalRows - GRID_ROWS);
            if (scroll < 0) {
                scrollOffset = Math.min(scrollOffset + 1, maxScroll);
            } else {
                scrollOffset = Math.max(scrollOffset - 1, 0);
            }
        }
    }

    private static boolean isShiftKeyDown() {
        return org.lwjgl.input.Keyboard.isKeyDown(42) || org.lwjgl.input.Keyboard.isKeyDown(54);
    }
}
