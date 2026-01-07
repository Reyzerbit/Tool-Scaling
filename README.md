# Tool Scaling

A Minecraft Forge mod that adds anvil-based tool upgrading with durability and enchantment preservation.

## Features

- **Anvil Tool Upgrades**: Upgrade tools in the anvil instead of crafting new ones
- **Durability Transfer**: Preserve tool durability when upgrading (percentage-based by default)
- **Data Preservation**: Keeps enchantments, custom names, lore, and all NBT data
- **Extensible Recipe System**: Other mods and datapacks can easily add their own upgrade recipes

## For Players

### How to Use

1. Place your tool in the left slot of an anvil
2. Place the upgrade material in the right slot (e.g., diamond for iron → diamond)
3. Take your upgraded tool with all enchantments and durability preserved!

### Available Upgrades

**Normal Tier Progression** (1 material, 1 level):
- Wooden → Stone (cobblestone, blackstone, etc.)
- Stone → Iron (iron ingot)
- Iron → Golden (gold ingot)
- Golden → Diamond (diamond)

**Special Shortcuts** (1 material, 3 levels):
- Iron → Diamond (diamond)

**Note**: Diamond → Netherite upgrades use the vanilla smithing table with netherite upgrade template.

---

## For Mod Developers

### Creating Custom Anvil Recipes

Tool Scaling automatically scans **all mods** in the classpath for custom anvil recipes. Simply add JSON files to your mod's resources:

```
src/main/resources/data/<your_mod_id>/anvil_recipes/
```

#### Recipe JSON Format

```json
{
  "left": {
    "item": "minecraft:iron_pickaxe"
  },
  "right": {
    "tag": "forge:gems/diamond"
  },
  "result": {
    "item": "minecraft:diamond_pickaxe"
  },
  "material_cost": 1,
  "level_cost": 1,
  "durability_mode": "PERCENTAGE"
}
```

#### Field Reference

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `left` | Ingredient | Base item (the tool being upgraded) | Yes |
| `right` | Ingredient | Upgrade material | Yes |
| `result` | ItemStack | Output item | Yes |
| `material_cost` | Integer | How many of the right item are consumed | No (default: 1) |
| `level_cost` | Integer | XP levels required | No (default: 1) |
| `durability_mode` | String | How durability is transferred (see below) | No (default: "PERCENTAGE") |

#### Ingredient Format

Use either a specific item:
```json
{
  "item": "minecraft:iron_pickaxe"
}
```

Or an item tag:
```json
{
  "tag": "forge:ingots/iron"
}
```

#### Durability Transfer Modes

- **`PERCENTAGE`** (default): Transfers durability as percentage
  - Example: 50% durability iron tool → 50% durability diamond tool
- **`AS_IS`**: Transfers damage value directly
  - Example: 100/250 damage iron tool → 100/1561 damage diamond tool
- **`FULL_REPAIR`**: Result has full durability regardless of input

### Example Recipes

#### Basic Tool Upgrade

```json
{
  "left": {
    "item": "minecraft:wooden_sword"
  },
  "right": {
    "tag": "minecraft:stone_tool_materials"
  },
  "result": {
    "item": "minecraft:stone_sword"
  },
  "material_cost": 1,
  "level_cost": 1,
  "durability_mode": "PERCENTAGE"
}
```

#### Expensive Shortcut Upgrade

```json
{
  "left": {
    "item": "minecraft:iron_pickaxe"
  },
  "right": {
    "tag": "forge:gems/diamond"
  },
  "result": {
    "item": "minecraft:diamond_pickaxe"
  },
  "material_cost": 2,
  "level_cost": 5,
  "durability_mode": "PERCENTAGE"
}
```

#### Full Repair Upgrade

```json
{
  "left": {
    "item": "mymod:broken_sword"
  },
  "right": {
    "item": "mymod:repair_kit"
  },
  "result": {
    "item": "mymod:broken_sword"
  },
  "material_cost": 1,
  "level_cost": 10,
  "durability_mode": "FULL_REPAIR"
}
```

#### Using Tags for Flexible Matching

```json
{
  "left": {
    "tag": "forge:tools/pickaxes"
  },
  "right": {
    "tag": "forge:gems/emerald"
  },
  "result": {
    "item": "mymod:emerald_pickaxe"
  },
  "material_cost": 3,
  "level_cost": 5,
  "durability_mode": "AS_IS"
}
```

### Important Notes

1. **Item Data Preservation**: ALL item data is automatically copied from input to output, including:
   - Enchantments
   - Custom names
   - Lore
   - Modded NBT data
   - Any other properties

2. **Recipe Loading**: Recipes are loaded during mod initialization. They're automatically discovered from all mods in the classpath.

3. **Recipe Priority**: If multiple recipes match the same inputs, the first one loaded wins. Control order by naming your files alphabetically if needed.

4. **No Restart Required**: For datapacks, recipes are loaded when joining a world. For mods, recipes are baked into the JAR.

---

## For Datapack Creators

Datapacks can also add custom anvil recipes! Since Tool Scaling scans the classpath, you can add recipes to your datapack:

```
datapacks/
└── your_datapack/
    └── data/
        └── your_namespace/
            └── anvil_recipes/
                └── your_recipe.json
```

**Note**: This requires Tool Scaling to be installed on the client/server.

---

## Building from Source

### Build Commands

```bash
# Build the mod
./gradlew build

# Run data generation (creates recipe JSONs)
./gradlew runData

# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer
```

The built JAR will be in `build/libs`

---

## API Documentation

### AnvilToolUpgradeRecipe

```java
public class AnvilToolUpgradeRecipe {
    public AnvilToolUpgradeRecipe(
        Ingredient leftInput,
        Ingredient rightInput,
        ItemStack result,
        int materialCost,
        int levelCost,
        DurabilityTransferMode durabilityMode
    )

    // Check if items match this recipe
    public boolean matches(ItemStack left, ItemStack right)

    // Get result with transferred data
    public ItemStack getResult(ItemStack leftItem)
}
```

### DurabilityTransferMode Enum

```java
public enum DurabilityTransferMode {
    AS_IS,        // Transfer damage value directly
    PERCENTAGE,   // Transfer durability percentage
    FULL_REPAIR   // Fully repair the result
}
```

---

## License

This mod is licensed under GPL v3. See LICENSE file for details.

---

## Credits

Created by Reyzerbit