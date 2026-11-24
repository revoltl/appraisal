package com.ruivo.appraisal.registry;

import com.mojang.serialization.Codec;
import com.ruivo.appraisal.AppraisalMod;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 玩家身上的持久数据。鉴定等级是玩家的基础等级，不含工具、附魔与药水的加成。
 */
public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AppraisalMod.MOD_ID);

    public static final Supplier<AttachmentType<Integer>> APPRAISAL_LEVEL = ATTACHMENTS.register(
            "appraisal_level",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.VAR_INT.cast())
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
