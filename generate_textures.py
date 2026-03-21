#!/usr/bin/env python3
"""Generate professional 16x16 pixel art textures for Beta Energistics mod.
Theme: Dark industrial tech (inspired by Applied Energistics 2).
Palette: Dark gray casings, cyan/blue accents, colored indicators per function.
"""
import struct, zlib, os

# --- PNG writer ---
def create_png(width, height, pixels):
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    raw = b''
    for row in pixels:
        raw += b'\x00'
        for r, g, b, a in row:
            raw += struct.pack('BBBB', r, g, b, a)
    idat = chunk(b'IDAT', zlib.compress(raw))
    iend = chunk(b'IEND', b'')
    return sig + ihdr + idat + iend

def save_png(path, pixels):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(create_png(16, 16, pixels))
    print(f"  -> {os.path.basename(path)}")

def blank(color=(0,0,0,0)):
    return [[color]*16 for _ in range(16)]

def fill(grid, x, y, w, h, color):
    for dy in range(h):
        for dx in range(w):
            if 0 <= y+dy < 16 and 0 <= x+dx < 16:
                grid[y+dy][x+dx] = color

def setpx(grid, x, y, color):
    if 0 <= y < 16 and 0 <= x < 16:
        grid[y][x] = color

def draw_border(grid, color):
    for i in range(16):
        grid[0][i] = color
        grid[15][i] = color
        grid[i][0] = color
        grid[i][15] = color

def draw_inner_border(grid, color):
    for i in range(1, 15):
        grid[1][i] = color
        grid[14][i] = color
        grid[i][1] = color
        grid[i][14] = color

# --- Color Palette ---
# Machine casing
DARK_CASE = (45, 45, 50, 255)
MID_CASE = (60, 60, 68, 255)
LIGHT_CASE = (75, 75, 82, 255)
EDGE_DARK = (30, 30, 35, 255)
EDGE_LIGHT = (85, 85, 92, 255)

# Accents
CYAN = (0, 200, 220, 255)
CYAN_DIM = (0, 140, 160, 255)
CYAN_BRIGHT = (100, 240, 255, 255)
BLUE = (40, 80, 200, 255)
BLUE_DIM = (30, 55, 140, 255)
BLUE_BRIGHT = (80, 130, 255, 255)
GREEN = (40, 200, 80, 255)
GREEN_DIM = (30, 140, 55, 255)
ORANGE = (220, 140, 30, 255)
ORANGE_DIM = (160, 100, 20, 255)
RED = (200, 40, 40, 255)
RED_DIM = (140, 30, 30, 255)
YELLOW = (220, 200, 40, 255)
YELLOW_DIM = (160, 140, 30, 255)
PURPLE = (140, 60, 200, 255)
PURPLE_DIM = (100, 40, 140, 255)
WHITE = (220, 220, 230, 255)
GOLD = (200, 170, 50, 255)
GOLD_DIM = (140, 120, 35, 255)

# Screen
SCREEN_BG = (20, 35, 50, 255)
SCREEN_LINE = (40, 70, 100, 255)
SCREEN_BRIGHT = (60, 110, 150, 255)

TRANS = (0, 0, 0, 0)

# --- Base machine face ---
def machine_base():
    """Standard machine casing - dark gray with subtle border."""
    g = blank(MID_CASE)
    draw_border(g, EDGE_DARK)
    draw_inner_border(g, DARK_CASE)
    # Corner highlights
    setpx(g, 0, 0, (20, 20, 25, 255))
    setpx(g, 15, 0, (20, 20, 25, 255))
    setpx(g, 0, 15, (20, 20, 25, 255))
    setpx(g, 15, 15, (20, 20, 25, 255))
    # Top-left subtle highlight
    for i in range(2, 14):
        setpx(g, i, 2, LIGHT_CASE)
        setpx(g, 2, i, LIGHT_CASE)
    return g

# ==================== BLOCK TEXTURES ====================

BLOCKS_DIR = "src/betaenergistics/assets/blocks"

