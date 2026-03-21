# Beta Energistics — Digital Storage for Minecraft Beta 1.7.3

Inspired by Refined Storage (simplicity) and Applied Energistics (depth).

## Workflow
- Edit mod source ONLY in `src/betaenergistics/` (organized packages)
- NEVER edit `mcp/minecraft/src/net/minecraft/src/BE_*.java` directly — those are transpiled output
- `bash scripts/test.sh` auto-transpiles → builds → injects → launches
- `bash scripts/test_unit.sh` auto-transpiles → recompiles → runs JUnit tests
- `bash scripts/transpile.sh` src/ → mcp/minecraft/src/ (flatten only)

## Build Commands
- Test (in-game): `bash scripts/test.sh`
- Unit tests: `bash scripts/test_unit.sh`
- Transpile only: `bash scripts/transpile.sh`

## Project Structure
- **Mod source (edit here):** `src/betaenergistics/`
  - `storage/` — ItemKey, DiskStorage, CompositeStorage, RootStorage
  - `network/` — StorageNetwork, NetworkNode, cable graph traversal
  - `block/` — Block subclasses (Controller, DiskDrive, Grid, Cable, etc.)
  - `tile/` — TileEntity subclasses
  - `container/` — Container subclasses
  - `gui/` — GuiContainer subclasses
  - `item/` — Items (StorageDisk tiers, Cable, etc.)
  - `render/` — Block/Item renderers (cables, disk LEDs)
  - `assets/` — textures (blocks/, gui/, item/)
  - `mod_BetaEnergistics.java` — main mod class
- **Transpiled output (don't edit):** `mcp/minecraft/src/net/minecraft/src/`
- **Libraries:** `../../../libraries/` (modellib + machineapi, shared across workspace)

## Naming Conventions
- All classes use `BE_` prefix (e.g., `BE_BlockController`, `BE_TileDiskDrive`)
- Block IDs start at 240 (RetroNism: 200-229, IC2HM: 230-239)
- Item IDs start at 700 (RetroNism: 500-599, IC2HM: 600-699)

## Architecture (MVP)
- **StorageNetwork**: graph of NetworkNodes connected via cables
- **DiskStorage**: Map<ItemKey, Integer> with capacity limit
- **CompositeStorage**: aggregates all DiskStorages by priority
- **Controller**: heart of network, energy buffer (EU from IC2)
- **Grid Terminal**: GUI for viewing/inserting/extracting items from network

## Language
- User communicates in Portuguese (BR). Respond in Portuguese.
