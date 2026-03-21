package betaenergistics.container;

import betaenergistics.storage.BE_FluidKey;
import betaenergistics.tile.BE_TileFluidTerminal;

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
public class BE_ContainerFluidTerminal extends Container {
    private BE_TileFluidTerminal terminal;
    private List<BE_FluidEntry> cachedFluids = new ArrayList<BE_FluidEntry>();

    public BE_ContainerFluidTerminal(InventoryPlayer playerInv, BE_TileFluidTerminal terminal) {
        this.terminal = terminal;

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }

        refreshFluids();
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

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return terminal.worldObj.getBlockTileEntity(terminal.xCoord, terminal.yCoord, terminal.zCoord) == terminal
            && player.getDistanceSq(terminal.xCoord + 0.5, terminal.yCoord + 0.5, terminal.zCoord + 0.5) <= 64.0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return null; // No shift-click behavior for fluids
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
