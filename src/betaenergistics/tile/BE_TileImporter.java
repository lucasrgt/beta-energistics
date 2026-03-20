package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.*;

/**
 * Import Bus — pulls items from adjacent inventory into the network.
 * Placed against a chest/furnace/etc, pulls 1 item per tick.
 */
public class BE_TileImporter extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 2;
    private static final int ITEMS_PER_TICK = 4;
    private static final int TICK_INTERVAL = 10; // every 10 ticks

    private BE_StorageNetwork network;
    private int tickCounter = 0;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld || network == null || !network.isActive()) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        // Find adjacent inventory
        IInventory adjacent = findAdjacentInventory();
        if (adjacent == null) return;

        // Pull items
        int pulled = 0;
        for (int slot = 0; slot < adjacent.getSizeInventory() && pulled < ITEMS_PER_TICK; slot++) {
            ItemStack stack = adjacent.getStackInSlot(slot);
            if (stack == null) continue;

            BE_ItemKey key = new BE_ItemKey(stack.itemID, stack.getItemDamage());
            int toInsert = Math.min(stack.stackSize, ITEMS_PER_TICK - pulled);

            // Simulate first
            int canInsert = network.getRootStorage().insert(key, toInsert, true);
            if (canInsert <= 0) continue;

            // Execute
            int inserted = network.getRootStorage().insert(key, canInsert, false);
            if (inserted > 0) {
                stack.stackSize -= inserted;
                if (stack.stackSize <= 0) {
                    adjacent.setInventorySlotContents(slot, null);
                }
                adjacent.onInventoryChanged();
                pulled += inserted;
            }
        }
    }

    private IInventory findAdjacentInventory() {
        int[][] offsets = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
        for (int[] off : offsets) {
            TileEntity te = worldObj.getBlockTileEntity(xCoord + off[0], yCoord + off[1], zCoord + off[2]);
            if (te instanceof IInventory && !(te instanceof BE_INetworkNode)) {
                return (IInventory) te;
            }
        }
        return null;
    }

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

    @Override
    public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); }
    @Override
    public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); }
}
