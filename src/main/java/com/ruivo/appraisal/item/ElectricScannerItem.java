package com.ruivo.appraisal.item;

import com.ruivo.appraisal.appraisal.AppraisalDrop;
import com.ruivo.appraisal.appraisal.AppraisalEngine;
import com.ruivo.appraisal.appraisal.AppraisalMode;
import com.ruivo.appraisal.AppraisalConfig;
import com.ruivo.appraisal.plugin.tool.PluginConfigurableTool;
import com.ruivo.appraisal.plugin.tool.ToolPluginInventory;
import com.ruivo.appraisal.plugin.tool.ToolPluginStats;
import com.ruivo.appraisal.machine.SolarGeneration;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.registry.ModDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 电动扫描仪。鉴定等级二级，没有耐久，单次消耗按配方能耗换算。
 * 支持九个插件槽，电容插件改变最大容量，节能插件降低单次消耗，
 * 分类模型插件直接增幅产物权重或抑制其他产物。
 */
public class ElectricScannerItem extends MagnifyingGlassItem implements PluginConfigurableTool {

    public ElectricScannerItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public boolean hasEnergyDisplay(ItemStack stack) {
        return true;
    }

    @Override
    public int energyStored(ItemStack stack) {
        return getEnergy(stack);
    }

    @Override
    public int energyCapacity(ItemStack stack) {
        return capacity(stack);
    }

    public static int capacity(ItemStack stack) {
        Double multiplier = stack.get(ModDataComponents.SCANNER_CAPACITY_MULTIPLIER.get());
        if (multiplier == null) {
            multiplier = refreshCapacityMultiplier(stack);
        }
        return (int) Math.round(AppraisalConfig.SCANNER_CAPACITY.get() * multiplier);
    }

    public static double refreshCapacityMultiplier(ItemStack stack) {
        double multiplier = ToolPluginStats.general(stack).capacityMultiplier();
        stack.set(ModDataComponents.SCANNER_CAPACITY_MULTIPLIER.get(), multiplier);
        return multiplier;
    }

    @Override
    public void onPluginsChanged(ItemStack stack) {
        refreshCapacityMultiplier(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        int stored = getEnergy(stack);
        if (stored >= capacity(stack)) {
            return;
        }
        int solar = SolarGeneration.fePerTick(ToolPluginStats.general(stack), level, player.blockPosition());
        if (solar > 0) {
            setEnergy(stack, stored + solar);
        }
    }

    public static int getEnergy(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SCANNER_ENERGY.get(), 0);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        stack.set(ModDataComponents.SCANNER_ENERGY.get(), Mth.clamp(energy, 0, capacity(stack)));
    }

    public static int energyCost(ItemStack stack, AppraisalRecipe recipe) {
        double multiplier = ToolPluginStats.general(stack).energyMultiplier();
        return Math.max(0, (int) Math.round(recipe.energy() * multiplier));
    }

    @Override
    protected boolean canConsume(ItemStack stack, Player player, AppraisalRecipe recipe) {
        return player.isCreative() || getEnergy(stack) >= energyCost(stack, recipe);
    }

    @Override
    protected void consume(ItemStack stack, Player player, AppraisalRecipe recipe) {
        if (player.isCreative()) {
            return;
        }
        setEnergy(stack, getEnergy(stack) - energyCost(stack, recipe));
    }

    @Override
    public ToolPluginInventory toolPlugins(ItemStack stack) {
        return new ToolPluginInventory(stack);
    }

    @Override
    protected List<AppraisalDrop> roll(ItemStack tool, AppraisalRecipe recipe, int level, ServerLevel serverLevel) {
        ToolPluginStats stats = toolPluginStats(tool, recipe.input().representativeBlock());
        return AppraisalEngine.roll(recipe, AppraisalMode.DIRECT, level, serverLevel.random,
                1.0D, stats.bonusChances(), stats.suppressOthers(), stats.whitelist());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnergy(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getEnergy(stack) / capacity(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xE53935;
    }
}
