package com.reyzerbit.toolscaling.jei;

import com.reyzerbit.toolscaling.ModConstants;
import com.reyzerbit.toolscaling.anvil.AnvilRecipeLoader;
import com.reyzerbit.toolscaling.anvil.AnvilToolUpgradeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public class ToolScalingJEIPlugin implements IModPlugin
{
    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(ModConstants.MODID, "jei_plugin");

    @Override public ResourceLocation getPluginUid() { return PLUGIN_ID; }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        List<IJeiAnvilRecipe> recipes = AnvilRecipeLoader.loadRecipes().stream().map(JEIConversionUtils::convertToJeiRecipe).toList();
        registration.addRecipes(RecipeTypes.ANVIL, recipes);
    }
}
