package com.ruivo.appraisal.appraisal;

import com.ruivo.appraisal.compat.Integrations;
import com.ruivo.appraisal.recipe.AppraisalOutput;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.registry.ModRecipes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 鉴定统一入口。世界交互与机器加工都经由这里取配方和结算。
 *
 * <p>取到配方后会经过内置联动增补，结果按配方对象缓存。配方重载后旧对象被回收，
 * 缓存自动失效。</p>
 */
public final class AppraisalEngine {

    private static final Map<AppraisalRecipe, AppraisalRecipe> AUGMENTED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private AppraisalEngine() {
    }

    /**
     * 对配方执行全部内置联动，同一份配方只计算一次。
     */
    public static AppraisalRecipe augment(AppraisalRecipe recipe) {
        return AUGMENTED.computeIfAbsent(recipe, Integrations::augment);
    }

    /**
     * 按方块状态查找鉴定配方。客户端同样可用，前提是配方已同步。
     */
    @Nullable
    public static AppraisalRecipe findRecipe(Level level, BlockState state) {
        if (state.isAir() || state.hasBlockEntity()) {
            return null;
        }
        for (RecipeHolder<AppraisalRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.APPRAISAL_TYPE.get())) {
            AppraisalRecipe recipe = holder.value();
            if (recipe.input().test(state)) {
                return augment(recipe);
            }
        }
        return null;
    }

    public static List<AppraisalDrop> roll(AppraisalRecipe recipe, AppraisalMode mode, int level, RandomSource random) {
        return roll(recipe, mode, level, random, 1.0D);
    }

    /**
     * 带产物概率倍率的结算。倍率为一时与原行为完全一致，小于一或大于一时
     * 先把每个可行产物的概率乘上倍率，再交给概率核心。
     */
    public static List<AppraisalDrop> roll(AppraisalRecipe recipe, AppraisalMode mode, int level,
                                           RandomSource random, double chanceMultiplier) {
        return roll(recipe, mode, level, random, chanceMultiplier, Map.of(), false, Set.of());
    }

    /**
     * 带产物概率倍率、模型插件加算与抑制白名单的结算。
     *
     * <p>模型插件的训练结果按倍率存储，换算成百分数后直接加到对应产物的概率上。
     * 开启抑制时只保留白名单内的产物。</p>
     */
    public static List<AppraisalDrop> roll(AppraisalRecipe recipe, AppraisalMode mode, int level,
                                           RandomSource random, double chanceMultiplier,
                                           Map<Item, Double> bonusChances,
                                           boolean suppressOthers,
                                           Set<Item> whitelist) {
        if (!recipe.allows(mode)) {
            return List.of();
        }
        List<AppraisalOutput> available = new ArrayList<>();
        for (AppraisalOutput output : recipe.outputs()) {
            if (output.minLevel() > level) {
                continue;
            }
            Item item = output.result().item().value();
            if (suppressOthers && !whitelist.contains(item)) {
                continue;
            }
            double probability = (output.probability()
                    + bonusChances.getOrDefault(item, 0.0D) * 100.0D) * chanceMultiplier;
            available.add(new AppraisalOutput(output.result(), probability, output.minLevel()));
        }
        if (available.isEmpty()) {
            return List.of();
        }
        return switch (mode) {
            case DIRECT -> ProbabilityCalculator.rollWeighted(available, random);
            case ADVANCED -> ProbabilityCalculator.rollIndependent(available, random);
        };
    }
}
