package betaenergistics.tile;

import net.minecraft.src.*;

/**
 * Crafting Terminal — extends Grid with a 3x3 crafting matrix.
 * The crafting matrix is a local inventory (not stored in network).
 */
public class BE_TileCraftingTerminal extends BE_TileGrid implements IInventory {
    private ItemStack[] craftMatrix = new ItemStack[9];
    private ItemStack craftResult = null;

    // IInventory for the 3x3 crafting matrix
    @Override
    public int getSizeInventory() { return 9; }

    @Override
    public ItemStack getStackInSlot(int slot) { return craftMatrix[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (craftMatrix[slot] == null) return null;
        if (craftMatrix[slot].stackSize <= amount) {
            ItemStack stack = craftMatrix[slot];
            craftMatrix[slot] = null;
            return stack;
        }
        return craftMatrix[slot].splitStack(amount);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        craftMatrix[slot] = stack;
    }

    @Override
    public String getInvName() { return "BE Crafting Terminal"; }

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

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("CraftMatrix");
        craftMatrix = new ItemStack[9];
        for (int i = 0; i < list.tagCount() && i < 9; i++) {
            NBTTagCompound slotTag = (NBTTagCompound) list.tagAt(i);
            if (slotTag.hasKey("id")) {
                craftMatrix[i] = new ItemStack(slotTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 9; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            if (craftMatrix[i] != null) {
                craftMatrix[i].writeToNBT(slotTag);
            }
            list.setTag(slotTag);
        }
        tag.setTag("CraftMatrix", list);
    }
}
