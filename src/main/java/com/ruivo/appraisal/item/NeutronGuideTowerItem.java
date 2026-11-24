package com.ruivo.appraisal.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * 中子引导塔的方块物品。Tooltip 说明四角放马铃薯。
 */
public class NeutronGuideTowerItem extends BlockItem {

    public NeutronGuideTowerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.appraisal.neutron_guide_tower.usage")
                .withStyle(ChatFormatting.GRAY));
    }
}
