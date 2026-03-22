package betaenergistics.tile;

import betaenergistics.item.BE_ItemPattern;
import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_PatternRegistry;

import net.minecraft.src.*;

/**
 * Recipe Encoder — defines crafting/processing patterns for use in the Autocrafter.
 *
 * Slot layout:
 *   0-8:  ghost input grid (3x3) — store ItemKey reference, not real items
 *   9:    ghost output result
 *   10:   real slot for blank pattern INPUT
 *   11:   real slot for encoded pattern OUTPUT
 */
public class BE_TileRecipeEncoder extends TileEntity implements IInventory, BE_INetworkNode {
    public static final int GHOST_SLOTS = 9;
    public static final int SLOT_OUTPUT = 9;
    public static final int SLOT_PATTERN_IN = 10;
    public static final int SLOT_PATTERN_OUT = 11;
    public static final int TOTAL_SLOTS = 12;

    private static final int ENERGY_USAGE = 1;

    /** false = crafting mode, true = processing mode */
    private boolean processingMode = false;

    private ItemStack[] ghostInputs = new ItemStack[GHOST_SLOTS];
    private ItemStack ghostOutput = null;
    private ItemStack patternInput = null;
    private ItemStack patternOutput = null;

    private BE_StorageNetwork network;

    public boolean isProcessingMode() { return processingMode; }