def gen_controller():
    """ME Controller - central brain with pulsing cyan circuit pattern."""
    g = machine_base()
    # Cyan cross/circuit pattern in center
    fill(g, 6, 3, 4, 10, CYAN_DIM)
    fill(g, 3, 6, 10, 4, CYAN_DIM)
    fill(g, 7, 4, 2, 8, CYAN)
    fill(g, 4, 7, 8, 2, CYAN)
    # Center bright core
    fill(g, 7, 7, 2, 2, CYAN_BRIGHT)
    # Corner circuit dots
    for (x, y) in [(4, 4), (11, 4), (4, 11), (11, 11)]:
        setpx(g, x, y, CYAN)
    # Edge connectors
    for (x, y) in [(7, 2), (8, 2), (7, 13), (8, 13), (2, 7), (2, 8), (13, 7), (13, 8)]:
        setpx(g, x, y, CYAN_DIM)
    save_png(f"{BLOCKS_DIR}/be_controller.png", g)

def gen_cable():
    """ME Cable - small center connector with subtle glow."""
    g = blank(TRANS)
    # Center cable core (6x6)
    fill(g, 5, 5, 6, 6, DARK_CASE)
    fill(g, 6, 6, 4, 4, MID_CASE)
    fill(g, 7, 7, 2, 2, CYAN_DIM)
    # Edge highlight
    for i in range(5, 11):
        setpx(g, i, 5, EDGE_DARK)
        setpx(g, 5, i, EDGE_DARK)
    save_png(f"{BLOCKS_DIR}/be_cable.png", g)

def gen_disk_drive():
    """ME Disk Drive - 3 horizontal drive bays with disk slots."""
    g = machine_base()
    # Three drive bay slots
    for row_y in [4, 7, 10]:
        fill(g, 3, row_y, 10, 2, (35, 35, 40, 255))
        # Drive slot lines
        fill(g, 4, row_y, 3, 1, (55, 55, 65, 255))
        fill(g, 9, row_y, 3, 1, (55, 55, 65, 255))
        # Activity LED
        setpx(g, 12, row_y, CYAN_DIM)
        setpx(g, 12, row_y + 1, CYAN)
    # Label area
    fill(g, 5, 2, 6, 1, EDGE_LIGHT)
    save_png(f"{BLOCKS_DIR}/be_disk_drive.png", g)

def gen_grid_terminal():
    """ME Grid Terminal - screen with item grid display."""
    g = machine_base()
    # Screen area
    fill(g, 3, 3, 10, 10, SCREEN_BG)
    # Grid lines on screen
    for i in range(4, 13, 2):
        for j in range(4, 13, 2):
            setpx(g, i, j, SCREEN_LINE)
    # Screen border glow
    for i in range(3, 13):
        setpx(g, i, 3, SCREEN_BRIGHT)
        setpx(g, 3, i, (30, 50, 75, 255))
    # Bottom indicator
    setpx(g, 7, 13, CYAN)
    setpx(g, 8, 13, CYAN)
    save_png(f"{BLOCKS_DIR}/be_grid_terminal.png", g)

def gen_crafting_terminal():
    """ME Crafting Terminal - screen + crafting grid accent."""
    g = machine_base()
    # Screen area (upper)
    fill(g, 3, 3, 10, 6, SCREEN_BG)
    for i in range(4, 13, 2):
        for j in range(4, 9, 2):
            setpx(g, i, j, SCREEN_LINE)
    setpx(g, 7, 3, CYAN)
    setpx(g, 8, 3, CYAN)
    # Crafting grid (lower) - 3x3 brown/tan squares
    craft_color = (100, 75, 45, 255)
    craft_dark = (70, 52, 30, 255)
    for cx in range(3):
        for cy in range(3):
            fill(g, 4 + cx * 3, 10 + cy * 1, 2, 1, craft_color)
    # Arrow indicator
    setpx(g, 12, 11, ORANGE)
    setpx(g, 13, 11, ORANGE)
    save_png(f"{BLOCKS_DIR}/be_crafting_terminal.png", g)

def gen_importer():
    """ME Import Bus - green arrow pointing into machine."""
    g = machine_base()
    # Center face plate
    fill(g, 4, 4, 8, 8, DARK_CASE)
    # Green arrow pointing DOWN (into machine)
    setpx(g, 7, 4, GREEN)
    setpx(g, 8, 4, GREEN)
    fill(g, 7, 5, 2, 4, GREEN)
    # Arrow head
    setpx(g, 5, 8, GREEN_DIM)
    setpx(g, 6, 9, GREEN)
    setpx(g, 7, 10, GREEN)
    setpx(g, 8, 10, GREEN)
    setpx(g, 9, 9, GREEN)
    setpx(g, 10, 8, GREEN_DIM)
    # Side channels
    fill(g, 3, 6, 1, 4, GREEN_DIM)
    fill(g, 12, 6, 1, 4, GREEN_DIM)
    save_png(f"{BLOCKS_DIR}/be_importer.png", g)

