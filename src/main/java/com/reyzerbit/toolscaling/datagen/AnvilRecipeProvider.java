package com.reyzerbit.toolscaling.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.reyzerbit.toolscaling.ModConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class AnvilRecipeProvider implements DataProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;

    public AnvilRecipeProvider(PackOutput output)
    {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache)
    {
        Path recipeFolder = this.output.getOutputFolder().resolve("data/" + ModConstants.MODID + "/anvil_recipes");

        // Tool types
        Item[][] tools = {
            {Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE},
            {Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE},
            {Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL},
            {Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE},
            {Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD}
        };

        String[] tierNames = {"wooden", "stone", "iron", "golden", "diamond"};
        String[] upgradeMaterials = {
            "minecraft:stone_tool_materials",
            "forge:ingots/iron",
            "forge:ingots/gold",
            "forge:gems/diamond",
            null
        };

        // Armor types
        Item[][] armor = {
            {Items.LEATHER_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET},
            {Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE},
            {Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS},
            {Items.LEATHER_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS}
        };

        String[] armorTierNames = {"leather", "iron", "golden", "diamond"};
        String[] armorUpgradeMaterials = {
            "forge:ingots/iron",
            "forge:ingots/gold",
            "forge:gems/diamond",
            null
        };

        return CompletableFuture.allOf(
            generateTierProgressionRecipes(cache, recipeFolder, tools, tierNames, upgradeMaterials, 4),
            generateTierProgressionRecipes(cache, recipeFolder, armor, armorTierNames, armorUpgradeMaterials, 3),
            generateShortcuts(cache, recipeFolder, tools, tierNames, 2, 4),
            generateShortcuts(cache, recipeFolder, armor, armorTierNames, 1, 3)
        );
    }

    private CompletableFuture<?> generateTierProgressionRecipes(CachedOutput cache, Path recipeFolder, Item[][] items, String[] tierNames, String[] upgradeMaterials, int upgradeCount)
    {

        CompletableFuture<?>[] futures = new CompletableFuture[items.length * upgradeCount]; // 5 tool types * 4 upgrades
        int index = 0;

        for (int toolType = 0; toolType < items.length; toolType++)
        {
            for (int tier = 0; tier < upgradeCount; tier++)
            { // 0-3: wood->stone, stone->iron, iron->gold, gold->diamond
                Item baseItem = items[toolType][tier];
                Item resultItem = items[toolType][tier + 1];
                String baseName = getItemName(baseItem);
                String resultName = getItemName(resultItem);
                String materialTag = upgradeMaterials[tier];

                JsonObject recipe = new JsonObject();

                // Left input (base tool)
                JsonObject left = new JsonObject();
                left.addProperty("item", "minecraft:" + baseName);
                recipe.add("left", left);

                // Right input (upgrade material)
                JsonObject right = new JsonObject();
                right.addProperty("tag", materialTag);
                recipe.add("right", right);

                // Result
                JsonObject result = new JsonObject();
                result.addProperty("item", "minecraft:" + resultName);
                recipe.add("result", result);

                // Costs
                recipe.addProperty("material_cost", 1);
                recipe.addProperty("level_cost", 1);
                recipe.addProperty("durability_mode", "PERCENTAGE");

                String fileName = tierNames[tier] + "_to_" + tierNames[tier + 1] + "_" + baseName + ".json";
                Path recipePath = recipeFolder.resolve(fileName);

                futures[index++] = DataProvider.saveStable(cache, recipe, recipePath);
            }
        }

        return CompletableFuture.allOf(futures);
    }

    private CompletableFuture<?> generateShortcuts(CachedOutput cache, Path recipeFolder, Item[][] items, String[] tierNames, int initialTier, int shortcutTier)
    {
        CompletableFuture<?>[] futures = new CompletableFuture[items.length]; // 5 tool types

        for (int tier = 0; tier < items.length; tier++)
        {
            Item initialItem = items[tier][initialTier];
            Item shortcutItem = items[tier][shortcutTier];

            String initialItemName = getItemName(initialItem);
            String shortcutItemName = getItemName(shortcutItem);

            JsonObject recipe = new JsonObject();

            JsonObject left = new JsonObject();
            left.addProperty("item", "minecraft:" + initialItemName);
            recipe.add("left", left);

            JsonObject right = new JsonObject();
            right.addProperty("tag", "forge:gems/diamond");
            recipe.add("right", right);

            JsonObject result = new JsonObject();
            result.addProperty("item", "minecraft:" + shortcutItemName);
            recipe.add("result", result);

            recipe.addProperty("material_cost", 1);
            recipe.addProperty("level_cost", 3);
            recipe.addProperty("durability_mode", "PERCENTAGE");

            String fileName = tierNames[initialTier] + "_to_" + tierNames[shortcutTier] + "_" + initialItemName + ".json";
            Path recipePath = recipeFolder.resolve(fileName);

            futures[tier] = DataProvider.saveStable(cache, recipe, recipePath);
        }

        return CompletableFuture.allOf(futures);
    }

    private String getItemName(Item item)
    {
        String fullName = item.toString();
        return fullName.replace("minecraft:", "");
    }

    @Override
    public String getName()
    {
        return "Anvil Recipes";
    }
}
