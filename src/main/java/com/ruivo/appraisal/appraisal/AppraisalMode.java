package com.ruivo.appraisal.appraisal;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum AppraisalMode implements StringRepresentable {

    DIRECT("direct"),
    ADVANCED("advanced");

    public static final Codec<AppraisalMode> CODEC = StringRepresentable.fromEnum(AppraisalMode::values);
    public static final StreamCodec<ByteBuf, AppraisalMode> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeByte(value.ordinal()),
            buf -> values()[buf.readUnsignedByte()]
    );

    private final String name;

    AppraisalMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