def gen_exporter():
    """ME Export Bus - orange arrow pointing out of machine."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    # Orange arrow pointing UP (out of machine)
    setpx(g, 7, 11, ORANGE)
    setpx(g, 8, 11, ORANGE)
    fill(g, 7, 7, 2, 4, ORANGE)
    # Arrow head
    setpx(g, 5, 7, ORANGE_DIM)
    setpx(g, 6, 6, ORANGE)
    setpx(g, 7, 5, ORANGE)
    setpx(g, 8, 5, ORANGE)
    setpx(g, 9, 6, ORANGE)
    setpx(g, 10, 7, ORANGE_DIM)
    # Side channels
    fill(g, 3, 6, 1, 4, ORANGE_DIM)
    fill(g, 12, 6, 1, 4, ORANGE_DIM)
    save_png(f"{BLOCKS_DIR}/be_exporter.png", g)

def gen_autocrafter():
    """ME Autocrafter - gear/cog pattern with processing indicator."""
    g = machine_base()
    # Gear/cog outline
    gear_color = (80, 90, 110, 255)
    gear_inner = CYAN_DIM
    # Outer gear teeth
    for pos in [(7, 3), (8, 3), (7, 12), (8, 12), (3, 7), (3, 8), (12, 7), (12, 8)]:
        setpx(g, pos[0], pos[1], gear_color)
    # Gear ring
    for pos in [(5, 4), (6, 4), (9, 4), (10, 4),
                (4, 5), (4, 6), (4, 9), (4, 10),
                (11, 5), (11, 6), (11, 9), (11, 10),
                (5, 11), (6, 11), (9, 11), (10, 11)]:
        setpx(g, pos[0], pos[1], gear_color)
    # Inner ring
    fill(g, 6, 6, 4, 4, (50, 55, 65, 255))
    fill(g, 7, 7, 2, 2, gear_inner)
    # Diagonal teeth
    for pos in [(5, 5), (10, 5), (5, 10), (10, 10)]:
        setpx(g, pos[0], pos[1], gear_color)
    save_png(f"{BLOCKS_DIR}/be_autocrafter.png", g)

def gen_storage_bus():
    """ME Storage Bus - purple connector plate with grid pattern."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    # Purple accent frame
    for i in range(4, 12):
        setpx(g, i, 4, PURPLE_DIM)
        setpx(g, i, 11, PURPLE_DIM)
        setpx(g, 4, i, PURPLE_DIM)
        setpx(g, 11, i, PURPLE_DIM)
    # Center grid (3x3 small squares)
    for cx in range(3):
        for cy in range(3):
            fill(g, 5 + cx * 2, 5 + cy * 2, 1, 1, PURPLE)
    # Center bright
    setpx(g, 7, 7, (180, 100, 255, 255))
    # Connection indicator
    setpx(g, 7, 2, PURPLE_DIM)
    setpx(g, 8, 2, PURPLE_DIM)
    setpx(g, 7, 13, PURPLE_DIM)
    setpx(g, 8, 13, PURPLE_DIM)
    save_png(f"{BLOCKS_DIR}/be_storage_bus.png", g)

