package com.ruivo.appraisal.item;

import com.ruivo.appraisal.experience.ExperienceRules;
import com.ruivo.appraisal.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 经验数据卡。Shift 右键按先验阶段存入，右键按先验阶段取出，不收取手续费。
 * 每次操作后在动作栏显示卡内当前状态。
 */
public class ExperienceDataCardItem extends Item {

    public ExperienceDataCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        int stored = getStoredExperience(stack);

        if (player.isShiftKeyDown()) {
            int target = nextStoreTarget(stored);
            int amount = Math.min(player.totalExperience, target - stored);
            if (amount <= 0) {
                showStatus(player, stack);
                return InteractionResultHolder.pass(stack);
            }
            setStoredExperience(stack, stored + amount);
            player.giveExperiencePoints(-amount);
            playPickup(level, player);
            showStatus(player, stack);
            return InteractionResultHolder.success(stack);
        }

        if (stored <= 0) {
            showStatus(player, stack);
            return InteractionResultHolder.pass(stack);
        }
        int target = nextRetrieveTarget(stored);
        int amount = stored - target;
        setStoredExperience(stack, target);
        player.giveExperiencePoints(amount);
        playPickup(level, player);
        showStatus(player, stack);
        return InteractionResultHolder.success(stack);
    }

    /**
     * 存入按阶段推进：先到 I 的门槛，再到 II 的门槛，最后到上限。
     */
    private static int nextStoreTarget(int stored) {
        if (stored < ExperienceRules.PRIOR_ONE) {
            return ExperienceRules.PRIOR_ONE;
        }
        if (stored < ExperienceRules.PRIOR_TWO) {
            return ExperienceRules.PRIOR_TWO;
        }
        return ExperienceRules.CARD_CAPACITY;
    }

    /**
     * 取出按阶段回退：III 降到 II，II 降到 I，I 清空。中间值取到上一阶段为止。
     */
    private static int nextRetrieveTarget(int stored) {
        if (stored > ExperienceRules.PRIOR_TWO) {
            return ExperienceRules.PRIOR_TWO;
        }
        if (stored > ExperienceRules.PRIOR_ONE) {
            return ExperienceRules.PRIOR_ONE;
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int stored = getStoredExperience(stack);
        tooltip.add(Component.translatable("tooltip.appraisal.experience_data_card.stored",
                stored, ExperienceRules.CARD_CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.appraisal.experience_data_card.prior",
                priorLevel(stored)).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.appraisal.experience_data_card.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getStoredExperience(stack) > 0;
    }

    public static int getStoredExperience(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_EXPERIENCE.get(), 0);
    }

    public static void setStoredExperience(ItemStack stack, int value) {
        stack.set(ModDataComponents.STORED_EXPERIENCE.get(),
                Mth.clamp(value, 0, ExperienceRules.CARD_CAPACITY));
    }

    public static Component priorLevel(int stored) {
        if (stored >= ExperienceRules.PRIOR_THREE) {
            return Component.literal("III");
        }
        if (stored >= ExperienceRules.PRIOR_TWO) {
            return Component.literal("II");
        }
        if (stored >= ExperienceRules.PRIOR_ONE) {
            return Component.literal("I");
        }
        return Component.translatable("tooltip.appraisal.experience_data_card.prior.none");
    }

    private static void showStatus(Player player, ItemStack stack) {
        int stored = getStoredExperience(stack);
        player.displayClientMessage(Component.translatable(
                "message.appraisal.experience_data_card.status",
                stored, ExperienceRules.CARD_CAPACITY, priorLevel(stored)), true);
    }

    private static void playPickup(Level level, Player player) {
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.1F, 0.9F + level.random.nextFloat() * 0.35F);
    }
}
