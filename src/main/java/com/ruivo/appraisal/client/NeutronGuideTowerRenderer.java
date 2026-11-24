package com.ruivo.appraisal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ruivo.appraisal.machine.NeutronGuideTowerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 中子引导塔的四角展示。每个角对应一个槽位，槽位有物品时在角上方悬浮并缓慢旋转。
 */
public class NeutronGuideTowerRenderer implements BlockEntityRenderer<NeutronGuideTowerBlockEntity> {

    private static final double[][] CORNERS = {
            {0.25D, 0.25D},
            {0.75D, 0.25D},
            {0.25D, 0.75D},
            {0.75D, 0.75D}
    };

    public NeutronGuideTowerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NeutronGuideTowerBlockEntity tower, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (tower.getLevel() == null) {
            return;
        }
        float time = (tower.getLevel().getGameTime() + partialTick) / 20.0F;
        for (int slot = 0; slot < NeutronGuideTowerBlockEntity.SLOTS; slot++) {
            ItemStack stack = tower.getInventory().getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            pose.pushPose();
            pose.translate(CORNERS[slot][0],
                    0.95D + Math.sin(time * 1.4F + slot) * 0.045D,
                    CORNERS[slot][1]);
            pose.mulPose(Axis.YP.rotationDegrees((time * 45.0F + slot * 90.0F) % 360.0F));
            pose.scale(0.35F, 0.35F, 0.35F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                    packedLight, packedOverlay, pose, buffers, null, 0);
            pose.popPose();
        }
    }
}
