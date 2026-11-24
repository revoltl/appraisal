package com.ruivo.appraisal.item;

import com.ruivo.appraisal.machine.BlockDataCardMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 方块数据卡。右键打开一个二十七格的存储面板，内容保存在原版容器组件里。
 * 卡内有物品时显示附魔光效，提示信息说明它在模型训练中的用途。
 */
public class BlockDataCardItem extends Item {

    public static final int SLOTS = 27;

    public BlockDataCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                            (containerId, inventory, p) ->
                                    new BlockDataCardMenu(containerId, inventory, stack),
                            Component.translatable("container.appraisal.block_data_card")),
                    buffer -> ItemStack.OPTIONAL_STREAM_CODEC.encode(
                            (RegistryFriendlyByteBuf) buffer, stack));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        return contents.nonEmptyStream().findAny().isPresent();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.appraisal.block_data_card.usage")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.appraisal.block_data_card.open")
                .withStyle(ChatFormatting.DARK_GRAY));

        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack item : contents.nonEmptyStream().toList()) {
            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, item)) {
                    existing.grow(item.getCount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(item.copy());
            }
        }

        if (merged.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.appraisal.block_data_card.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.appraisal.block_data_card.count", merged.size())
                .withStyle(ChatFormatting.GOLD));
        int shown = Math.min(merged.size(), 5);
        for (int index = 0; index < shown; index++) {
            ItemStack item = merged.get(index);
            tooltip.add(Component.translatable("tooltip.appraisal.block_data_card.item_count",
                    item.getHoverName(), item.getCount()).withStyle(ChatFormatting.GRAY));
        }
        if (merged.size() > shown) {
            tooltip.add(Component.translatable("tooltip.appraisal.block_data_card.more",
                    merged.size() - shown).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
