package com.ruivo.appraisal.compat.ftbultimine;

import com.ruivo.appraisal.item.AppraisalTool;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

/**
 * 连锁鉴定。FTB Ultimine 会把选中范围内的方块交给这里处理，逐个调用放大镜的
 * {@code useOn}，因此鉴定规则、耐久消耗与普通右键完全一致。
 */
public final class MagnifyingGlassRightClickHandler implements RightClickHandler {

    @Override
    public int handleRightClickBlock(ShapeContext context, InteractionHand hand, Collection<BlockPos> positions) {
        ServerPlayer player = context.player();
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof AppraisalTool appraisalTool)
                || !appraisalTool.supportsChainedAppraisal()) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        int processed = 0;
        for (BlockPos pos : positions) {
            if (!level.mayInteract(player, pos)) {
                continue;
            }
            ItemStack tool = player.getItemInHand(hand);
            if (tool.isEmpty()) {
                break;
            }
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), context.face(), pos, false);
            if (tool.useOn(new UseOnContext(player, hand, hit)).consumesAction()) {
                processed++;
            }
        }
        return processed;
    }
}
