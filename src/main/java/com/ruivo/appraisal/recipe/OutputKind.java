package com.ruivo.appraisal.recipe;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum OutputKind implements StringRepresentable {

    ITEM("item"),
    BLOCK("block");

    public static final Codec<OutputKind> CODEC = StringRepresentable.fromEnum(OutputKind::values);
    public static final StreamCodec<ByteBuf, OutputKind> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeByte(value.ordinal()),
            buf -> values()[buf.readUnsignedByte()]
    );

    private final String name;

    OutputKind(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
