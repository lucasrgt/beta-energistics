package betaenergistics.network;

import betaenergistics.container.*;
import betaenergistics.mod_BetaEnergistics;
import betaenergistics.storage.BE_FluidKey;
import betaenergistics.storage.BE_ItemKey;
import betaenergistics.tile.*;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;

import java.util.List;
import java.util.Map;

/**
 * Handles multiplayer packet creation and dispatch for Beta Energistics.
 * Uses Packet230ModLoader with packetType to distinguish message types.
 *
 * Packet types (client → server):
 *   1 = Grid click interaction (itemId, damage, button, shift)
 *   2 = Request crafting (itemId, damage, quantity)
 *   3 = Crafting Terminal action (actionType: 0=fill, 1=clear, 2=craftToNet, 3=requestMissing)
 *   4 = Request Terminal action (actionType: 0=select, 1=confirm, 2=cancel, 3=incQty, 4=decQty)
 *   5 = Sort mode change (sortMode)
 *
 * Packet types (server → client):
 *   10 = Terminal item contents (flat array: [itemId, damage, count, ...])
 *   11 = Controller status update (energy, capacity, active)
 *   12 = Fluid terminal contents (flat array: [fluidType, amountMB, ...])
 *   13 = Crafting Terminal status text
 *   14 = Request Terminal craftable items
 *   15 = Request Terminal plan entries
 */
public class BE_PacketHandler {
    // Client → Server packet types
    public static final int C2S_GRID_CLICK = 1;
    public static final int C2S_REQUEST_CRAFT = 2;
    public static final int C2S_CRAFTING_ACTION = 3;
    public static final int C2S_REQUEST_TERMINAL_ACTION = 4;
    public static final int C2S_SORT_MODE = 5;

    // Server → Client packet types
    public static final int S2C_TERMINAL_CONTENTS = 10;
    public static final int S2C_CONTROLLER_STATUS = 11;
    public static final int S2C_FLUID_CONTENTS = 12;
    public static final int S2C_CRAFTING_STATUS = 13;
    public static final int S2C_CRAFTABLE_ITEMS = 14;
    public static final int S2C_PLAN_ENTRIES = 15;

    // ====== Client → Server packet builders ======

    /**
     * Build a grid click packet.
     * dataInt: [tileX, tileY, tileZ, itemId, damage, button, shift(0/1)]
     * itemId = -1 means clicked empty area.
     */
    public static Packet230ModLoader buildGridClick(TileEntity tile, BE_ItemKey key, int button, boolean shift) {
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = C2S_GRID_CLICK;
        pkt.dataInt = new int[] {
            tile.xCoord, tile.yCoord, tile.zCoord,
            key != null ? key.itemId : -1,
            key != null ? key.damageValue : 0,
            button,
            shift ? 1 : 0
        };
        return pkt;
    }

    /**
     * Build a request crafting packet.
     * dataInt: [tileX, tileY, tileZ, itemId, damage, quantity]
     */
    public static Packet230ModLoader buildRequestCraft(TileEntity tile, BE_ItemKey key, int quantity) {
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = C2S_REQUEST_CRAFT;
        pkt.dataInt = new int[] {
            tile.xCoord, tile.yCoord, tile.zCoord,
            key.itemId, key.damageValue, quantity
        };
        return pkt;
    }

    /**
     * Build a crafting terminal action packet.
     * dataInt: [tileX, tileY, tileZ, actionType]
     * actionType: 0=fill, 1=clear, 2=craftToNet, 3=requestMissing
     */
    public static Packet230ModLoader buildCraftingAction(TileEntity tile, int actionType) {
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = C2S_CRAFTING_ACTION;
        pkt.dataInt = new int[] {
            tile.xCoord, tile.yCoord, tile.zCoord,
            actionType
        };
        return pkt;
    }

    /**
     * Build a request terminal action packet.
     * dataInt: [tileX, tileY, tileZ, actionType, itemId, damage, quantity]
     * actionType: 0=select, 1=confirm, 2=cancel, 3=incQty, 4=decQty
     */
    public static Packet230ModLoader buildRequestTerminalAction(TileEntity tile, int actionType,
                                                                  BE_ItemKey key, int quantity) {
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = C2S_REQUEST_TERMINAL_ACTION;
        pkt.dataInt = new int[] {
            tile.xCoord, tile.yCoord, tile.zCoord,
            actionType,
            key != null ? key.itemId : -1,
            key != null ? key.damageValue : 0,
            quantity
        };
        return pkt;
    }

