package com.reyzerbit.toolscaling.jei;

import com.reyzerbit.toolscaling.ModConstants;
import com.reyzerbit.toolscaling.ToolScaling;
import com.reyzerbit.toolscaling.anvil.AnvilToolUpgradeRecipe;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
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
                final String from = ForgeRegistries.ITEMS.getKey(recipe.getLeftInput().getItems()[0].getItem()).getPath();
                final String to = ForgeRegistries.ITEMS.getKey(recipe.getResultItem().getItem()).getPath();
                return new ResourceLocation(ModConstants.MODID, from + "_to_" + to);
            }
        };
    }
}
