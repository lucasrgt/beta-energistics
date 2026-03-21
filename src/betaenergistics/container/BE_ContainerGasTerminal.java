package betaenergistics.container;

import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_CompositeGasStorage;
import betaenergistics.storage.BE_GasKey;
import betaenergistics.tile.BE_TileGasTerminal;
import betaenergistics.tile.BE_TileTerminalBase;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BE_ContainerGasTerminal extends BE_ContainerTerminalBase {
    private BE_TileGasTerminal tile;
    private List<BE_GasEntry> cachedGases = new ArrayList<BE_GasEntry>();

    public BE_ContainerGasTerminal(InventoryPlayer playerInv, BE_TileGasTerminal tile) {
        this.tile = tile;
        addPlayerInventory(playerInv);
    }

    @Override
    protected BE_TileTerminalBase getTerminalTile() {
        return tile;
    }

    @Override
    public void updateCraftingResults() {
        super.updateCraftingResults();
        refreshGases();
    }

    public void refreshGases() {
        cachedGases.clear();
        BE_StorageNetwork network = tile.getNetwork();
        if (network == null || !network.isActive()) return;

        BE_CompositeGasStorage gasStorage = network.getGasStorage();
        if (gasStorage == null) return;

        Map<BE_GasKey, Integer> allGases = gasStorage.getAllGases();
        for (Map.Entry<BE_GasKey, Integer> e : allGases.entrySet()) {
            cachedGases.add(new BE_GasEntry(e.getKey(), e.getValue()));
        }

        Collections.sort(cachedGases, new Comparator<BE_GasEntry>() {
            public int compare(BE_GasEntry a, BE_GasEntry b) {
                return b.amountMB - a.amountMB;
            }
        });
    }

    public List<BE_GasEntry> getGases() {
        return cachedGases;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return null;
    }

    public static class BE_GasEntry {
        public final BE_GasKey key;
        public final int amountMB;

        public BE_GasEntry(BE_GasKey key, int amountMB) {
            this.key = key;
            this.amountMB = amountMB;
        }
    }
}
