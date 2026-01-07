package com.reyzerbit.toolscaling.anvil;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AnvilRecipeMatcher
{
    private final List<AnvilToolUpgradeRecipe> recipes;

    public AnvilRecipeMatcher(List<AnvilToolUpgradeRecipe> recipes)
    {
        this.recipes = recipes;
    }

    /**
     * Find a recipe that matches the given inputs
     *
     * @param left  The left input (base item)
     * @param right The right input (upgrade material)
     * @return The matching recipe, or null if none found
     */
    public AnvilToolUpgradeRecipe findRecipe(ItemStack left, ItemStack right)
    {
        if (left.isEmpty() || right.isEmpty())
        {
            return null;
        }

        for (AnvilToolUpgradeRecipe recipe : recipes)
        {
            if (recipe.matches(left, right))
            {
                return recipe;
            }
        }

        return null;
    }

    public int getRecipeCount()
    {
        return recipes.size();
    }
}
