package com.ruivo.appraisal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 只渲染方块模型的通用渲染器，用于没有额外动画的机器。
 */
public class MachineModelRenderer implements BlockEntityRenderer<BlockEntity> {

    public MachineModelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        MachineModels.render(blockEntity, pose, buffers, packedLight, packedOverlay);
    }
}
