package com.ruivo.appraisal.item;

import com.ruivo.appraisal.AppraisalConfig;

import com.ruivo.appraisal.machine.DecayGeneratorBlockEntity;
import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.plugin.PluginSpec;
import com.ruivo.appraisal.plugin.PluginStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 衰变发电机的方块物品。Tooltip 读取方块数据里的插件槽，按插件状态显示容量、传输和产出，
 * 有加成时在括号里标出增加量。
 */
public class DecayGeneratorItem extends BlockItem {

    public DecayGeneratorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        PluginStats stats = readStats(stack, context);
        int generation = (int) Math.round(AppraisalConfig.DECAY_GENERATOR_GENERATION.get()
                * stats.generatorEfficiencyMultiplier());
        int transfer = (int) Math.round(AppraisalConfig.DECAY_GENERATOR_TRANSFER.get()
                * stats.transferMultiplier());
        int capacity = (int) Math.round(AppraisalConfig.DECAY_GENERATOR_CAPACITY.get()
                * stats.capacityMultiplier());

        tooltip.add(Component.translatable("tooltip.appraisal.decay_generator.usage")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.appraisal.decay_generator.generation",
                        withBonus(generation + " FE/t",
                                generation - AppraisalConfig.DECAY_GENERATOR_GENERATION.get(),
                                " FE/t"))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.appraisal.decay_generator.transfer",
                        withBonus(String.format(Locale.ROOT, "%,d FE/t", transfer),
                                transfer - AppraisalConfig.DECAY_GENERATOR_TRANSFER.get(),
                                " FE/t"))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.appraisal.decay_generator.capacity",
                        capacityWithBonus(capacity))
                .withStyle(ChatFormatting.GRAY));
    }

    private static PluginStats readStats(ItemStack stack, TooltipContext context) {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return PluginStats.EMPTY;
        }
        CompoundTag tag = data.copyTag();
        if (!tag.contains("inventory")) {
            return PluginStats.EMPTY;
        }
        ItemStackHandler handler = new ItemStackHandler(DecayGeneratorBlockEntity.SLOT_COUNT);
        handler.deserializeNBT(context.registries(), tag.getCompound("inventory"));
        List<PluginSpec> specs = new ArrayList<>();
        for (int index = DecayGeneratorBlockEntity.PLUGIN_START;
             index < DecayGeneratorBlockEntity.SLOT_COUNT; index++) {
            ItemStack pluginStack = handler.getStackInSlot(index);
            if (pluginStack.getItem() instanceof PluginItem plugin) {
                specs.add(plugin.spec());
            }
        }
        return PluginStats.combine(specs);
    }

    private static Component withBonus(String value, int bonus, String unit) {
        if (bonus <= 0) {
            return Component.literal(value);
        }
        return Component.translatable("tooltip.appraisal.decay_generator.with_bonus",
                value, bonus + unit);
    }

    private static Component capacityWithBonus(int capacity) {
        int bonus = capacity - AppraisalConfig.DECAY_GENERATOR_CAPACITY.get();
        String value = formatEnergy(capacity);
        if (bonus <= 0) {
            return Component.literal(value);
        }
        return Component.translatable("tooltip.appraisal.decay_generator.with_bonus",
                value, formatEnergy(bonus));
    }

    private static String formatEnergy(int value) {
        if (value >= 1000 && value % 1000 == 0) {
            return (value / 1000) + "k";
        }
        return String.format(Locale.ROOT, "%,d", value);
    }
}
