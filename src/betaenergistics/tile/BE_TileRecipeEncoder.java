package betaenergistics.tile;

import betaenergistics.item.BE_ItemPattern;
import betaenergistics.storage.BE_PatternRegistry;

import net.minecraft.src.*;

/**
 * Recipe Encoder — defines crafting patterns for use in the Autocrafter.
 *
 * Has 9 ghost input slots (items not consumed, just references),
 * 1 output preview slot (auto-computed from CraftingManager),
 * and 1 pattern slot (blank pattern in → encoded pattern out).
 *
 * Slot layout:
 *   0-8:  ghost input grid (3x3)
 *   9:    output preview (read-only, auto-computed)
 *   10:   pattern slot (accepts BE_ItemPattern)
 */
public class BE_TileRecipeEncoder extends TileEntity implements IInventory {
    public static final int GHOST_SLOTS = 9;
    public static final int SLOT_OUTPUT = 9;
    public static final int SLOT_PATTERN = 10;
    public static final int TOTAL_SLOTS = 11;

    private ItemStack[] ghostInputs = new ItemStack[GHOST_SLOTS];
    private ItemStack outputPreview = null;
    private ItemStack patternSlot = null;

    /**
     * Set a ghost input slot (copy only, not consumed).
     */
    public void setGhostSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= GHOST_SLOTS) return;
        if (stack != null) {
            ghostInputs[slot] = new ItemStack(stack.itemID, 1, stack.getItemDamage());
        } else {
            ghostInputs[slot] = null;
        }
        updateOutputPreview();
    }

    /**
     * Update the output preview by querying CraftingManager.
     */
    public void updateOutputPreview() {
        // Build a temporary InventoryCrafting to query recipes
        InventoryCrafting tempCraft = new InventoryCrafting(new Container() {
            public boolean isUsableByPlayer(EntityPlayer p) { return false; }
        }, 3, 3);
        for (int i = 0; i < GHOST_SLOTS; i++) {
            tempCraft.setInventorySlotContents(i, ghostInputs[i]);
        }
        outputPreview = CraftingManager.getInstance().findMatchingRecipe(tempCraft);
    }

    /**
     * Encode the current recipe into a blank pattern.
     * Returns true if encoding succeeded.
     */
    public boolean encodePattern() {
        if (patternSlot == null) return false;
        if (!(patternSlot.getItem() instanceof BE_ItemPattern)) return false;
        if (outputPreview == null) return false;

        // Create pattern in registry
        int patternId = BE_PatternRegistry.createPattern(ghostInputs, outputPreview);

        // Replace blank pattern with encoded pattern
        patternSlot = new ItemStack(patternSlot.getItem(), 1, patternId);

        return true;
    }

    public ItemStack getOutputPreview() {
        return outputPreview;
    }

    // IInventory implementation
    @Override
    public int getSizeInventory() { return TOTAL_SLOTS; }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot >= 0 && slot < GHOST_SLOTS) return ghostInputs[slot];
        if (slot == SLOT_OUTPUT) return outputPreview;
        if (slot == SLOT_PATTERN) return patternSlot;
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot >= 0 && slot < GHOST_SLOTS) {
            ItemStack old = ghostInputs[slot];
            ghostInputs[slot] = null;
            updateOutputPreview();
            return old;
        }
        if (slot == SLOT_OUTPUT) return null; // read-only
        if (slot == SLOT_PATTERN) {
            ItemStack old = patternSlot;
            patternSlot = null;
            return old;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot >= 0 && slot < GHOST_SLOTS) {
            ghostInputs[slot] = stack;
            updateOutputPreview();
        } else if (slot == SLOT_PATTERN) {
            patternSlot = stack;
        }
        // SLOT_OUTPUT is read-only
    }

    @Override
    public String getInvName() { return "BE Recipe Encoder"; }
    @Override
    public int getInventoryStackLimit() { return 1; }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void onInventoryChanged() {
        super.onInventoryChanged();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        // Load ghost inputs
        NBTTagList ghostList = tag.getTagList("GhostInputs");
        for (int i = 0; i < ghostList.tagCount() && i < GHOST_SLOTS; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) ghostList.tagAt(i);
            if (slotTag.hasKey("id")) {
                ghostInputs[i] = new ItemStack(slotTag);
            }
        }

        // Load pattern slot
        if (tag.hasKey("PatternSlot")) {
            NBTTagCompound patTag = tag.getCompoundTag("PatternSlot");
            if (patTag.hasKey("id")) {
                patternSlot = new ItemStack(patTag);
            }
        }

        updateOutputPreview();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        // Save ghost inputs
        NBTTagList ghostList = new NBTTagList();
        for (int i = 0; i < GHOST_SLOTS; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (ghostInputs[i] != null) {
                ghostInputs[i].writeToNBT(slotTag);
            }
            ghostList.setTag(slotTag);
        }
        tag.setTag("GhostInputs", ghostList);

        // Save pattern slot
        NBTTagCompound patTag = new NBTTagCompound();
        if (patternSlot != null) {
            patternSlot.writeToNBT(patTag);
        }
        tag.setTag("PatternSlot", patTag);
    }
}
