package com.reyzerbit.toolscaling.anvil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class AnvilToolUpgradeRecipe
{
    private final Ingredient leftInput;
    private final Ingredient rightInput;
    private final ItemStack result;
    private final int materialCost;
    private final int levelCost;
    private final DurabilityTransferMode durabilityMode;

    public AnvilToolUpgradeRecipe(
        Ingredient leftInput,
        Ingredient rightInput,
        ItemStack result,
        int materialCost,
        int levelCost,
        DurabilityTransferMode durabilityMode
    )
    {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.result = result;
        this.materialCost = materialCost;
        this.levelCost = levelCost;
        this.durabilityMode = durabilityMode;
    }

    public boolean matches(ItemStack left, ItemStack right)
    {
        return this.leftInput.test(left) && this.rightInput.test(right);
    }

    public ItemStack getResult(ItemStack leftItem)
    {
        ItemStack resultCopy = this.result.copy();

        // Copy ALL NBT data from input to output (enchantments, custom name, modded data, etc.)
        if (leftItem.hasTag())
        {
            resultCopy.setTag(leftItem.getTag().copy());
        }

        // Handle durability based on mode
        if (leftItem.isDamageableItem() && resultCopy.isDamageableItem())
        {
            switch (this.durabilityMode)
            {
                case AS_IS:
                    // Transfer damage value as-is
                    int leftDamage = leftItem.getDamageValue();
                    resultCopy.setDamageValue(Math.min(leftDamage, resultCopy.getMaxDamage()));
                    break;

                case PERCENTAGE:
                    // Transfer durability percentage
                    int baseDamage = leftItem.getDamageValue();
                    int baseMaxDamage = leftItem.getMaxDamage();
                    float durabilityPercent = 1.0f - ((float) baseDamage / baseMaxDamage);

                    int resultMaxDamage = resultCopy.getMaxDamage();
                    int resultDamage = (int) ((1.0f - durabilityPercent) * resultMaxDamage);
                    resultCopy.setDamageValue(resultDamage);
                    break;

                case FULL_REPAIR:
                    // Leave result at full durability (damage = 0)
                    resultCopy.setDamageValue(0);
                    break;
            }
        }

        return resultCopy;
    }

    public Ingredient getLeftInput()
    {
        return leftInput;
    }

    public Ingredient getRightInput()
    {
        return rightInput;
    }

    public int getMaterialCost()
    {
        return materialCost;
    }

    public int getLevelCost()
    {
        return levelCost;
    }

    public DurabilityTransferMode getDurabilityMode()
    {
        return durabilityMode;
    }
}
