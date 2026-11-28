package com.ruivo.appraisal.registry;

import com.mojang.serialization.Codec;
import com.ruivo.appraisal.AppraisalMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AppraisalMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_EXPERIENCE =
            DATA_COMPONENTS.register("stored_experience", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SCANNER_ENERGY =
            DATA_COMPONENTS.register("scanner_energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> TOOL_PLUGINS =
            DATA_COMPONENTS.register("tool_plugins", () -> DataComponentType.<List<ItemStack>>builder()
                    .persistent(ItemStack.OPTIONAL_CODEC.listOf())
                    .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> SCANNER_CAPACITY_MULTIPLIER =
            DATA_COMPONENTS.register("scanner_capacity_multiplier", () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

    private ModDataComponents() {
    }

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
