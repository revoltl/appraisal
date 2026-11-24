package com.ruivo.appraisal.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.state.BlockState;

public record AppraisalRecipeInput(BlockState state) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? new ItemStack(state.getBlock()) : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}
