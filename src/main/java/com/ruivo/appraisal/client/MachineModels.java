package com.ruivo.appraisal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * 机器方块模型的统一渲染。使用不剔除背面的渲染类型，
 * 让框架薄壁的内侧也能显示纹理。
 */
public final class MachineModels {

    private MachineModels() {
    }

    public static void render(BlockEntity blockEntity, PoseStack pose,
                              MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockEntity.getBlockState(), pose, buffers, packedLight, packedOverlay,
                ModelData.EMPTY, RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
    }
}
