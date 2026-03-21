package betaenergistics.container;

import betaenergistics.network.BE_StorageNetwork;
import betaenergistics.storage.BE_CompositeFluidStorage;
import betaenergistics.storage.BE_FluidKey;
import betaenergistics.tile.BE_TileFluidTerminal;
import betaenergistics.tile.BE_TileTerminalBase;

import aero.machineapi.Aero_FluidType;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Container for the Fluid Terminal.
 * No machine slots — just the player inventory + virtual network fluid display.
 */
public class BE_ContainerFluidTerminal extends BE_ContainerTerminalBase {
    private BE_TileFluidTerminal terminal;
    private List<BE_FluidEntry> cachedFluids = new ArrayList<BE_FluidEntry>();

    public BE_ContainerFluidTerminal(InventoryPlayer playerInv, BE_TileFluidTerminal terminal) {
        this.terminal = terminal;
        addPlayerInventory(playerInv);
        refreshFluids();
    }

    @Override
    protected BE_TileTerminalBase getTerminalTile() {
        return terminal;
    }

    public void refreshFluids() {
        cachedFluids.clear();
        Map<BE_FluidKey, Integer> networkFluids = terminal.getNetworkFluids();
        if (networkFluids != null) {
            for (Map.Entry<BE_FluidKey, Integer> entry : networkFluids.entrySet()) {
                cachedFluids.add(new BE_FluidEntry(entry.getKey(), entry.getValue()));
            }
            Collections.sort(cachedFluids, new Comparator<BE_FluidEntry>() {
                public int compare(BE_FluidEntry a, BE_FluidEntry b) {
                    return b.amountMB - a.amountMB; // descending by amount
                }
            });
        }
    }

    @Override
    public void updateCraftingResults() {
        super.updateCraftingResults();
        refreshFluids();
    }

    public List<BE_FluidEntry> getFluids() {
        return cachedFluids;
    }

    public BE_TileFluidTerminal getTerminal() {
        return terminal;
    }

    /**
     * Handle bucket click on the fluid grid.
     */
    public void handleBucketClick(ItemStack held, EntityPlayer player) {
        BE_StorageNetwork network = terminal.getNetwork();
        if (network == null || !network.isActive()) return;
        BE_CompositeFluidStorage fluidStorage = network.getFluidStorage();
        if (fluidStorage == null) return;

        int BUCKET_MB = 1000;

        if (held.itemID == Item.bucketWater.shiftedIndex) {
            int inserted = fluidStorage.insertFluid(new BE_FluidKey(Aero_FluidType.WATER), BUCKET_MB, false);
            if (inserted >= BUCKET_MB) {
                held.itemID = Item.bucketEmpty.shiftedIndex;
                held.stackSize = 1;
            }
        } else if (held.itemID == Item.bucketLava.shiftedIndex) {
            int inserted = fluidStorage.insertFluid(new BE_FluidKey(Aero_FluidType.LAVA), BUCKET_MB, false);
            if (inserted >= BUCKET_MB) {
                held.itemID = Item.bucketEmpty.shiftedIndex;
                held.stackSize = 1;
            }
        } else if (held.itemID == Item.bucketEmpty.shiftedIndex) {
            for (java.util.Map.Entry<BE_FluidKey, Integer> e : fluidStorage.getAllFluids().entrySet()) {
                if (e.getValue() >= BUCKET_MB) {
                    int extracted = fluidStorage.extractFluid(e.getKey(), BUCKET_MB, false);
                    if (extracted >= BUCKET_MB) {
                        if (e.getKey().fluidType == Aero_FluidType.WATER) {
                            held.itemID = Item.bucketWater.shiftedIndex;
                        } else if (e.getKey().fluidType == Aero_FluidType.LAVA) {
                            held.itemID = Item.bucketLava.shiftedIndex;
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return null; // No shift-click behavior for fluids
    }

    // ====== Multiplayer receive methods ======

    /**
     * Receive network fluid data from a server packet (multiplayer client-side).
     */
    public void receiveNetworkFluids(int[] data, int offset, int numEntries) {
        cachedFluids.clear();
        for (int i = 0; i < numEntries; i++) {
            int idx = offset + i * 2;
            if (idx + 1 >= data.length) break;
            BE_FluidKey key = new BE_FluidKey(data[idx]);
            int amount = data[idx + 1];
            cachedFluids.add(new BE_FluidEntry(key, amount));
        }
        Collections.sort(cachedFluids, new Comparator<BE_FluidEntry>() {
            public int compare(BE_FluidEntry a, BE_FluidEntry b) {
                return b.amountMB - a.amountMB;
            }
        });
    }

    /** Entry in the fluid display: a fluid type + amount in mB. */
    public static class BE_FluidEntry {
        public final BE_FluidKey key;
        public int amountMB;

        public BE_FluidEntry(BE_FluidKey key, int amountMB) {
            this.key = key;
            this.amountMB = amountMB;
        }
    }
}
