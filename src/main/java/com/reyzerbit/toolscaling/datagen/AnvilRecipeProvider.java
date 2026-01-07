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
        Path recipeFolder = this.output.getOutputFolder()
                .resolve("data/" + ModConstants.MODID + "/anvil_recipes");

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
                "minecraft:stone_tool_materials", // wooden -> stone
                "forge:ingots/iron",              // stone -> iron
                "forge:ingots/gold",              // iron -> golden
                "forge:gems/diamond",             // golden -> diamond
                null                               // diamond (no upgrade to netherite)
        };

        return CompletableFuture.allOf(
                // Generate normal tier progression recipes
                generateTierProgressionRecipes(cache, recipeFolder, tools, tierNames, upgradeMaterials),

                // Generate special iron -> diamond shortcut recipes
                generateIronToDiamondShortcuts(cache, recipeFolder, tools)
        );
    }

    private CompletableFuture<?> generateTierProgressionRecipes(
            CachedOutput cache, Path recipeFolder, Item[][] tools,
            String[] tierNames, String[] upgradeMaterials)
    {

        CompletableFuture<?>[] futures = new CompletableFuture[tools.length * 4]; // 5 tool types * 4 upgrades
        int index = 0;

        for (int toolType = 0; toolType < tools.length; toolType++)
        {
            for (int tier = 0; tier < 4; tier++)
            { // 0-3: wood->stone, stone->iron, iron->gold, gold->diamond
                Item baseItem = tools[toolType][tier];
                Item resultItem = tools[toolType][tier + 1];
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

    private CompletableFuture<?> generateIronToDiamondShortcuts(
            CachedOutput cache, Path recipeFolder, Item[][] tools)
    {

        CompletableFuture<?>[] futures = new CompletableFuture[tools.length]; // 5 tool types

        for (int toolType = 0; toolType < tools.length; toolType++)
        {
            Item ironTool = tools[toolType][2]; // Iron tier (index 2)
            Item diamondTool = tools[toolType][4]; // Diamond tier (index 4)
            String ironName = getItemName(ironTool);
            String diamondName = getItemName(diamondTool);

            JsonObject recipe = new JsonObject();

            // Left input (iron tool)
            JsonObject left = new JsonObject();
            left.addProperty("item", "minecraft:" + ironName);
            recipe.add("left", left);

            // Right input (diamond)
            JsonObject right = new JsonObject();
            right.addProperty("tag", "forge:gems/diamond");
            recipe.add("right", right);

            // Result (diamond tool)
            JsonObject result = new JsonObject();
            result.addProperty("item", "minecraft:" + diamondName);
            recipe.add("result", result);

            // Costs (higher for skipping tiers)
            recipe.addProperty("material_cost", 1);
            recipe.addProperty("level_cost", 3);
            recipe.addProperty("durability_mode", "PERCENTAGE");

            String fileName = "iron_to_diamond_" + ironName + ".json";
            Path recipePath = recipeFolder.resolve(fileName);

            futures[toolType] = DataProvider.saveStable(cache, recipe, recipePath);
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
