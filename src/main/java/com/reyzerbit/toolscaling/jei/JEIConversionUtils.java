package com.reyzerbit.toolscaling.jei;

import com.reyzerbit.toolscaling.ModConstants;
import com.reyzerbit.toolscaling.anvil.AnvilToolUpgradeRecipe;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class JEIConversionUtils
{
    public static IJeiAnvilRecipe convertToJeiRecipe(AnvilToolUpgradeRecipe recipe)
    {
        return new IJeiAnvilRecipe()
        {
            @Override public @Unmodifiable List<ItemStack> getLeftInputs() { return List.of(recipe.getLeftInput().getItems()); }
            @Override public @Unmodifiable List<ItemStack> getRightInputs() { return List.of(recipe.getRightInput().getItems()); }
            @Override public @Unmodifiable List<ItemStack> getOutputs() { return List.of(recipe.getResultItem()); }

            @Override
            public @Nullable ResourceLocation getUid()
            {
                final String from = BuiltInRegistries.ITEM.getKey(recipe.getLeftInput().getItems()[0].getItem()).getPath();
                final String to = BuiltInRegistries.ITEM.getKey(recipe.getResultItem().getItem()).getPath();
                return ResourceLocation.fromNamespaceAndPath(ModConstants.MODID, from + "_to_" + to);
            }
        };
    }
}
