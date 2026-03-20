package betaenergistics.storage;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps an external IInventory (chest, furnace, etc.) as a network storage.
 * Reads/writes directly to the external inventory — no buffer.
 * Used by Storage Bus.
 */
public class BE_ExternalStorage {
    private final IInventory inventory;
    private int priority = 0;
    private BE_AccessMode accessMode = BE_AccessMode.INSERT_EXTRACT;

    public BE_ExternalStorage(IInventory inventory) {
        this.inventory = inventory;
    }

    public int insert(BE_ItemKey key, int amount, boolean simulate) {
        if (!accessMode.allowsInsert() || amount <= 0) return 0;

        int remaining = amount;
        for (int slot = 0; slot < inventory.getSizeInventory() && remaining > 0; slot++) {
            ItemStack existing = inventory.getStackInSlot(slot);
            if (existing == null) {
                int toPlace = Math.min(remaining, inventory.getInventoryStackLimit());
                if (!simulate) {
                    inventory.setInventorySlotContents(slot, new ItemStack(key.itemId, toPlace, key.damageValue));
                }
                remaining -= toPlace;
            } else if (existing.itemID == key.itemId && existing.getItemDamage() == key.damageValue) {
                int maxStack = Math.min(existing.getMaxStackSize(), inventory.getInventoryStackLimit());
                int space = maxStack - existing.stackSize;
                int toPlace = Math.min(remaining, space);
                if (toPlace > 0) {
                    if (!simulate) {
                        existing.stackSize += toPlace;
                    }
                    remaining -= toPlace;
                }
            }
        }
        if (!simulate && remaining < amount) {
            inventory.onInventoryChanged();
        }
        return amount - remaining;
    }

    public int extract(BE_ItemKey key, int amount, boolean simulate) {
        if (!accessMode.allowsExtract() || amount <= 0) return 0;

        int remaining = amount;
        for (int slot = 0; slot < inventory.getSizeInventory() && remaining > 0; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null && stack.itemID == key.itemId && stack.getItemDamage() == key.damageValue) {
                int toTake = Math.min(remaining, stack.stackSize);
                if (!simulate) {
                    stack.stackSize -= toTake;
                    if (stack.stackSize <= 0) {
                        inventory.setInventorySlotContents(slot, null);
                    }
                }
                remaining -= toTake;
            }
        }
        if (!simulate && remaining < amount) {
            inventory.onInventoryChanged();
        }
        return amount - remaining;
    }

    public int getCount(BE_ItemKey key) {
        int total = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null && stack.itemID == key.itemId && stack.getItemDamage() == key.damageValue) {
                total += stack.stackSize;
            }
        }
        return total;
    }

    public Map<BE_ItemKey, Integer> getAll() {
        Map<BE_ItemKey, Integer> map = new HashMap<BE_ItemKey, Integer>();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null && stack.stackSize > 0) {
                BE_ItemKey key = new BE_ItemKey(stack.itemID, stack.getItemDamage());
                Integer existing = map.get(key);
                map.put(key, (existing != null ? existing : 0) + stack.stackSize);
            }
        }
        return map;
    }

    public int getStored() {
        int total = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null) total += stack.stackSize;
        }
        return total;
    }

    public int getCapacity() {
        return inventory.getSizeInventory() * inventory.getInventoryStackLimit();
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public BE_AccessMode getAccessMode() { return accessMode; }
    public void setAccessMode(BE_AccessMode mode) { this.accessMode = mode; }
    public IInventory getInventory() { return inventory; }
}
