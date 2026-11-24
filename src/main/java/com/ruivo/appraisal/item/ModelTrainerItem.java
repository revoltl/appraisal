package com.ruivo.appraisal.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * 模型训练机方块物品。Tooltip 说明它用数据卡训练插件。
 */
public class ModelTrainerItem extends BlockItem {

    public ModelTrainerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.appraisal.model_trainer.usage")
                .withStyle(ChatFormatting.GRAY));
    }
}
