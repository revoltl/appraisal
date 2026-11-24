package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import com.ruivo.appraisal.machine.AppraisalMachineBlockEntity;
import com.ruivo.appraisal.machine.DecayGeneratorBlockEntity;
import com.ruivo.appraisal.machine.ModelTrainerBlockEntity;
import com.ruivo.appraisal.machine.NeutronGuideTowerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AppraisalMod.MOD_ID);

    public static final Supplier<BlockEntityType<AppraisalMachineBlockEntity>> APPRAISAL_MACHINE =
            BLOCK_ENTITIES.register("appraisal_machine",
                    () -> BlockEntityType.Builder.of(AppraisalMachineBlockEntity::new,
                            ModBlocks.APPRAISAL_MACHINE.get()).build(null));

    public static final Supplier<BlockEntityType<NeutronGuideTowerBlockEntity>> NEUTRON_GUIDE_TOWER =
            BLOCK_ENTITIES.register("neutron_guide_tower",
                    () -> BlockEntityType.Builder.of(NeutronGuideTowerBlockEntity::new,
                            ModBlocks.NEUTRON_GUIDE_TOWER.get()).build(null));

    public static final Supplier<BlockEntityType<DecayGeneratorBlockEntity>> DECAY_GENERATOR =
            BLOCK_ENTITIES.register("decay_generator",
                    () -> BlockEntityType.Builder.of(DecayGeneratorBlockEntity::new,
                            ModBlocks.DECAY_GENERATOR.get()).build(null));

    public static final Supplier<BlockEntityType<ModelTrainerBlockEntity>> MODEL_TRAINER =
            BLOCK_ENTITIES.register("model_trainer",
                    () -> BlockEntityType.Builder.of(ModelTrainerBlockEntity::new,
                            ModBlocks.MODEL_TRAINER.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
