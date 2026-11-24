package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import com.ruivo.appraisal.machine.AppraisalMachineBlock;
import com.ruivo.appraisal.machine.DecayGeneratorBlock;
import com.ruivo.appraisal.machine.ModelTrainerBlock;
import com.ruivo.appraisal.machine.NeutronGuideTowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AppraisalMod.MOD_ID);

    public static final DeferredBlock<AppraisalMachineBlock> APPRAISAL_MACHINE = BLOCKS.register(
            "appraisal_machine",
            () -> new AppraisalMachineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<NeutronGuideTowerBlock> NEUTRON_GUIDE_TOWER = BLOCKS.register(
            "neutron_guide_tower",
            () -> new NeutronGuideTowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<DecayGeneratorBlock> DECAY_GENERATOR = BLOCKS.register(
            "decay_generator",
            () -> new DecayGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<ModelTrainerBlock> MODEL_TRAINER = BLOCKS.register(
            "model_trainer",
            () -> new ModelTrainerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    private ModBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
