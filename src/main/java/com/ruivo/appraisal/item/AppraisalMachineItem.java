package com.ruivo.appraisal.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * 鉴定机的方块物品。Tooltip 说明它消耗电能进行进阶鉴定。
 */
public class AppraisalMachineItem extends BlockItem {

    public AppraisalMachineItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.appraisal.appraisal_machine.usage")
                .withStyle(ChatFormatting.GRAY));
    }
}
