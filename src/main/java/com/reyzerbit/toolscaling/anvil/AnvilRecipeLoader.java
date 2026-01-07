package com.reyzerbit.toolscaling.anvil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AnvilRecipeLoader
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String RECIPE_FOLDER = "anvil_recipes";

    public static List<AnvilToolUpgradeRecipe> loadRecipes()
    {
        List<AnvilToolUpgradeRecipe> recipes = new ArrayList<>();

        for (IModFileInfo modFileInfo : ModList.get().getModFiles())
        {
            IModFile modFile = modFileInfo.getFile();
            String modId = modFileInfo.getMods().get(0).getModId();

            Path recipePath = modFile.findResource("data", modId, RECIPE_FOLDER);

            if (Files.exists(recipePath) && Files.isDirectory(recipePath))
            {
                try (Stream<Path> paths = Files.walk(recipePath))
                {
                    paths.filter(path -> path.toString().endsWith(".json")).forEach(path ->
                        {
                            try
                            {
                                AnvilToolUpgradeRecipe recipe = loadRecipe(path, modId);
                                if (recipe != null)
                                {
                                    recipes.add(recipe);
                                    LOGGER.debug("Loaded anvil recipe from: {}", path.getFileName());
                                }
                            }
                            catch (Exception e)
                            {
                                LOGGER.error("Failed to load anvil recipe from {}: {}", path, e.getMessage());
                            }
                        });
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to scan anvil recipes for mod {}: {}", modId, e.getMessage());
                }
            }
        }

        LOGGER.info("Loaded {} anvil upgrade recipes from mods", recipes.size());
        return recipes;
    }

    public static List<AnvilToolUpgradeRecipe> loadDatapackRecipes(List<Path> datapackPaths)
    {
        List<AnvilToolUpgradeRecipe> recipes = new ArrayList<>();

        for (Path datapackPath : datapackPaths)
        {
            if (!Files.exists(datapackPath)) continue;

            if (Files.isDirectory(datapackPath))
            {
                recipes.addAll(loadFromDirectory(datapackPath));
            }
            else if (datapackPath.toString().toLowerCase().endsWith(".zip"))
            {
                recipes.addAll(loadFromZip(datapackPath));
            }
        }

        LOGGER.info("Loaded {} anvil recipes from datapacks", recipes.size());
        return recipes;
    }

    /**
     * Load recipes from an unzipped datapack directory
     */
    private static List<AnvilToolUpgradeRecipe> loadFromDirectory(Path datapackRoot)
    {
        List<AnvilToolUpgradeRecipe> recipes = new ArrayList<>();

        try
        {
            Path dataFolder = datapackRoot.resolve("data");
            if (!Files.exists(dataFolder)) return recipes;

            try (Stream<Path> namespaceDirs = Files.list(dataFolder))
            {
                namespaceDirs.filter(Files::isDirectory).forEach(namespaceDir ->
                    {
                        String namespace = namespaceDir.getFileName().toString();
                        Path recipeFolder = namespaceDir.resolve(RECIPE_FOLDER);

                        if (Files.exists(recipeFolder) && Files.isDirectory(recipeFolder))
                        {
                            try (Stream<Path> recipePaths = Files.walk(recipeFolder))
                            {
                                recipePaths.filter(path -> path.toString().endsWith(".json")).forEach(path ->
                                    {
                                        try
                                        {
                                            AnvilToolUpgradeRecipe recipe = loadRecipe(path, namespace);
                                            if (recipe != null)
                                            {
                                                recipes.add(recipe);
                                                LOGGER.debug("Loaded datapack anvil recipe from directory: {}", path.getFileName());
                                            }
                                        }
                                        catch (Exception e) { LOGGER.error("Failed to load datapack anvil recipe from {}: {}", path, e.getMessage()); }
                                    });
                            }
                            catch (IOException e) { LOGGER.error("Failed to scan datapack recipes for namespace {}: {}", namespace, e.getMessage()); }
                        }
                    });
            }
        }
        catch (IOException e) { LOGGER.error("Failed to scan datapack folder {}: {}", datapackRoot, e.getMessage()); }

        return recipes;
    }

    private static List<AnvilToolUpgradeRecipe> loadFromZip(Path zipPath)
    {
        List<AnvilToolUpgradeRecipe> recipes = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipPath.toFile()))
        {
            zipFile.stream()
                .filter(entry -> !entry.isDirectory())
                .filter(entry -> entry.getName().contains("/data/"))
                .filter(entry -> entry.getName().contains("/" + RECIPE_FOLDER + "/"))
                .filter(entry -> entry.getName().endsWith(".json"))
                .forEach(entry ->
                {
                    try
                    {
                        // Extract namespace from path: data/<namespace>/anvil_recipes/recipe.json
                        String entryPath = entry.getName();
                        String[] parts = entryPath.split("/");
                        String namespace = "unknown";

                        for (int i = 0; i < parts.length - 1; i++)
                        {
                            if (parts[i].equals("data") && i + 1 < parts.length)
                            {
                                namespace = parts[i + 1];
                                break;
                            }
                        }

                        // Load the recipe from the ZIP entry
                        AnvilToolUpgradeRecipe recipe = loadRecipeFromZip(zipFile, entry, namespace);
                        if (recipe != null)
                        {
                            recipes.add(recipe);
                            LOGGER.debug("Loaded datapack anvil recipe from ZIP: {}", entry.getName());
                        }
                    }
                    catch (Exception e) { LOGGER.error("Failed to load recipe from ZIP entry {}: {}", entry.getName(), e.getMessage()); }
                });
        }
        catch (IOException e) { LOGGER.error("Failed to read ZIP datapack {}: {}", zipPath, e.getMessage()); }

        return recipes;
    }

    private static AnvilToolUpgradeRecipe loadRecipeFromZip(ZipFile zipFile, ZipEntry entry, String namespace) throws IOException
    {
        try (InputStream inputStream = zipFile.getInputStream(entry); InputStreamReader reader = new InputStreamReader(inputStream))
        {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            Ingredient leftInput = Ingredient.fromJson(json.getAsJsonObject("left"));
            Ingredient rightInput = Ingredient.fromJson(json.getAsJsonObject("right"));

            ItemStack result = CraftingHelper.getItemStack(json.getAsJsonObject("result"), true);

            int materialCost = json.has("material_cost") ? json.get("material_cost").getAsInt() : 1;
            int levelCost = json.has("level_cost") ? json.get("level_cost").getAsInt() : 1;

            String durabilityModeStr = json.has("durability_mode") ? json.get("durability_mode").getAsString() : "PERCENTAGE";
            DurabilityTransferMode durabilityMode = DurabilityTransferMode.fromString(durabilityModeStr);

            return new AnvilToolUpgradeRecipe(
                leftInput,
                rightInput,
                result,
                materialCost,
                levelCost,
                durabilityMode
            );
        }
    }

    private static AnvilToolUpgradeRecipe loadRecipe(Path path, String modId) throws IOException
    {
        try (InputStream inputStream = Files.newInputStream(path); InputStreamReader reader = new InputStreamReader(inputStream))
        {

            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            Ingredient leftInput = Ingredient.fromJson(json.getAsJsonObject("left"));
            Ingredient rightInput = Ingredient.fromJson(json.getAsJsonObject("right"));

            ItemStack result = CraftingHelper.getItemStack(json.getAsJsonObject("result"), true);

            int materialCost = json.has("material_cost") ? json.get("material_cost").getAsInt() : 1;
            int levelCost = json.has("level_cost") ? json.get("level_cost").getAsInt() : 1;

            String durabilityModeStr = json.has("durability_mode") ? json.get("durability_mode").getAsString() : "PERCENTAGE";
            DurabilityTransferMode durabilityMode = DurabilityTransferMode.fromString(durabilityModeStr);

            return new AnvilToolUpgradeRecipe(
                leftInput,
                rightInput,
                result,
                materialCost,
                levelCost,
                durabilityMode
            );
        }
    }
}
