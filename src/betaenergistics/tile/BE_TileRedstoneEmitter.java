package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

/**
 * Redstone Emitter — emits redstone signal based on item quantity in the network.
 * Has a ghost filter slot (item type), threshold, and 6 comparison modes.
 */
public class BE_TileRedstoneEmitter extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 1;
    private static final int UPDATE_INTERVAL = 20; // ticks

    // Comparison modes
    public static final int MODE_EQUAL = 0;         // ==
    public static final int MODE_NOT_EQUAL = 1;     // !=
    public static final int MODE_GREATER = 2;       // >
    public static final int MODE_LESS = 3;          // <
    public static final int MODE_GREATER_EQ = 4;    // >=
    public static final int MODE_LESS_EQ = 5;       // <=
    public static final int MODE_COUNT = 6;

    public static final String[] MODE_LABELS = {"==", "!=", ">", "<", ">=", "<="};

    private BE_StorageNetwork network;

    // Filter: the item type to monitor (null = no filter)
    private BE_ItemKey filterItem = null;
    private int threshold = 0;
    private int comparisonMode = MODE_GREATER_EQ;
    private int redstoneOutput = 0;
    private int tickCounter = 0;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld) return;

        tickCounter++;
        if (tickCounter < UPDATE_INTERVAL) return;
        tickCounter = 0;

        int oldOutput = redstoneOutput;
        redstoneOutput = calculateOutput();

        if (oldOutput != redstoneOutput) {
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
        }
    }

    private int calculateOutput() {
        if (network == null || !network.isActive()) return 0;
        if (filterItem == null) return 0;

        int count = network.getRootStorage().getCount(filterItem);
        boolean result;

        switch (comparisonMode) {
            case MODE_EQUAL:      result = count == threshold; break;
            case MODE_NOT_EQUAL:  result = count != threshold; break;
            case MODE_GREATER:    result = count > threshold; break;
            case MODE_LESS:       result = count < threshold; break;
            case MODE_GREATER_EQ: result = count >= threshold; break;
            case MODE_LESS_EQ:    result = count <= threshold; break;
            default:              result = false; break;
        }

        return result ? 15 : 0;
    }

    // --- Filter and config ---

    public void setFilterItem(BE_ItemKey key) {
        this.filterItem = key;
    }

    public BE_ItemKey getFilterItem() {
        return filterItem;
    }

    public void setThreshold(int threshold) {
        this.threshold = Math.max(0, threshold);
    }

    public int getThreshold() {
        return threshold;
    }

    public void cycleMode() {
        comparisonMode = (comparisonMode + 1) % MODE_COUNT;
    }

    public int getComparisonMode() {
        return comparisonMode;
    }

    public String getModeLabel() {
        return MODE_LABELS[comparisonMode];
    }

    public int getRedstoneOutput() {
        return redstoneOutput;
    }

    // --- BE_INetworkNode ---

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

    // --- NBT ---

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        comparisonMode = tag.getInteger("CompMode");
        threshold = tag.getInteger("Threshold");
        redstoneOutput = tag.getInteger("RSOut");

        if (tag.hasKey("FilterId")) {
            int filterId = tag.getInteger("FilterId");
            int filterDmg = tag.getInteger("FilterDmg");
            filterItem = new BE_ItemKey(filterId, filterDmg);
        } else {
            filterItem = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("CompMode", comparisonMode);
        tag.setInteger("Threshold", threshold);
        tag.setInteger("RSOut", redstoneOutput);

        if (filterItem != null) {
            tag.setInteger("FilterId", filterItem.itemId);
            tag.setInteger("FilterDmg", filterItem.damageValue);
        }
    }
}
