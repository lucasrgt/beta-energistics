package betaenergistics.item;

import net.minecraft.src.*;

/**
 * Pattern item — stores a crafting recipe (3x3 input → 1 output).
 * Created in the Pattern Encoder, used by the Autocrafter.
 */
public class BE_ItemPattern extends Item {
    public BE_ItemPattern(int itemId) {
        super(itemId);
        setMaxStackSize(1);
    }

    /** Check if this pattern has a recipe encoded. */
    public static boolean isEncoded(ItemStack stack) {
        return stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("output");
    }

    /** Get the output item from a pattern. */
    public static ItemStack getOutput(ItemStack pattern) {
        if (!isEncoded(pattern)) return null;
        NBTTagCompound outputTag = pattern.stackTagCompound.getCompoundTag("output");
        return ItemStack.loadItemStackFromNBT(outputTag);
    }

    /** Get the input items (9 slots, some may be null). */
    public static ItemStack[] getInputs(ItemStack pattern) {
        ItemStack[] inputs = new ItemStack[9];
        if (!isEncoded(pattern)) return inputs;
        NBTTagList list = pattern.stackTagCompound.getTagList("inputs");
        for (int i = 0; i < list.tagCount() && i < 9; i++) {
            NBTTagCompound tag = (NBTTagCompound) list.tagAt(i);
            if (tag.hasKey("id")) {
                inputs[i] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
        return inputs;
    }

    /** Encode a recipe onto a pattern item. */
    public static void encode(ItemStack pattern, ItemStack[] inputs, ItemStack output) {
        if (pattern.stackTagCompound == null) {
            pattern.stackTagCompound = new NBTTagCompound();
        }
        // Save output
        NBTTagCompound outputTag = new NBTTagCompound();
        output.writeToNBT(outputTag);
        pattern.stackTagCompound.setCompoundTag("output", outputTag);

        // Save inputs
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 9; i++) {
            NBTTagCompound tag = new NBTTagCompound();
            if (i < inputs.length && inputs[i] != null) {
                inputs[i].writeToNBT(tag);
            }
            list.tagList.add(tag);
        }
        pattern.stackTagCompound.setTag("inputs", list);
    }

    @Override
    public String getItemNameIS(ItemStack stack) {
        if (isEncoded(stack)) {
            ItemStack output = getOutput(stack);
            if (output != null && output.getItem() != null) {
                return "Pattern: " + output.getItem().getItemName();
            }
        }
        return "Blank Pattern";
    }
}
