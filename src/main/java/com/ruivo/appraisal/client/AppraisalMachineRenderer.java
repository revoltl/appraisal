package com.ruivo.appraisal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ruivo.appraisal.machine.AppraisalMachineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 鉴定机的输入展示。读取输入槽，在样品台上方悬浮一个缓慢旋转的物品或方块。
 *
 * <p>做法参考 Actually Additions 的物品展示台：平移到方块中心，用正弦函数上下浮动，
 * 绕 Y 轴旋转，方块物品和普通物品用不同缩放，最后以 FIXED 上下文交给物品渲染器。</p>
 */
public class AppraisalMachineRenderer implements BlockEntityRenderer<AppraisalMachineBlockEntity> {

    private static final double BASE_HEIGHT = 0.95D;
    private static final double FLOAT_AMPLITUDE = 0.045D;
    private static final float ROTATION_SPEED = 45.0F;
    private static final float BLOCK_SCALE = 0.45F;
    private static final float ITEM_SCALE = 0.4F;

    public AppraisalMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AppraisalMachineBlockEntity blockEntity, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getInventory()
                .getStackInSlot(AppraisalMachineBlockEntity.INPUT_SLOT);
        if (stack.isEmpty()) {
            return;
        }

        float time = (Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime() + partialTick
                : 0.0F) / 20.0F;

        pose.pushPose();
        pose.translate(0.5D,
                BASE_HEIGHT + Math.sin(time * 1.5F) * FLOAT_AMPLITUDE,
                0.5D);
        pose.mulPose(Axis.YP.rotationDegrees((time * ROTATION_SPEED) % 360.0F));

        float scale = stack.getItem() instanceof BlockItem ? BLOCK_SCALE : ITEM_SCALE;
        pose.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                pose, buffers, null, 0);

        pose.popPose();
    }
}
