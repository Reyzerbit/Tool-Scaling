package com.reyzerbit.toolscaling;

import com.mojang.logging.LogUtils;
import com.reyzerbit.toolscaling.anvil.AnvilRecipeLoader;
import com.reyzerbit.toolscaling.anvil.AnvilRecipeMatcher;
import com.reyzerbit.toolscaling.anvil.AnvilToolUpgradeRecipe;
import com.reyzerbit.toolscaling.datagen.AnvilRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod(ModConstants.MODID)
public class ToolScaling
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private AnvilRecipeMatcher recipeMatcher;
    private List<AnvilToolUpgradeRecipe> modRecipes;

    public ToolScaling(IEventBus modEventBus)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        NeoForge.EVENT_BUS.addListener(this::handleAnvilRecipes);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        LOGGER.info("Tool Scaling mod initialized");
    }

    private void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeServer(), new AnvilRecipeProvider(output));
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            modRecipes = AnvilRecipeLoader.loadRecipes();
            recipeMatcher = new AnvilRecipeMatcher(modRecipes);

            LOGGER.info("Loaded {} anvil upgrade recipes from mods", recipeMatcher.getRecipeCount());
        });
    }

    private void onServerStarting(ServerStartingEvent event)
    {
        List<AnvilToolUpgradeRecipe> allRecipes = new ArrayList<>(modRecipes);

        List<Path> datapackPaths = new ArrayList<>();

        try
        {
            Path worldPath = event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            Path datapackFolder = worldPath.resolve("datapacks");

            if (java.nio.file.Files.exists(datapackFolder) && java.nio.file.Files.isDirectory(datapackFolder))
            {
                try (java.util.stream.Stream<Path> datapacks = java.nio.file.Files.list(datapackFolder))
                {
                    datapacks
                        .filter(path -> java.nio.file.Files.isDirectory(path) || path.toString().toLowerCase().endsWith(".zip"))
                        .forEach(datapackPaths::add);
                }
            }
        }
        catch (Exception e) { LOGGER.error("Failed to scan datapack folder: {}", e.getMessage()); }

        List<AnvilToolUpgradeRecipe> datapackRecipes = AnvilRecipeLoader.loadDatapackRecipes(datapackPaths);
        allRecipes.addAll(datapackRecipes);

        recipeMatcher = new AnvilRecipeMatcher(allRecipes);

        LOGGER.info("Loaded total of {} anvil upgrade recipes ({} from mods, {} from datapacks)", allRecipes.size(), modRecipes.size(), datapackRecipes.size());
    }

    private void handleAnvilRecipes(AnvilUpdateEvent event)
    {
        if (recipeMatcher == null) return;

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        AnvilToolUpgradeRecipe recipe = recipeMatcher.findRecipe(left, right);

        if (recipe != null)
        {
            ItemStack result = recipe.getResult(left);

            event.setOutput(result);
            event.setMaterialCost(recipe.getMaterialCost());
            event.setCost(recipe.getLevelCost());
        }
    }
}