    /**
     * Build a sort mode change packet.
     * dataInt: [tileX, tileY, tileZ, sortMode]
     */
    public static Packet230ModLoader buildSortMode(TileEntity tile, int sortMode) {
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = C2S_SORT_MODE;
        pkt.dataInt = new int[] {
            tile.xCoord, tile.yCoord, tile.zCoord,
            sortMode
        };
        return pkt;
    }

    // ====== Server → Client packet builders ======

    /**
     * Build terminal contents packet from a map of items.
     * dataInt: [tileX, tileY, tileZ, numEntries, itemId1, damage1, count1, ...]
     */
    public static Packet230ModLoader buildTerminalContents(TileEntity tile, Map<BE_ItemKey, Integer> items) {
        int count = (items != null) ? items.size() : 0;
        int[] data = new int[4 + count * 3];
        data[0] = tile.xCoord;
        data[1] = tile.yCoord;
        data[2] = tile.zCoord;
        data[3] = count;
        int i = 4;
        if (items != null) {
            for (Map.Entry<BE_ItemKey, Integer> entry : items.entrySet()) {
                data[i++] = entry.getKey().itemId;
                data[i++] = entry.getKey().damageValue;
                data[i++] = entry.getValue();
            }
        }
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = S2C_TERMINAL_CONTENTS;
        pkt.dataInt = data;
        return pkt;
    }

    /**
     * Build controller status packet.
     * dataInt: [energy, capacity, active(0/1)]
     */
    public static Packet230ModLoader buildControllerStatus(int energy, int capacity, boolean active) {
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = S2C_CONTROLLER_STATUS;
        pkt.dataInt = new int[] { energy, capacity, active ? 1 : 0 };
        return pkt;
    }

    /**
     * Build fluid terminal contents packet.
     * dataInt: [tileX, tileY, tileZ, numEntries, fluidType1, amountMB1, ...]
     */
    public static Packet230ModLoader buildFluidContents(TileEntity tile, Map<BE_FluidKey, Integer> fluids) {
        int count = (fluids != null) ? fluids.size() : 0;
        int[] data = new int[4 + count * 2];
        data[0] = tile.xCoord;
        data[1] = tile.yCoord;
        data[2] = tile.zCoord;
        data[3] = count;
        int i = 4;
        if (fluids != null) {
            for (Map.Entry<BE_FluidKey, Integer> entry : fluids.entrySet()) {
                data[i++] = entry.getKey().fluidType;
                data[i++] = entry.getValue();
            }
        }
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = S2C_FLUID_CONTENTS;
        pkt.dataInt = data;
        return pkt;
    }

    /**
     * Build crafting terminal status text packet.
     * dataString: [statusText]
     * dataInt: [tileX, tileY, tileZ]
     */
    public static Packet230ModLoader buildCraftingStatus(TileEntity tile, String statusText) {
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = S2C_CRAFTING_STATUS;
        pkt.dataInt = new int[] { tile.xCoord, tile.yCoord, tile.zCoord };
        pkt.dataString = new String[] { statusText != null ? statusText : "" };
        return pkt;
    }

    /**
     * Build craftable items list for Request Terminal.
     * dataInt: [tileX, tileY, tileZ, numEntries, itemId1, damage1, crafterCount1, ...]
     */
    public static Packet230ModLoader buildCraftableItems(TileEntity tile,
                                                          List<BE_ContainerRequestTerminal.CraftableEntry> items) {
        int count = (items != null) ? items.size() : 0;
        int[] data = new int[4 + count * 3];
        data[0] = tile.xCoord;
        data[1] = tile.yCoord;
        data[2] = tile.zCoord;
        data[3] = count;
        int i = 4;
        if (items != null) {
            for (BE_ContainerRequestTerminal.CraftableEntry entry : items) {
                data[i++] = entry.key.itemId;
                data[i++] = entry.key.damageValue;
                data[i++] = entry.crafterCount;
            }
        }
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = S2C_CRAFTABLE_ITEMS;
        pkt.dataInt = data;
        return pkt;
    }

    /**
     * Build plan entries for Request Terminal preview.
     * dataInt: [tileX, tileY, tileZ, numEntries, itemId1, damage1, count1, type1, ...]
     */
    public static Packet230ModLoader buildPlanEntries(TileEntity tile,
                                                       List<BE_ContainerRequestTerminal.PlanEntry> entries) {
        int count = (entries != null) ? entries.size() : 0;
        int[] data = new int[4 + count * 4];
        data[0] = tile.xCoord;
        data[1] = tile.yCoord;
        data[2] = tile.zCoord;
        data[3] = count;
        int i = 4;
        if (entries != null) {
            for (BE_ContainerRequestTerminal.PlanEntry entry : entries) {
                data[i++] = entry.key.itemId;
                data[i++] = entry.key.damageValue;
                data[i++] = entry.count;
                data[i++] = entry.type;
            }
        }
        Packet230ModLoader pkt = new Packet230ModLoader();
        pkt.packetType = S2C_PLAN_ENTRIES;
        pkt.dataInt = data;
        return pkt;
    }

