package betaenergistics.gui;

import betaenergistics.container.BE_ContainerGrid;
import betaenergistics.crafting.BE_CraftingPlan;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileGrid;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

/**
 * Grid Terminal GUI — scrollable grid of network items.
 * Supports three view modes:
 *   STORED (default): shows all items in network storage
 *   CRAFTABLE: shows items that can be auto-crafted via Autocrafter patterns
 *   PREVIEW: shows crafting plan (take/craft/missing) with confirm/cancel
 *
 * Tabs at the top switch between Stored and Craftable views.
 */
public class BE_GuiGrid extends BE_GuiTerminalBase {
    private static final String TEXTURE = "/gui/be_grid_terminal.png";
    private static final String[] SORT_TAB_LABELS = {"ID", "A-Z", "Qty"};
    private static final RenderItem gridItemRenderer = new RenderItem();

    // View tab constants
    private static final int VIEW_TAB_W = 44;
    private static final int VIEW_TAB_H = 13;

    // Preview layout
    private static final int PLAN_X = 8;
    private static final int PLAN_Y = 32;
    private static final int PLAN_ROW_H = 14;
    private static final int PLAN_ROWS = 8;
    private static final int QTY_BTN_W = 12;
    private static final int QTY_BTN_H = 10;

    protected BE_ContainerGrid containerGrid;
    private BE_TileGrid tileGrid;
    private int planScrollOffset = 0;

    public BE_GuiGrid(InventoryPlayer playerInv, BE_TileGrid grid) {
        super(new BE_ContainerGrid(playerInv, grid));
        this.containerGrid = (BE_ContainerGrid) this.inventorySlots;
        this.tileGrid = grid;
    }

    // ====== Abstract implementations ======

    @Override
    protected String getTerminalTitle() {
        int vm = containerGrid.getViewMode();
        if (vm == BE_ContainerGrid.VIEW_PREVIEW) {
            BE_ItemKey sel = containerGrid.getSelectedItem();
            if (sel != null) {
                Item item = Item.itemsList[sel.itemId];
                if (item != null) {
                    String name = ("" + StringTranslate.getInstance().translateNamedKey(
                        item.getItemNameIS(new ItemStack(sel.itemId, 1, sel.damageValue)))).trim();
                    return "Request: " + name;
                }
            }
            return "Request";
        }
        return "Grid Terminal";
    }

    @Override
    protected String getTexturePath() { return TEXTURE; }

    @Override
    protected boolean hasSortTabs() {
        return containerGrid.getViewMode() == BE_ContainerGrid.VIEW_STORED;
    }

    @Override
    protected String[] getSortTabLabels() { return SORT_TAB_LABELS; }

    @Override
    protected int getActiveSortTab() { return containerGrid.getSortMode(); }

    @Override
    protected void onSortTabClicked(int tabIndex) {
        containerGrid.setSortMode(tabIndex);
        containerGrid.refreshItems();
    }

    @Override
    protected boolean needsItemLighting() { return true; }

    @Override
    protected void refreshData() {
        int vm = containerGrid.getViewMode();
        if (vm == BE_ContainerGrid.VIEW_STORED) {
            containerGrid.refreshItems();
        } else if (vm == BE_ContainerGrid.VIEW_CRAFTABLE) {
            containerGrid.refreshCraftableItems();
        }
    }

    @Override
    protected int getEntryCount() {
        int vm = containerGrid.getViewMode();
        if (vm == BE_ContainerGrid.VIEW_CRAFTABLE) {
            return getFilteredCraftable().size();
        }
        if (vm == BE_ContainerGrid.VIEW_PREVIEW) {
            return 0; // Preview doesn't use grid
        }
        return getFilteredItems().size();
    }

