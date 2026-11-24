package com.ruivo.appraisal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ruivo.appraisal.machine.DecayGeneratorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 衰变发电机的悬浮展示。中心画输入物或产物，四个角画插件。
 */
public class DecayGeneratorRenderer implements BlockEntityRenderer<DecayGeneratorBlockEntity> {

    private static final double[][] CORNERS = {
            {0.25D, 0.25D},
            {0.75D, 0.25D},
            {0.25D, 0.75D},
            {0.75D, 0.75D}
    };

    public DecayGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DecayGeneratorBlockEntity generator, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (generator.getLevel() == null) {
            return;
        }
        float time = (generator.getLevel().getGameTime() + partialTick) / 20.0F;

        ItemStack center = generator.getInventory().getStackInSlot(DecayGeneratorBlockEntity.SLOT_CENTER);
        if (!center.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5D, 1.075D + Math.sin(time * 1.4F) * 0.045D, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees((time * 45.0F) % 360.0F));
            pose.scale(0.4F, 0.4F, 0.4F);
            Minecraft.getInstance().getItemRenderer().renderStatic(center, ItemDisplayContext.FIXED,
                    packedLight, packedOverlay, pose, buffers, null, 0);
            pose.popPose();
        }

        for (int index = 0; index < DecayGeneratorBlockEntity.PLUGIN_COUNT; index++) {
            ItemStack plugin = generator.getInventory()
                    .getStackInSlot(DecayGeneratorBlockEntity.PLUGIN_START + index);
            if (plugin.isEmpty()) {
                continue;
            }
            pose.pushPose();
            pose.translate(CORNERS[index][0],
                    0.88D + Math.sin(time * 1.4F + index) * 0.03D,
                    CORNERS[index][1]);
            pose.mulPose(Axis.YP.rotationDegrees((time * 40.0F + index * 90.0F) % 360.0F));
            pose.scale(0.28F, 0.28F, 0.28F);
            Minecraft.getInstance().getItemRenderer().renderStatic(plugin, ItemDisplayContext.FIXED,
                    packedLight, packedOverlay, pose, buffers, null, 0);
            pose.popPose();
        }
    }
}
