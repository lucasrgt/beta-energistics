package betaenergistics.network;

import betaenergistics.storage.BE_CompositeFluidStorage;
import betaenergistics.storage.BE_CompositeGasStorage;
import betaenergistics.storage.BE_CompositeStorage;
import betaenergistics.storage.BE_IFluidStorage;
import betaenergistics.storage.BE_IGasStorage;
import betaenergistics.storage.BE_IStorage;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.BE_TileAutocrafter;
import betaenergistics.tile.BE_TileCoprocessor;

import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Represents a storage network — a connected graph of BE_INetworkNode tiles.
 * Managed by the Controller tile entity.
 *
 * Responsibilities:
 * - Track all connected nodes
 * - Aggregate storage from all IStorageProvider nodes
 * - Manage energy pool (EU)
 * - Tick importers/exporters (future)
 */
public class BE_StorageNetwork {
    private final List<BE_INetworkNode> nodes = new ArrayList<BE_INetworkNode>();
    private final BE_CompositeStorage rootStorage = new BE_CompositeStorage();
    private final BE_CompositeFluidStorage fluidStorage = new BE_CompositeFluidStorage();
    private final BE_CompositeGasStorage gasStorage = new BE_CompositeGasStorage();
    private int energyStored = 0;
    private int energyCapacity = 1600; // base capacity from controller
    private boolean active = false;

    // Side offsets: down, up, north, south, west, east
    private static final int[][] SIDE_OFFSETS = {
        {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}
    };
    private static final int[] OPPOSITE_SIDES = {1, 0, 3, 2, 5, 4};

    public void addNode(BE_INetworkNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            node.onNetworkJoin(this);
            rebuildStorage();
        }
    }

    public void removeNode(BE_INetworkNode node) {
        nodes.remove(node);
        node.onNetworkLeave();
        rebuildStorage();
    }

    /**
     * Rebuild the composite storage from all IStorageProvider nodes.
     * Called when nodes join/leave or disks change.
     */
    public void rebuildStorage() {
        rootStorage.clear();
        for (BE_INetworkNode node : nodes) {
            if (node instanceof BE_IStorageProvider) {
                for (BE_IStorage storage : ((BE_IStorageProvider) node).getStorages()) {
                    rootStorage.addStorage(storage);
                }
            }
        }
        rootStorage.markDirty();
        rebuildFluidStorage();
    }

    /**
     * Rebuild the composite fluid storage from all IFluidStorageProvider nodes.
     * Called when nodes join/leave or fluid disks change.
     */
    public void rebuildFluidStorage() {
        fluidStorage.clear();
        for (BE_INetworkNode node : nodes) {
            if (node instanceof BE_IFluidStorageProvider) {
                for (BE_IFluidStorage storage : ((BE_IFluidStorageProvider) node).getFluidStorages()) {
                    fluidStorage.addStorage(storage);
                }
            }
        }
        fluidStorage.markDirty();
    }

    /**
     * Discover all connected network nodes via BFS from the controller position.
     * Builds the complete network graph.
     */
    public void discover(World world, int startX, int startY, int startZ) {
        // Disconnect all existing nodes
        for (BE_INetworkNode node : new ArrayList<BE_INetworkNode>(nodes)) {
            node.onNetworkLeave();
        }
        nodes.clear();

        Set<Long> visited = new HashSet<Long>();
        Queue<int[]> queue = new LinkedList<int[]>();
        queue.add(new int[]{startX, startY, startZ});
        visited.add(posKey(startX, startY, startZ));

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0], y = pos[1], z = pos[2];

            TileEntity te = world.getBlockTileEntity(x, y, z);
            if (!(te instanceof BE_INetworkNode)) continue;

            BE_INetworkNode node = (BE_INetworkNode) te;
            nodes.add(node);
            node.onNetworkJoin(this);

            // Check all 6 sides
            for (int side = 0; side < 6; side++) {
                if (!node.canConnectOnSide(side)) continue;

                int nx = x + SIDE_OFFSETS[side][0];
                int ny = y + SIDE_OFFSETS[side][1];
                int nz = z + SIDE_OFFSETS[side][2];
                long key = posKey(nx, ny, nz);

                if (visited.contains(key)) continue;
                visited.add(key);

                TileEntity neighbor = world.getBlockTileEntity(nx, ny, nz);
                if (neighbor instanceof BE_INetworkNode) {
                    BE_INetworkNode neighborNode = (BE_INetworkNode) neighbor;
                    if (neighborNode.canConnectOnSide(OPPOSITE_SIDES[side])) {
                        queue.add(new int[]{nx, ny, nz});
                    }
                }
            }
        }

        rebuildStorage();
        active = true;
    }

    /** Tick the network — consume energy, run automation. */
    public void tick() {
        if (!active) return;

        int totalUsage = 0;
        for (BE_INetworkNode node : nodes) {
            totalUsage += node.getEnergyUsage();
        }

        if (energyStored >= totalUsage) {
            energyStored -= totalUsage;
        } else {
            // Not enough energy — network goes offline
            active = false;
        }
    }

    public void addEnergy(int amount) {
        energyStored = Math.min(energyStored + amount, energyCapacity);
    }

    public void setEnergyCapacity(int capacity) {
        this.energyCapacity = capacity;
        if (energyStored > energyCapacity) energyStored = energyCapacity;
    }

    // Accessors
    public BE_CompositeStorage getRootStorage() { return rootStorage; }
    public BE_CompositeFluidStorage getFluidStorage() { return fluidStorage; }
    public BE_CompositeGasStorage getGasStorage() { return gasStorage; }
    public List<BE_INetworkNode> getNodes() { return nodes; }
    public int getEnergyStored() { return energyStored; }
    public int getEnergyCapacity() { return energyCapacity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getNodeCount() { return nodes.size(); }

    /** Count connected coprocessors in the network. */
    public int getCoprocessorCount() {
        int count = 0;
        for (BE_INetworkNode node : nodes) {
            if (node instanceof BE_TileCoprocessor) count++;
        }
        return count;
    }

    /** Max concurrent crafting tasks = 1 (base) + number of coprocessors. */
    public int getMaxConcurrentCrafts() {
        return 1 + getCoprocessorCount();
    }

    /** Count how many autocrafters are currently actively crafting. */
    public int getActiveCraftCount() {
        int count = 0;
        for (BE_INetworkNode node : nodes) {
            if (node instanceof BE_TileAutocrafter && ((BE_TileAutocrafter) node).isCrafting()) {
                count++;
            }
        }
        return count;
    }

    private static long posKey(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) | (((long) y & 0xFF) << 26) | (((long) z & 0x3FFFFFF) << 34);
    }
}