def gen_energy_acceptor():
    """ME Energy Acceptor - yellow/gold lightning bolt symbol."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    # Lightning bolt
    bolt = YELLOW
    bolt_dim = YELLOW_DIM
    setpx(g, 9, 4, bolt)
    setpx(g, 8, 5, bolt)
    setpx(g, 7, 6, bolt)
    fill(g, 6, 7, 4, 1, bolt)
    setpx(g, 8, 8, bolt)
    setpx(g, 7, 9, bolt)
    setpx(g, 6, 10, bolt)
    setpx(g, 5, 11, bolt)
    # Glow effect
    setpx(g, 10, 4, bolt_dim)
    setpx(g, 9, 5, bolt_dim)
    setpx(g, 5, 10, bolt_dim)
    setpx(g, 4, 11, bolt_dim)
    # Corner EU indicators
    setpx(g, 3, 3, GOLD_DIM)
    setpx(g, 12, 3, GOLD_DIM)
    setpx(g, 3, 12, GOLD_DIM)
    setpx(g, 12, 12, GOLD_DIM)
    save_png(f"{BLOCKS_DIR}/be_energy_acceptor.png", g)

def gen_recipe_encoder():
    """ME Recipe Encoder - crafting grid with encode arrow."""
    g = machine_base()
    # 3x3 crafting grid (left side)
    grid_bg = (50, 55, 65, 255)
    grid_line = (70, 75, 85, 255)
    fill(g, 3, 4, 7, 7, grid_bg)
    for i in range(3):
        for j in range(3):
            fill(g, 4 + i * 2, 5 + j * 2, 1, 1, grid_line)
    # Arrow pointing right
    setpx(g, 10, 7, CYAN)
    setpx(g, 11, 7, CYAN)
    setpx(g, 11, 6, CYAN_DIM)
    setpx(g, 11, 8, CYAN_DIM)
    # Output slot (right)
    fill(g, 12, 6, 2, 3, grid_bg)
    setpx(g, 12, 7, CYAN_BRIGHT)
    # Label
    setpx(g, 7, 2, CYAN_DIM)
    setpx(g, 8, 2, CYAN_DIM)
    save_png(f"{BLOCKS_DIR}/be_recipe_encoder.png", g)

def gen_coprocessor():
    """ME Crafting Coprocessor - dual processing cores."""
    g = machine_base()
    # Two processor cores
    core_bg = (50, 55, 65, 255)
    # Core 1 (upper left)
    fill(g, 3, 3, 5, 5, core_bg)
    fill(g, 4, 4, 3, 3, BLUE_DIM)
    setpx(g, 5, 5, BLUE_BRIGHT)
    # Core 2 (lower right)
    fill(g, 8, 8, 5, 5, core_bg)
    fill(g, 9, 9, 3, 3, BLUE_DIM)
    setpx(g, 10, 10, BLUE_BRIGHT)
    # Connection trace between cores
    setpx(g, 7, 7, CYAN_DIM)
    setpx(g, 8, 7, CYAN_DIM)
    setpx(g, 7, 8, CYAN_DIM)
    # Corner indicators
    setpx(g, 3, 12, BLUE_DIM)
    setpx(g, 12, 3, BLUE_DIM)
    save_png(f"{BLOCKS_DIR}/be_coprocessor.png", g)

def gen_request_terminal():
    """ME Request Terminal - screen with request list + orange craft button."""
    g = machine_base()
    # Screen
    fill(g, 3, 3, 10, 10, SCREEN_BG)
    # List lines (like item requests)
    for row in range(4, 11, 2):
        fill(g, 4, row, 6, 1, SCREEN_LINE)
        setpx(g, 11, row, (60, 90, 40, 255))  # green checkmarks
    # Orange "craft" button at bottom
    fill(g, 4, 12, 4, 1, ORANGE)
    # Top accent
    setpx(g, 7, 3, ORANGE)
    setpx(g, 8, 3, ORANGE)
    save_png(f"{BLOCKS_DIR}/be_request_terminal.png", g)

def gen_redstone_emitter():
    """ME Redstone Emitter - machine with red indicator and comparator symbol."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    # Redstone torch/indicator (center)
    fill(g, 7, 5, 2, 3, RED)
    setpx(g, 7, 4, RED_DIM)
    setpx(g, 8, 4, RED_DIM)
    # Base
    fill(g, 6, 8, 4, 1, (80, 80, 90, 255))
    # Comparison symbol < >
    setpx(g, 5, 10, RED_DIM)
    setpx(g, 4, 11, RED_DIM)
    setpx(g, 5, 12, RED_DIM)  # <
    setpx(g, 10, 10, RED_DIM)
    setpx(g, 11, 11, RED_DIM)
    setpx(g, 10, 12, RED_DIM)  # >
    # Side redstone dust
    setpx(g, 2, 7, RED_DIM)
    setpx(g, 2, 8, RED_DIM)
    setpx(g, 13, 7, RED_DIM)
    setpx(g, 13, 8, RED_DIM)
    save_png(f"{BLOCKS_DIR}/be_redstone_emitter.png", g)