    public void toggleMode() {
        processingMode = !processingMode;
        if (!processingMode) {
            updateOutputPreview();
        }
    }

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
        if (!processingMode) {
            updateOutputPreview();
        }
    }

    /**
     * Set the output ghost slot directly (used in processing mode).
     */
    public void setOutputSlot(ItemStack stack) {
        if (stack != null) {
            ghostOutput = new ItemStack(stack.itemID, 1, stack.getItemDamage());
        } else {
            ghostOutput = null;
        }
    }

    /**
     * Update the output preview by querying CraftingManager (crafting mode only).
     */
    public void updateOutputPreview() {
        if (processingMode) return;
        InventoryCrafting tempCraft = new InventoryCrafting(new Container() {
            public boolean isUsableByPlayer(EntityPlayer p) { return false; }
        }, 3, 3);
        for (int i = 0; i < GHOST_SLOTS; i++) {
            tempCraft.setInventorySlotContents(i, ghostInputs[i]);
        }
        ghostOutput = CraftingManager.getInstance().findMatchingRecipe(tempCraft);
    }

    /**
     * Encode the current recipe into a blank pattern.
     * Consumes one blank pattern from patternInput, produces encoded pattern in patternOutput.
     */
    public boolean encode() {
        // Must have a blank pattern in the input slot
        if (patternInput == null) return false;
        if (!(patternInput.getItem() instanceof BE_ItemPattern)) return false;
        if (patternInput.getItemDamage() != 0) return false;

        // Must have an output defined
        if (ghostOutput == null) return false;

        // At least one input is required
        boolean hasInput = false;
        for (int i = 0; i < GHOST_SLOTS; i++) {
            if (ghostInputs[i] != null) { hasInput = true; break; }
        }
        if (!hasInput) return false;

        // Output slot must be empty
        if (patternOutput != null) return false;

        // Create pattern data and register
        int type = processingMode ? BE_PatternRegistry.TYPE_PROCESSING : BE_PatternRegistry.TYPE_CRAFTING;
        int patternId = BE_PatternRegistry.createPattern(ghostInputs, ghostOutput, type);

        // Create encoded pattern item
        patternOutput = new ItemStack(patternInput.getItem(), 1, patternId);

        // Consume one blank pattern
        patternInput.stackSize--;
        if (patternInput.stackSize <= 0) {
            patternInput = null;
        }

        onInventoryChanged();
        return true;
    }

    public ItemStack getGhostOutput() {
        return ghostOutput;
    }

    // IInventory implementation
    @Override
    public int getSizeInventory() { return TOTAL_SLOTS; }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot >= 0 && slot < GHOST_SLOTS) return ghostInputs[slot];
        if (slot == SLOT_OUTPUT) return ghostOutput;
        if (slot == SLOT_PATTERN_IN) return patternInput;
        if (slot == SLOT_PATTERN_OUT) return patternOutput;
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot >= 0 && slot < GHOST_SLOTS) {
            ItemStack old = ghostInputs[slot];
            ghostInputs[slot] = null;
            if (!processingMode) updateOutputPreview();
            return old;
        }
        if (slot == SLOT_OUTPUT) return null; // ghost, no real extraction
        if (slot == SLOT_PATTERN_IN) {
            if (patternInput == null) return null;
            if (amount >= patternInput.stackSize) {
                ItemStack old = patternInput;
                patternInput = null;
                return old;
            }
            ItemStack split = patternInput.splitStack(amount);
            if (patternInput.stackSize <= 0) patternInput = null;
            return split;
        }
        if (slot == SLOT_PATTERN_OUT) {
            ItemStack old = patternOutput;
            patternOutput = null;
            return old;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot >= 0 && slot < GHOST_SLOTS) {
            // Ghost slot: store copy with stackSize=1
            if (stack != null) {
                ghostInputs[slot] = new ItemStack(stack.itemID, 1, stack.getItemDamage());
            } else {
                ghostInputs[slot] = null;
            }
            if (!processingMode) updateOutputPreview();
        } else if (slot == SLOT_OUTPUT) {
            // Ghost output: store copy with stackSize=1
            if (stack != null) {
                ghostOutput = new ItemStack(stack.itemID, 1, stack.getItemDamage());
            } else {
                ghostOutput = null;
            }
        } else if (slot == SLOT_PATTERN_IN) {
            patternInput = stack;
        } else if (slot == SLOT_PATTERN_OUT) {
            patternOutput = stack;
        }
    }

    @Override
    public String getInvName() { return "BE Recipe Encoder"; }
    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void onInventoryChanged() {
        super.onInventoryChanged();
    }

    // BE_INetworkNode implementation
    @Override
    public int getEnergyUsage() { return ENERGY_USAGE; }
    @Override
    public void onNetworkJoin(BE_StorageNetwork network) { this.network = network; }
    @Override
    public void onNetworkLeave() { this.network = null; }
    @Override
    public BE_StorageNetwork getNetwork() { return network; }
    @Override
    public TileEntity getTileEntity() { return this; }
    @Override
    public boolean canConnectOnSide(int side) { return true; }

    // NBT persistence
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        processingMode = tag.getBoolean("ProcessingMode");

        // Load ghost inputs
        NBTTagList ghostList = tag.getTagList("GhostInputs");
        for (int i = 0; i < ghostList.tagCount() && i < GHOST_SLOTS; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) ghostList.tagAt(i);
            if (slotTag.hasKey("id")) {
                ghostInputs[i] = new ItemStack(slotTag);
            }
        }

        // Load ghost output
        if (tag.hasKey("GhostOutput")) {
            NBTTagCompound outTag = tag.getCompoundTag("GhostOutput");
            if (outTag.hasKey("id")) {
                ghostOutput = new ItemStack(outTag);
            }
        } else if (!processingMode) {
            updateOutputPreview();
        }

        // Load pattern input
        if (tag.hasKey("PatternInput")) {
            NBTTagCompound patTag = tag.getCompoundTag("PatternInput");
            if (patTag.hasKey("id")) {
                patternInput = new ItemStack(patTag);
            }
        }

        // Load pattern output
        if (tag.hasKey("PatternOutput")) {
            NBTTagCompound patTag = tag.getCompoundTag("PatternOutput");
            if (patTag.hasKey("id")) {
                patternOutput = new ItemStack(patTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setBoolean("ProcessingMode", processingMode);

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

        // Save ghost output
        if (ghostOutput != null) {
            NBTTagCompound outTag = new NBTTagCompound();
            ghostOutput.writeToNBT(outTag);
            tag.setTag("GhostOutput", outTag);
        }

        // Save pattern input
        NBTTagCompound patInTag = new NBTTagCompound();
        if (patternInput != null) {
            patternInput.writeToNBT(patInTag);
        }
        tag.setTag("PatternInput", patInTag);

        // Save pattern output
        NBTTagCompound patOutTag = new NBTTagCompound();
        if (patternOutput != null) {
            patternOutput.writeToNBT(patOutTag);
        }
        tag.setTag("PatternOutput", patOutTag);
    }
}