    @Override
    protected void renderEntryAt(int x, int y, int index) {
        if (containerGrid.getViewMode() == BE_ContainerGrid.VIEW_CRAFTABLE) {
            List<BE_ContainerGrid.CraftableEntry> items = getFilteredCraftable();
            if (index >= items.size()) return;
            BE_ContainerGrid.CraftableEntry entry = items.get(index);
            ItemStack stack = new ItemStack(entry.key.itemId, 1, entry.key.damageValue);
            if (stack.getItem() != null) {
                gridItemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, x, y);
            }
            return;
        }
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        if (index >= items.size()) return;
        BE_ContainerGrid.BE_GridEntry entry = items.get(index);
        ItemStack stack = new ItemStack(entry.key.itemId, entry.count, entry.key.damageValue);
        if (stack.getItem() != null) {
            gridItemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, stack, x, y);
        }
    }

    @Override
    protected void renderEntryAmount(int index, int x, int y) {
        if (containerGrid.getViewMode() == BE_ContainerGrid.VIEW_CRAFTABLE) {
            // Craftable items show "Craft" indicator
            renderScaledAmount("Craft", x, y);
            return;
        }
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        if (index >= items.size()) return;
        int count = items.get(index).count;
        if (count <= 1) return;
        renderScaledAmount(formatItemCount(count), x, y);
    }

    @Override
    protected String getEntryTooltip(int index) {
        if (containerGrid.getViewMode() == BE_ContainerGrid.VIEW_CRAFTABLE) {
            List<BE_ContainerGrid.CraftableEntry> items = getFilteredCraftable();
            if (index >= items.size()) return null;
            return getItemName(items.get(index).key);
        }
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        if (index >= items.size()) return null;
        return getItemName(items.get(index).key);
    }

    @Override
    protected void handleGridClick(int index, int mouseButton) {
        if (containerGrid.getViewMode() == BE_ContainerGrid.VIEW_CRAFTABLE) {
            List<BE_ContainerGrid.CraftableEntry> items = getFilteredCraftable();
            if (index >= 0 && index < items.size()) {
                BE_ItemKey key = items.get(index).key;
                containerGrid.sendRequestAction(BE_ContainerGrid.ACTION_SELECT, key, 1);
                planScrollOffset = 0;
            }
            return;
        }
        // Stored mode: normal item interaction
        List<BE_ContainerGrid.BE_GridEntry> items = getFilteredItems();
        BE_ItemKey key = null;
        if (index >= 0 && index < items.size()) {
            key = items.get(index).key;
        }
        boolean shiftHeld = isShiftKeyDown();
        containerGrid.handleGridClick(key, mouseButton, shiftHeld, this.mc.thePlayer);
        containerGrid.refreshItems();
    }

    // ====== View tabs + Preview rendering ======

    @Override
    protected void drawGuiContainerForegroundLayer() {
        int vm = containerGrid.getViewMode();
        if (vm == BE_ContainerGrid.VIEW_PREVIEW) {
            drawPreviewForeground();
            this.fontRenderer.drawString("Inventory", 7, this.ySize - 96 + 2, 4210752);
            return;
        }
        super.drawGuiContainerForegroundLayer();

        // Crafting monitor — show pending crafts count
        int pending = tileGrid.getTotalPendingCrafts();
        if (pending > 0) {
            String status = "Crafting: " + pending;
            int sw = this.fontRenderer.getStringWidth(status);
            this.fontRenderer.drawString(status, xSize - sw - 7, ySize - 96 + 2, 0x44AA44);
        }

        // Draw view tab labels (foreground uses relative coords, font works here)
        drawViewTabLabels();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick) {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw view tab backgrounds FIRST (behind GUI texture)
        drawViewTabBGs(x, y);

        int vm = containerGrid.getViewMode();
        if (vm == BE_ContainerGrid.VIEW_PREVIEW) {
            drawPreviewBackground(partialTick);
            return;
        }
        // Draw base (texture + sort tabs + grid + scrollbar)
        super.drawGuiContainerBackgroundLayer(partialTick);
    }

    private void drawViewTabBGs(int guiX, int guiY) {
        int vm = containerGrid.getViewMode();
        int tab1X = guiX + 8;
        int tab2X = guiX + 8 + VIEW_TAB_W + 2;
        int tabY = guiY - VIEW_TAB_H + 1;

        drawViewTabBG(tab1X, tabY, VIEW_TAB_W, VIEW_TAB_H, vm == BE_ContainerGrid.VIEW_STORED);
        drawViewTabBG(tab2X, tabY, VIEW_TAB_W + 8, VIEW_TAB_H, vm == BE_ContainerGrid.VIEW_CRAFTABLE || vm == BE_ContainerGrid.VIEW_PREVIEW);
    }

    private void drawViewTabBG(int tx, int ty, int tw, int th, boolean active) {
        int BG = active ? 0xFFC6C6C6 : 0xFF8B8B8B;
        int WH = 0xFFFFFFFF;
        int DK = 0xFF555555;

        // Fill
        drawRect(tx, ty + 2, tx + tw, ty + th + 1, BG);
        // Top edge (black)
        drawRect(tx + 2, ty, tx + tw - 2, ty + 1, 0xFF000000);
        // Corners
        drawRect(tx + 1, ty + 1, tx + 2, ty + 2, 0xFF000000);
        drawRect(tx + tw - 2, ty + 1, tx + tw - 1, ty + 2, 0xFF000000);
        // Left border
        drawRect(tx, ty + 2, tx + 1, ty + th, 0xFF000000);
        // Right border
        drawRect(tx + tw - 1, ty + 2, tx + tw, ty + th, 0xFF000000);
        // Highlights
        drawRect(tx + 1, ty + 2, tx + 2, ty + th, WH);
        drawRect(tx + 2, ty + 1, tx + tw - 2, ty + 2, WH);
        // Bottom: active = merge (no border), inactive = dark line
        if (!active) {
            drawRect(tx, ty + th, tx + tw, ty + th + 1, 0xFF000000);
        }
    }

    /** Draw tab labels in foreground layer (relative coords, font renderer works) */
    private void drawViewTabLabels() {
        int vm = containerGrid.getViewMode();
        int tab1X = 8;
        int tab2X = 8 + VIEW_TAB_W + 2;
        int tabY = -VIEW_TAB_H + 1;

        drawTabLabel(tab1X, tabY, VIEW_TAB_W, VIEW_TAB_H, "Stored", vm == BE_ContainerGrid.VIEW_STORED);
        drawTabLabel(tab2X, tabY, VIEW_TAB_W + 8, VIEW_TAB_H, "Craftable", vm == BE_ContainerGrid.VIEW_CRAFTABLE || vm == BE_ContainerGrid.VIEW_PREVIEW);
    }

    private void drawTabLabel(int tx, int ty, int tw, int th, String label, boolean active) {
        int labelW = this.fontRenderer.getStringWidth(label);
        int labelX = tx + (tw - labelW) / 2;
        int labelY = ty + (th - 8) / 2 + 1;
        this.fontRenderer.drawString(label, labelX, labelY, active ? 0x404040 : 0xD0D0D0);
    }

    // ====== Preview mode ======

    private void drawPreviewForeground() {
        // Title
        String title = getTerminalTitle();
        this.fontRenderer.drawString(title, 8, 6, 4210752);

        // Quantity
        String qtyStr = "x" + containerGrid.getRequestQuantity();
        this.fontRenderer.drawString(qtyStr, 135, 8, 4210752);

        // +/- buttons
        drawMiniButton(120, 7, QTY_BTN_W, QTY_BTN_H, "-");
        drawMiniButton(152, 7, QTY_BTN_W, QTY_BTN_H, "+");

        // Plan header
        this.fontRenderer.drawString("Crafting Plan:", 8, 22, 4210752);

        // Plan entries
        List<BE_ContainerGrid.PlanEntry> entries = containerGrid.getPlanEntries();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int visibleEnd = Math.min(entries.size(), planScrollOffset + PLAN_ROWS);
        for (int i = planScrollOffset; i < visibleEnd; i++) {
            BE_ContainerGrid.PlanEntry entry = entries.get(i);
            int py = PLAN_Y + (i - planScrollOffset) * PLAN_ROW_H;

            int color;
            String prefix;
            switch (entry.type) {
                case BE_ContainerGrid.PlanEntry.TYPE_TAKE:
                    color = 0x00AA00; prefix = "[TAKE] "; break;
                case BE_ContainerGrid.PlanEntry.TYPE_CRAFT:
                    color = 0xAAAA00; prefix = "[CRAFT] "; break;
                default:
                    color = 0xAA0000; prefix = "[MISS] "; break;
            }

            String name = getItemName(entry.key);
            String line = prefix + name + " x" + entry.count;
            if (this.fontRenderer.getStringWidth(line) > 160) {
                while (this.fontRenderer.getStringWidth(line + "..") > 160 && line.length() > 10)
                    line = line.substring(0, line.length() - 1);
                line += "..";
            }
            this.fontRenderer.drawString(line, PLAN_X, py, color);
        }

        // Status
        BE_CraftingPlan plan = containerGrid.getCurrentPlan();
        if (plan != null) {
            String status = plan.isComplete() ? "Ready to craft" : "Missing items!";
            int statusColor = plan.isComplete() ? 0x00AA00 : 0xAA0000;
            this.fontRenderer.drawString(status, 8, 148, statusColor);
        }

        // Confirm / Cancel buttons
        BE_GuiUtils.drawButton(this.fontRenderer, 8, 155, 76, 14, "Confirm", screenMouseX - (width - xSize) / 2, screenMouseY - (height - ySize) / 2);
        BE_GuiUtils.drawButton(this.fontRenderer, 92, 155, 76, 14, "Cancel", screenMouseX - (width - xSize) / 2, screenMouseY - (height - ySize) / 2);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawPreviewBackground(float partialTick) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int texId = this.mc.renderEngine.getTexture(getTexturePath());
        this.mc.renderEngine.bindTexture(texId);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // View tabs
        drawViewTabBGs(x, y);

        // Preview area overlay (darker background over grid area)
        drawRect(x + 4, y + 18, x + 172, y + 152, 0xFF8B8B8B);
        drawRect(x + 5, y + 19, x + 171, y + 151, 0xFFC6C6C6);
    }

    private void drawMiniButton(int bx, int by, int bw, int bh, String label) {
        drawRect(bx, by, bx + bw, by + bh, 0xFF555555);
        drawRect(bx, by, bx + bw - 1, by + 1, 0xFFFFFFFF);
        drawRect(bx, by, bx + 1, by + bh - 1, 0xFFFFFFFF);
        drawRect(bx + 1, by + 1, bx + bw - 1, by + bh - 1, 0xFFA0A0A0);
        int labelW = this.fontRenderer.getStringWidth(label);
        this.fontRenderer.drawString(label, bx + (bw - labelW) / 2, by + 1, 0xFFFFFF);
    }

    // ====== Input handling overrides ======

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;

        // View tab clicks (above GUI)
        int vm = containerGrid.getViewMode();
        int tab1X = 8;
        int tab2X = 8 + VIEW_TAB_W + 2;
        int tabY = -VIEW_TAB_H + 1;
        if (relY >= tabY && relY < tabY + VIEW_TAB_H) {
            if (relX >= tab1X && relX < tab1X + VIEW_TAB_W && vm != BE_ContainerGrid.VIEW_STORED) {
                containerGrid.setViewMode(BE_ContainerGrid.VIEW_STORED);
                scrollOffset = 0;
                return;
            }
            if (relX >= tab2X && relX < tab2X + VIEW_TAB_W + 8 && vm != BE_ContainerGrid.VIEW_CRAFTABLE) {
                containerGrid.setViewMode(BE_ContainerGrid.VIEW_CRAFTABLE);
                scrollOffset = 0;
                return;
            }
        }

        // Preview mode clicks
        if (vm == BE_ContainerGrid.VIEW_PREVIEW) {
            // Quantity minus
            if (relX >= 120 && relX < 132 && relY >= 7 && relY < 17) {
                containerGrid.sendRequestAction(BE_ContainerGrid.ACTION_DEC_QTY, null, 0);
                return;
            }
            // Quantity plus
            if (relX >= 152 && relX < 164 && relY >= 7 && relY < 17) {
                containerGrid.sendRequestAction(BE_ContainerGrid.ACTION_INC_QTY, null, 0);
                return;
            }
            // Confirm button
            if (relX >= 8 && relX < 84 && relY >= 155 && relY < 169) {
                containerGrid.sendRequestAction(BE_ContainerGrid.ACTION_CONFIRM, null, 0);
                return;
            }
            // Cancel button
            if (relX >= 92 && relX < 168 && relY >= 155 && relY < 169) {
                containerGrid.cancelPreview();
                scrollOffset = 0;
                return;
            }
            // Don't process grid clicks in preview mode
            super.mouseClicked(mouseX, mouseY, button);
            return;
        }

        // Default: sort tabs + search + grid clicks
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleMouseInput() {
        int vm = containerGrid.getViewMode();
        if (vm == BE_ContainerGrid.VIEW_PREVIEW) {
            // Scroll plan entries
            super.handleMouseInput();
            int scroll = org.lwjgl.input.Mouse.getDWheel();
            if (scroll != 0) {
                List<BE_ContainerGrid.PlanEntry> entries = containerGrid.getPlanEntries();
                int maxScroll = Math.max(0, entries.size() - PLAN_ROWS);
                if (scroll < 0) {
                    planScrollOffset = Math.min(planScrollOffset + 1, maxScroll);
                } else {
                    planScrollOffset = Math.max(planScrollOffset - 1, 0);
                }
            }
            return;
        }
        super.handleMouseInput();
    }

    // ====== Scrollbar ======

    @Override
    protected void drawScrollbarThumb(int trackX, int thumbY, int trackW, int thumbH) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        Tessellator t = Tessellator.instance;
        fillSolid(t, trackX + 1, thumbY + 1, trackX + trackW - 1, thumbY + thumbH - 1, 198, 198, 198);
        fillSolid(t, trackX, thumbY, trackX + trackW - 1, thumbY + 1, 255, 255, 255);
        fillSolid(t, trackX, thumbY, trackX + 1, thumbY + thumbH - 1, 255, 255, 255);
        fillSolid(t, trackX + 1, thumbY + thumbH - 1, trackX + trackW, thumbY + thumbH, 85, 85, 85);
        fillSolid(t, trackX + trackW - 1, thumbY + 1, trackX + trackW, thumbY + thumbH, 85, 85, 85);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void fillSolid(Tessellator t, int x1, int y1, int x2, int y2, int r, int g, int b) {
        t.startDrawingQuads();
        t.setColorOpaque(r, g, b);
        t.addVertex(x1, y2, 0.0);
        t.addVertex(x2, y2, 0.0);
        t.addVertex(x2, y1, 0.0);
        t.addVertex(x1, y1, 0.0);
        t.draw();
    }

    // ====== Helpers ======

    private static String getItemName(BE_ItemKey key) {
        Item item = Item.itemsList[key.itemId];
        if (item == null) return "???";
        String name = ("" + StringTranslate.getInstance().translateNamedKey(
            item.getItemNameIS(new ItemStack(key.itemId, 1, key.damageValue)))).trim();
        return name.length() > 0 ? name : "???";
    }

    private List<BE_ContainerGrid.BE_GridEntry> getFilteredItems() {
        if (searchText.isEmpty()) return containerGrid.getItems();
        List<BE_ContainerGrid.BE_GridEntry> filtered = new java.util.ArrayList<BE_ContainerGrid.BE_GridEntry>();
        String query = searchText.toLowerCase();
        for (BE_ContainerGrid.BE_GridEntry entry : containerGrid.getItems()) {
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

    private List<BE_ContainerGrid.CraftableEntry> getFilteredCraftable() {
        if (searchText.isEmpty()) return containerGrid.getCraftableItems();
        List<BE_ContainerGrid.CraftableEntry> filtered = new java.util.ArrayList<BE_ContainerGrid.CraftableEntry>();
        String query = searchText.toLowerCase();
        for (BE_ContainerGrid.CraftableEntry entry : containerGrid.getCraftableItems()) {
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
}