def gen_advanced_interface():
    """ME Advanced Interface - gold/brass connector with pipe ports."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    # Gold frame
    for i in range(4, 12):
        setpx(g, i, 4, GOLD_DIM)
        setpx(g, i, 11, GOLD_DIM)
        setpx(g, 4, i, GOLD_DIM)
        setpx(g, 11, i, GOLD_DIM)
    # Center diamond/interface symbol
    setpx(g, 7, 5, GOLD)
    setpx(g, 8, 5, GOLD)
    setpx(g, 6, 6, GOLD)
    setpx(g, 9, 6, GOLD)
    fill(g, 5, 7, 6, 2, GOLD)
    setpx(g, 6, 9, GOLD)
    setpx(g, 9, 9, GOLD)
    setpx(g, 7, 10, GOLD)
    setpx(g, 8, 10, GOLD)
    # Center bright
    fill(g, 7, 7, 2, 2, (240, 210, 80, 255))
    # Port indicators (N/S/E/W)
    setpx(g, 7, 2, GOLD_DIM)
    setpx(g, 8, 2, GOLD_DIM)
    setpx(g, 7, 13, GOLD_DIM)
    setpx(g, 8, 13, GOLD_DIM)
    save_png(f"{BLOCKS_DIR}/be_advanced_interface.png", g)

def gen_fluid_terminal():
    """ME Fluid Terminal - screen with blue fluid wave pattern."""
    g = machine_base()
    fill(g, 3, 3, 10, 10, SCREEN_BG)
    # Fluid wave pattern
    water_light = (40, 100, 180, 255)
    water_dark = (25, 60, 120, 255)
    for row in range(4, 12):
        for col in range(4, 12):
            if (col + row) % 3 == 0:
                setpx(g, col, row, water_light)
            elif (col + row) % 3 == 1:
                setpx(g, col, row, water_dark)
    # Fluid drops at top
    setpx(g, 7, 3, (60, 140, 220, 255))
    setpx(g, 8, 3, (60, 140, 220, 255))
    # Bottom bar
    fill(g, 4, 12, 8, 1, water_dark)
    save_png(f"{BLOCKS_DIR}/be_fluid_terminal.png", g)

def gen_fluid_importer():
    """ME Fluid Import Bus - like importer but blue-tinted."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    water = (40, 120, 200, 255)
    water_dim = (30, 80, 140, 255)
    # Blue arrow pointing down
    setpx(g, 7, 4, water)
    setpx(g, 8, 4, water)
    fill(g, 7, 5, 2, 4, water)
    setpx(g, 5, 8, water_dim)
    setpx(g, 6, 9, water)
    setpx(g, 7, 10, water)
    setpx(g, 8, 10, water)
    setpx(g, 9, 9, water)
    setpx(g, 10, 8, water_dim)
    fill(g, 3, 6, 1, 4, water_dim)
    fill(g, 12, 6, 1, 4, water_dim)
    # Fluid drops
    setpx(g, 5, 5, water_dim)
    setpx(g, 10, 6, water_dim)
    save_png(f"{BLOCKS_DIR}/be_fluid_importer.png", g)

