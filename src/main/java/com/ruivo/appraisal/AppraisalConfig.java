package com.ruivo.appraisal;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AppraisalConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue DURABILITY_COST;
    public static final ModConfigSpec.BooleanValue ALLOW_BLOCK_ENTITIES;
    public static final ModConfigSpec.IntValue MACHINE_LEVEL;
    public static final ModConfigSpec.IntValue MACHINE_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue MACHINE_ENERGY_TRANSFER;
    public static final ModConfigSpec.IntValue MODEL_TRAINER_LEVEL;
    public static final ModConfigSpec.IntValue MODEL_TRAINER_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue MODEL_TRAINER_ENERGY_TRANSFER;
    public static final ModConfigSpec.IntValue MODEL_TRAINER_TRAINING_TIME;
    public static final ModConfigSpec.IntValue DECAY_GENERATOR_CAPACITY;
    public static final ModConfigSpec.IntValue DECAY_GENERATOR_TRANSFER;
    public static final ModConfigSpec.IntValue DECAY_GENERATOR_GENERATION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DURABILITY_COST = builder
                .comment("每次成功鉴定消耗的放大镜耐久点数")
                .translation("appraisal.configuration.durabilityCost")
                .defineInRange("durabilityCost", 1, 0, 64);

        ALLOW_BLOCK_ENTITIES = builder
                .comment("是否允许鉴定带方块实体的方块，例如箱子与熔炉")
                .translation("appraisal.configuration.allowBlockEntities")
                .define("allowBlockEntities", false);

        MACHINE_LEVEL = builder
                .comment("鉴定机的默认鉴定等级，机器等级与插件等级相加后生效")
                .translation("appraisal.configuration.machineLevel")
                .defineInRange("machineLevel", 2, 0, 1024);

        MACHINE_ENERGY_CAPACITY = builder
                .comment("鉴定机的基础能量容量，单位 FE。电容插件按倍率放大这个值")
                .translation("appraisal.configuration.machineEnergyCapacity")
                .defineInRange("machineEnergyCapacity", 10_000, 1_000, 10_000_000);

        MACHINE_ENERGY_TRANSFER = builder
                .comment("鉴定机的基础能量传输速度，单位 FE/t。电容插件按倍率放大这个值")
                .translation("appraisal.configuration.machineEnergyTransfer")
                .defineInRange("machineEnergyTransfer", 200, 10, 1_000_000);

        MODEL_TRAINER_LEVEL = builder
                .comment("模型训练机的默认鉴定等级，机器等级与插件等级相加后生效")
                .translation("appraisal.configuration.modelTrainerLevel")
                .defineInRange("modelTrainerLevel", 2, 0, 1024);

        MODEL_TRAINER_ENERGY_CAPACITY = builder
                .comment("模型训练机的基础能量容量，单位 FE。电容插件按倍率放大这个值")
                .translation("appraisal.configuration.modelTrainerEnergyCapacity")
                .defineInRange("modelTrainerEnergyCapacity", 10_000, 1_000, 10_000_000);

        MODEL_TRAINER_ENERGY_TRANSFER = builder
                .comment("模型训练机的基础能量传输速度，单位 FE/t。电容插件按倍率放大这个值")
                .translation("appraisal.configuration.modelTrainerEnergyTransfer")
                .defineInRange("modelTrainerEnergyTransfer", 200, 10, 1_000_000);

        MODEL_TRAINER_TRAINING_TIME = builder
                .comment("模型训练机每次训练的基础时长，单位游戏刻。速度插件按倍率缩短这个值")
                .translation("appraisal.configuration.modelTrainerTrainingTime")
                .defineInRange("modelTrainerTrainingTime", 6_000, 20, 1_000_000);

        DECAY_GENERATOR_CAPACITY = builder
                .comment("衰变发电机的基础能量容量，单位 FE。电容插件按倍率放大这个值")
                .translation("appraisal.configuration.decayGeneratorCapacity")
                .defineInRange("decayGeneratorCapacity", 100_000, 1_000, 10_000_000);

        DECAY_GENERATOR_TRANSFER = builder
                .comment("衰变发电机的基础能量传输速度，单位 FE/t。电容插件按倍率放大这个值")
                .translation("appraisal.configuration.decayGeneratorTransfer")
                .defineInRange("decayGeneratorTransfer", 400, 10, 1_000_000);

        DECAY_GENERATOR_GENERATION = builder
                .comment("衰变发电机的基础发电量，单位 FE/t。产能插件按倍率放大这个值")
                .translation("appraisal.configuration.decayGeneratorGeneration")
                .defineInRange("decayGeneratorGeneration", 200, 0, 1_000_000);

        SPEC = builder.build();
    }

    private AppraisalConfig() {
    }
}
