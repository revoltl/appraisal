package com.ruivo.appraisal.item;

import com.ruivo.appraisal.AppraisalConfig;
import com.ruivo.appraisal.appraisal.AppraisalDrop;
import com.ruivo.appraisal.appraisal.AppraisalEngine;
import com.ruivo.appraisal.appraisal.AppraisalLevels;
import com.ruivo.appraisal.appraisal.AppraisalMode;
import com.ruivo.appraisal.appraisal.OutputResolver;
import com.ruivo.appraisal.machine.ToolPluginMenu;
import com.ruivo.appraisal.plugin.tool.PluginConfigurableTool;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MagnifyingGlassItem extends Item implements AppraisalTool {

    private final int appraisalLevel;

    public MagnifyingGlassItem(Properties properties, int appraisalLevel) {
        super(properties);
        this.appraisalLevel = appraisalLevel;
    }

    public int getAppraisalLevel() {
        return appraisalLevel;
    }

    @Override
    public int appraisalLevel(ItemStack stack) {
        return appraisalLevel;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack tool = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), tool)) {
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(pos);
        if (!AppraisalConfig.ALLOW_BLOCK_ENTITIES.get() && state.hasBlockEntity()) {
            return InteractionResult.PASS;
        }

        AppraisalRecipe recipe = AppraisalEngine.findRecipe(level, state);
        if (recipe == null || !recipe.allows(AppraisalMode.DIRECT)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!canConsume(tool, player, recipe)) {
            player.displayClientMessage(
                    Component.translatable("message.appraisal.not_enough_energy"), true);
            return InteractionResult.PASS;
        }

        int playerLevel = AppraisalLevels.of(player, tool);
        if (recipe.minLevel() > playerLevel) {
            player.displayClientMessage(
                    Component.translatable("message.appraisal.level_too_low", recipe.minLevel()), true);
            return InteractionResult.PASS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        List<AppraisalDrop> drops = roll(tool, recipe, playerLevel, serverLevel);
        if (drops.isEmpty()) {
            return InteractionResult.PASS;
        }

        OutputResolver.applyToWorld(serverLevel, pos, drops);

        consume(tool, player, recipe);

        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.4F);
        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
                12, 0.35D, 0.35D, 0.35D, 0.02D
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (openPluginMenu(level, player, hand)) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    protected boolean openPluginMenu(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }
        ItemStack tool = player.getItemInHand(hand);
        if (!(tool.getItem() instanceof PluginConfigurableTool)) {
            return false;
        }
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new ToolPluginMenu(id, inventory),
                    Component.translatable("gui.appraisal.tool_plugins")));
        }
        return true;
    }

    protected List<AppraisalDrop> roll(ItemStack tool, AppraisalRecipe recipe, int level, ServerLevel serverLevel) {
        return AppraisalEngine.roll(recipe, AppraisalMode.DIRECT, level, serverLevel.random);
    }

    protected boolean canConsume(ItemStack stack, Player player, AppraisalRecipe recipe) {
        return true;
    }

    protected void consume(ItemStack stack, Player player, AppraisalRecipe recipe) {
        int cost = AppraisalConfig.DURABILITY_COST.get();
        if (cost > 0) {
            stack.hurtAndBreak(cost, player, EquipmentSlot.MAINHAND);
        }
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(Items.GLASS) || repairCandidate.is(Items.AMETHYST_SHARD);
    }
}