def gen_fluid_exporter():
    """ME Fluid Export Bus - like exporter but blue-tinted."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    water = (40, 120, 200, 255)
    water_dim = (30, 80, 140, 255)
    # Blue arrow pointing up
    setpx(g, 7, 11, water)
    setpx(g, 8, 11, water)
    fill(g, 7, 7, 2, 4, water)
    setpx(g, 5, 7, water_dim)
    setpx(g, 6, 6, water)
    setpx(g, 7, 5, water)
    setpx(g, 8, 5, water)
    setpx(g, 9, 6, water)
    setpx(g, 10, 7, water_dim)
    fill(g, 3, 6, 1, 4, water_dim)
    fill(g, 12, 6, 1, 4, water_dim)
    setpx(g, 5, 10, water_dim)
    setpx(g, 10, 9, water_dim)
    save_png(f"{BLOCKS_DIR}/be_fluid_exporter.png", g)

def gen_fluid_storage_bus():
    """ME Fluid Storage Bus - purple connector with blue fluid accent."""
    g = machine_base()
    fill(g, 4, 4, 8, 8, DARK_CASE)
    water_purple = (80, 50, 180, 255)
    water_purple_dim = (55, 35, 120, 255)
    # Frame
    for i in range(4, 12):
        setpx(g, i, 4, water_purple_dim)
        setpx(g, i, 11, water_purple_dim)
        setpx(g, 4, i, water_purple_dim)
        setpx(g, 11, i, water_purple_dim)
    # Fluid drops in center
    water = (40, 120, 200, 255)
    setpx(g, 7, 6, water)
    setpx(g, 8, 6, water)
    setpx(g, 6, 7, water)
    setpx(g, 7, 7, (80, 160, 240, 255))
    setpx(g, 8, 7, (80, 160, 240, 255))
    setpx(g, 9, 7, water)
    setpx(g, 7, 8, water)
    setpx(g, 8, 8, water)
    setpx(g, 7, 9, (30, 80, 140, 255))
    # Connectors
    setpx(g, 7, 2, water_purple_dim)
    setpx(g, 8, 2, water_purple_dim)
    setpx(g, 7, 13, water_purple_dim)
    setpx(g, 8, 13, water_purple_dim)
    save_png(f"{BLOCKS_DIR}/be_fluid_storage_bus.png", g)


# ==================== ITEM TEXTURES ====================

ITEMS_DIR = "src/betaenergistics/assets/item"

def gen_storage_disk(tier, filename):
    """Storage disk - colored rectangle with circuit traces.
    Tiers: 0=1K(white), 1=4K(orange), 2=16K(magenta), 3=64K(blue), 4=256K(green), 5=1024K(red)
    """
    tier_colors = {
        0: ((200, 200, 210, 255), (160, 160, 170, 255)),  # White/silver
        1: ((220, 150, 40, 255), (170, 110, 25, 255)),     # Orange
        2: ((180, 60, 180, 255), (130, 40, 130, 255)),     # Magenta
        3: ((50, 100, 220, 255), (35, 70, 160, 255)),      # Blue
        4: ((50, 180, 70, 255), (35, 130, 50, 255)),       # Green
        5: ((200, 50, 50, 255), (150, 35, 35, 255)),       # Red
    }
    main, dark = tier_colors.get(tier, tier_colors[0])

    g = blank(TRANS)
    # Disk body
    fill(g, 3, 4, 10, 9, (50, 50, 58, 255))
    fill(g, 4, 5, 8, 7, (60, 62, 70, 255))
    # Top edge
    fill(g, 3, 4, 10, 1, (40, 40, 48, 255))
    # Colored label area
    fill(g, 4, 5, 8, 3, main)
    fill(g, 5, 6, 6, 1, dark)
    # Circuit traces on lower part
    setpx(g, 5, 9, (80, 82, 92, 255))
    setpx(g, 6, 9, (80, 82, 92, 255))
    setpx(g, 5, 10, (80, 82, 92, 255))
    setpx(g, 8, 10, (80, 82, 92, 255))
    setpx(g, 9, 9, (80, 82, 92, 255))
    setpx(g, 10, 10, (80, 82, 92, 255))
    # Contact pins at bottom
    for x in [5, 7, 9]:
        setpx(g, x, 12, GOLD_DIM)
    # Corner notch
    setpx(g, 3, 4, TRANS)
    setpx(g, 12, 4, TRANS)
    save_png(f"{ITEMS_DIR}/{filename}", g)

def gen_fluid_disk(tier, filename):
    """Fluid disk - rounded shape with fluid color coding.
    Tiers: 0=8K(light blue), 1=32K(teal), 2=128K(dark blue), 3=512K(purple)
    """
    tier_colors = {
        0: ((100, 180, 240, 255), (70, 130, 180, 255)),    # Light blue
        1: ((40, 190, 180, 255), (30, 140, 130, 255)),     # Teal
        2: ((40, 70, 180, 255), (30, 50, 130, 255)),       # Dark blue
        3: ((130, 60, 200, 255), (95, 40, 150, 255)),      # Purple
    }
    main, dark = tier_colors.get(tier, tier_colors[0])

    g = blank(TRANS)
    # Rounded disk body (more rounded than storage disk)
    fill(g, 4, 4, 8, 9, (50, 55, 68, 255))
    fill(g, 5, 3, 6, 1, (50, 55, 68, 255))
    fill(g, 5, 13, 6, 1, (50, 55, 68, 255))
    # Inner
    fill(g, 5, 5, 6, 7, (60, 65, 78, 255))
    # Fluid window
    fill(g, 5, 5, 6, 3, main)
    fill(g, 6, 6, 4, 1, dark)
    # Fluid droplet symbol
    setpx(g, 7, 9, main)
    setpx(g, 8, 9, main)
    setpx(g, 7, 10, dark)
    setpx(g, 8, 10, dark)
    setpx(g, 7, 11, dark)
    # Contact pins
    for x in [6, 8]:
        setpx(g, x, 12, GOLD_DIM)
    save_png(f"{ITEMS_DIR}/{filename}", g)

def gen_pattern():
    """Pattern item - parchment with grid lines."""
    g = blank(TRANS)
    parch = (200, 190, 160, 255)
    parch_dark = (170, 160, 130, 255)
    parch_light = (220, 210, 180, 255)
    # Parchment body
    fill(g, 4, 2, 8, 12, parch)
    fill(g, 4, 2, 8, 1, parch_light)
    fill(g, 4, 13, 8, 1, parch_dark)
    # Folded corner
    setpx(g, 11, 2, TRANS)
    setpx(g, 10, 2, parch_dark)
    setpx(g, 11, 3, parch_dark)
    # Grid lines (3x3)
    for i in range(3):
        fill(g, 5, 4 + i * 3, 6, 1, parch_dark)
        fill(g, 5 + i * 2, 4, 1, 8, parch_dark)
    # Small cyan dots at intersections (circuit/recipe hint)
    for x in [5, 7, 9]:
        for y in [4, 7, 10]:
            setpx(g, x, y, CYAN_DIM)
    save_png(f"{ITEMS_DIR}/be_pattern.png", g)

def gen_mobile_terminal():
    """Mobile Terminal - handheld device with screen."""
    g = blank(TRANS)
    body = (55, 55, 65, 255)
    body_dark = (40, 40, 48, 255)
    # Device body
    fill(g, 5, 2, 6, 12, body)
    fill(g, 5, 2, 6, 1, body_dark)
    fill(g, 5, 13, 6, 1, body_dark)
    # Screen
    fill(g, 6, 3, 4, 7, SCREEN_BG)
    # Screen content (mini grid)
    for i in range(7, 10, 2):
        for j in range(4, 9, 2):
            setpx(g, i, j, SCREEN_LINE)
    setpx(g, 7, 3, SCREEN_BRIGHT)
    setpx(g, 8, 3, SCREEN_BRIGHT)
    # Button at bottom
    fill(g, 7, 11, 2, 1, CYAN_DIM)
    # Antenna
    setpx(g, 9, 1, (80, 80, 92, 255))
    setpx(g, 10, 1, TRANS)
    save_png(f"{ITEMS_DIR}/be_mobile_terminal.png", g)


# ==================== GENERATE ALL ====================

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    print("=== Generating Block Textures ===")
    gen_controller()
    gen_cable()
    gen_disk_drive()
    gen_grid_terminal()
    gen_crafting_terminal()
    gen_importer()
    gen_exporter()
    gen_autocrafter()
    gen_storage_bus()
    gen_energy_acceptor()
    gen_recipe_encoder()
    gen_coprocessor()
    gen_request_terminal()
    gen_redstone_emitter()
    gen_advanced_interface()
    gen_fluid_terminal()
    gen_fluid_importer()
    gen_fluid_exporter()
    gen_fluid_storage_bus()

    print("\n=== Generating Item Textures ===")
    # Storage disks (6 tiers)
    gen_storage_disk(0, "be_storage_disk_1k.png")
    gen_storage_disk(1, "be_storage_disk_4k.png")
    gen_storage_disk(2, "be_storage_disk_16k.png")
    gen_storage_disk(3, "be_storage_disk_64k.png")
    gen_storage_disk(4, "be_storage_disk_256k.png")
    gen_storage_disk(5, "be_storage_disk_1024k.png")
    # Keep base disk as 1K default
    gen_storage_disk(0, "be_storage_disk.png")

    # Fluid disks (4 tiers)
    gen_fluid_disk(0, "be_fluid_disk_8k.png")
    gen_fluid_disk(1, "be_fluid_disk_32k.png")
    gen_fluid_disk(2, "be_fluid_disk_128k.png")
    gen_fluid_disk(3, "be_fluid_disk_512k.png")
    # Keep base fluid disk as 8K default
    gen_fluid_disk(0, "be_fluid_disk.png")

    # Pattern
    gen_pattern()

    # Mobile terminal
    gen_mobile_terminal()

    print("\n=== Done! ===")
    print("Generated 19 block textures + 14 item textures")
