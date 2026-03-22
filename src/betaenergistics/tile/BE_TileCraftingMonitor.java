package betaenergistics.tile;

import betaenergistics.network.BE_INetworkNode;
import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_ItemKey;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Crafting Monitor — shows all active and pending crafts in the network.
 * Read-only display + cancel functionality.
 */
public class BE_TileCraftingMonitor extends TileEntity implements BE_INetworkNode {
    private static final int ENERGY_USAGE = 1;
    private BE_StorageNetwork network;

    /** Get a snapshot of all craft jobs across all autocrafters in the network. */
    public List<CraftJob> getCraftJobs() {
        List<CraftJob> jobs = new ArrayList<CraftJob>();
        if (network == null || !network.isActive()) return jobs;

        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (!(te instanceof BE_TileAutocrafter)) continue;
            BE_TileAutocrafter crafter = (BE_TileAutocrafter) te;

            for (int i = 0; i < BE_TileAutocrafter.PATTERN_SLOTS; i++) {
                int pending = crafter.getPendingCrafts(i);
                boolean active = crafter.isCrafting() && crafter.getActiveCraftIndex() == i;
                if (pending > 0 || active) {
                    net.minecraft.src.ItemStack output = crafter.getPatternOutput(i);
                    if (output != null) {
                        CraftJob job = new CraftJob();
                        job.outputKey = new BE_ItemKey(output.itemID, output.getItemDamage());
                        job.pending = pending;
                        job.active = active;
                        job.progress = active ? crafter.getCraftProgressScaled(100) : 0;
                        job.crafter = crafter;
                        job.slotIndex = i;
                        jobs.add(job);
                    }
                }
            }
        }
        return jobs;
    }

    /** Cancel all pending crafts in the network. */
    public void cancelAllCrafts() {
        if (network == null) return;
        for (BE_INetworkNode node : network.getNodes()) {
            TileEntity te = node.getTileEntity();
            if (te instanceof BE_TileAutocrafter) {
                ((BE_TileAutocrafter) te).cancelAllCrafts();
            }
        }
    }

    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override public int getEnergyUsage() { return ENERGY_USAGE; }
    @Override public void onNetworkJoin(BE_StorageNetwork network) { this.network = network; }
    @Override public void onNetworkLeave() { this.network = null; }
    @Override public BE_StorageNetwork getNetwork() { return network; }
    @Override public TileEntity getTileEntity() { return this; }
    @Override public boolean canConnectOnSide(int side) { return true; }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); }
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); }

    public static class CraftJob {
        public BE_ItemKey outputKey;
        public int pending;
        public boolean active;
        public int progress;
        public BE_TileAutocrafter crafter;
        public int slotIndex;
    }
}