    // ====== Client-side packet handling (received from server) ======

    /**
     * Handle a packet received on the client side.
     * Dispatches to the appropriate container if the player has it open.
     */
    public static void handleClientPacket(Packet230ModLoader pkt) {
        EntityPlayer player = ModLoader.getMinecraftInstance().thePlayer;
        if (player == null) return;

        Container container = player.craftingInventory;

        switch (pkt.packetType) {
            case S2C_TERMINAL_CONTENTS:
                handleTerminalContents(pkt, container);
                break;
            case S2C_CONTROLLER_STATUS:
                // Store globally for any GUI that wants to display it
                handleControllerStatus(pkt);
                break;
            case S2C_FLUID_CONTENTS:
                handleFluidContents(pkt, container);
                break;
            case S2C_CRAFTING_STATUS:
                handleCraftingStatus(pkt, container);
                break;
            case S2C_CRAFTABLE_ITEMS:
                handleCraftableItems(pkt, container);
                break;
            case S2C_PLAN_ENTRIES:
                handlePlanEntries(pkt, container);
                break;
        }
    }

    private static void handleTerminalContents(Packet230ModLoader pkt, Container container) {
        int[] data = pkt.dataInt;
        if (data == null || data.length < 4) return;
        int numEntries = data[3];

        if (container instanceof BE_ContainerGrid) {
            BE_ContainerGrid grid = (BE_ContainerGrid) container;
            grid.receiveNetworkItems(data, 4, numEntries);
        } else if (container instanceof BE_ContainerCraftingTerminal) {
            BE_ContainerCraftingTerminal ct = (BE_ContainerCraftingTerminal) container;
            ct.receiveNetworkItems(data, 4, numEntries);
        }
    }

    private static void handleFluidContents(Packet230ModLoader pkt, Container container) {
        int[] data = pkt.dataInt;
        if (data == null || data.length < 4) return;
        int numEntries = data[3];

        if (container instanceof BE_ContainerFluidTerminal) {
            BE_ContainerFluidTerminal ft = (BE_ContainerFluidTerminal) container;
            ft.receiveNetworkFluids(data, 4, numEntries);
        }
    }

    private static void handleCraftingStatus(Packet230ModLoader pkt, Container container) {
        if (container instanceof BE_ContainerCraftingTerminal) {
            String status = (pkt.dataString != null && pkt.dataString.length > 0) ? pkt.dataString[0] : "";
            ((BE_ContainerCraftingTerminal) container).receiveStatusText(status);
        }
    }

    private static void handleCraftableItems(Packet230ModLoader pkt, Container container) {
        int[] data = pkt.dataInt;
        if (data == null || data.length < 4) return;
        int numEntries = data[3];

        if (container instanceof BE_ContainerRequestTerminal) {
            ((BE_ContainerRequestTerminal) container).receiveCraftableItems(data, 4, numEntries);
        }
    }

    private static void handlePlanEntries(Packet230ModLoader pkt, Container container) {
        int[] data = pkt.dataInt;
        if (data == null || data.length < 4) return;
        int numEntries = data[3];

        if (container instanceof BE_ContainerRequestTerminal) {
            ((BE_ContainerRequestTerminal) container).receivePlanEntries(data, 4, numEntries);
        }
    }

    // Controller status is stored globally for any GUI to access
    private static int lastEnergy = 0;
    private static int lastCapacity = 0;
    private static boolean lastActive = false;

    private static void handleControllerStatus(Packet230ModLoader pkt) {
        int[] data = pkt.dataInt;
        if (data == null || data.length < 3) return;
        lastEnergy = data[0];
        lastCapacity = data[1];
        lastActive = data[2] == 1;
    }

    public static int getLastEnergy() { return lastEnergy; }
    public static int getLastCapacity() { return lastCapacity; }
    public static boolean getLastActive() { return lastActive; }

    /**
     * Send a packet from the client to the server.
     * Only works in multiplayer (multiplayerWorld == true).
     */
    public static void sendToServer(Packet230ModLoader pkt) {
        BaseModMp mod = ModLoaderMp.GetModInstance(mod_BetaEnergistics.class);
        if (mod != null) {
            ModLoaderMp.SendPacket(mod, pkt);
        }
    }
}
