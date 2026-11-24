package com.ruivo.appraisal.network;

import com.ruivo.appraisal.machine.AppraisalMachineMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 存储页滚动到指定位置。客户端拖动滚动条或滚轮时发送绝对偏移。
 */
public record StorageScrollPayload(int scroll) implements CustomPacketPayload {

    public static final Type<StorageScrollPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("appraisal", "storage_scroll"));

    public static final StreamCodec<FriendlyByteBuf, StorageScrollPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, StorageScrollPayload::scroll,
                    StorageScrollPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StorageScrollPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof AppraisalMachineMenu menu) {
                menu.handleStoragePage(payload.scroll());
            }
        });
    }
}
