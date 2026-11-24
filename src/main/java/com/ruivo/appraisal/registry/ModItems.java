package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import com.ruivo.appraisal.item.AppraisalMachineItem;
import com.ruivo.appraisal.item.BlockDataCardItem;
import com.ruivo.appraisal.item.DecayGeneratorItem;
import com.ruivo.appraisal.item.ExperienceDataCardItem;
import com.ruivo.appraisal.item.ModelTrainerItem;
import com.ruivo.appraisal.item.NeutronGuideTowerItem;
import com.ruivo.appraisal.plugin.ModelFamily;
import com.ruivo.appraisal.plugin.ModelPluginItem;
import com.ruivo.appraisal.item.MagnifyingGlassItem;
import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.plugin.PluginSpec;
import com.ruivo.appraisal.plugin.PluginStat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppraisalMod.MOD_ID);

    public static final DeferredItem<MagnifyingGlassItem> MAGNIFYING_GLASS = ITEMS.register(
            "magnifying_glass",
            () -> new MagnifyingGlassItem(new Item.Properties().stacksTo(1).durability(256), 0)
    );

    public static final DeferredItem<MagnifyingGlassItem> AMETHYST_MAGNIFYING_GLASS = ITEMS.register(
            "amethyst_magnifying_glass",
            () -> new MagnifyingGlassItem(new Item.Properties().stacksTo(1).durability(2048), 1)
    );

    public static final DeferredItem<Item> INSIGHT_ORB = ITEMS.registerSimpleItem("insight_orb");

    public static final DeferredItem<Item> URANIUM_TITANIUM_THALLIUM = ITEMS.registerSimpleItem(
            "uranium_titanium_thallium");

    public static final DeferredItem<Item> PLUTONIUM_TITANIUM_THALLIUM = ITEMS.registerSimpleItem(
            "plutonium_titanium_thallium");

    public static final DeferredItem<Item> EXPERIENCE_DATA_CARD = ITEMS.register(
            "experience_data_card",
            () -> new ExperienceDataCardItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.STORED_EXPERIENCE.get(), 0)));

    public static final DeferredItem<BlockDataCardItem> BLOCK_DATA_CARD = ITEMS.register(
            "block_data_card",
            () -> new BlockDataCardItem(new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)));

    public static final DeferredItem<BlockItem> APPRAISAL_MACHINE = ITEMS.register(
            "appraisal_machine",
            () -> new AppraisalMachineItem(ModBlocks.APPRAISAL_MACHINE.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> NEUTRON_GUIDE_TOWER = ITEMS.register(
            "neutron_guide_tower",
            () -> new NeutronGuideTowerItem(ModBlocks.NEUTRON_GUIDE_TOWER.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> DECAY_GENERATOR = ITEMS.register(
            "decay_generator",
            () -> new DecayGeneratorItem(ModBlocks.DECAY_GENERATOR.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> MODEL_TRAINER = ITEMS.register(
            "model_trainer",
            () -> new ModelTrainerItem(ModBlocks.MODEL_TRAINER.get(), new Item.Properties())
    );

    public static final DeferredItem<PluginItem> BLANK_PLUGIN = ITEMS.register(
            "blank_plugin",
            () -> new PluginItem(new Item.Properties().stacksTo(64), PluginSpec.builder().build()));

    public static final DeferredItem<PluginItem> LEVEL_PLUGIN = registerPlugin(
            "level_plugin", PluginSpec.builder()
                    .put(PluginStat.APPRAISAL_LEVEL, 1.0D)
                    .put(PluginStat.ENERGY_CONSUMPTION, 1.0D)
                    .installLimit(4)
                    .build());

    public static final DeferredItem<PluginItem> SPEED_PLUGIN = registerPlugin(
            "speed_plugin", PluginSpec.builder()
                    .put(PluginStat.APPRAISAL_SPEED, 0.5D)
                    .put(PluginStat.ENERGY_CONSUMPTION, 0.7D)
                    .build());

    public static final DeferredItem<PluginItem> PRODUCTIVITY_PLUGIN = registerPlugin(
            "productivity_plugin", PluginSpec.builder()
                    .put(PluginStat.PRODUCT_CHANCE, 0.1D)
                    .put(PluginStat.BLOCK_CONSUMPTION, -0.05D)
                    .put(PluginStat.APPRAISAL_SPEED, -0.15D)
                    .put(PluginStat.ENERGY_CONSUMPTION, 0.8D)
                    .build());

    public static final DeferredItem<PluginItem> ENERGY_SAVING_PLUGIN = registerPlugin(
            "energy_saving_plugin", PluginSpec.builder()
                    .put(PluginStat.ENERGY_CONSUMPTION, -0.5D)
                    .build());

    public static final DeferredItem<PluginItem> CAPACITOR_PLUGIN = registerPlugin(
            "capacitor_plugin", PluginSpec.builder()
                    .put(PluginStat.CAPACITY, 0.5D)
                    .put(PluginStat.TRANSFER, 0.5D)
                    .put(PluginStat.GENERATOR_EFFICIENCY, 0.2D)
                    .build());

    public static final DeferredItem<PluginItem> COOLANT_PLUGIN = registerPlugin(
            "coolant_plugin", PluginSpec.builder()
                    .put(PluginStat.PRODUCT_CHANCE, 0.05D)
                    .put(PluginStat.APPRAISAL_SPEED, 0.05D)
                    .put(PluginStat.COOLANT_CONSUMPTION, 0.05D)
                    .build());

    public static final DeferredItem<PluginItem> VOID_PLUGIN = registerPlugin(
            "void_plugin", PluginSpec.builder()
                    .put(PluginStat.VOID, 1.0D)
                    .installLimit(1)
                    .build());

    public static final DeferredItem<PluginItem> STATISTICS_PLUGIN = ITEMS.register(
            "statistics_plugin",
            () -> new ModelPluginItem(new Item.Properties().stacksTo(1),
                    PluginSpec.builder()
                            .put(PluginStat.APPRAISAL_LEVEL, 1.0D)
                            .put(PluginStat.ENERGY_CONSUMPTION, 1.0D)
                            .installLimit(1)
                            .build(), null, ModelFamily.STATISTICS, 0.55D));

    public static final DeferredItem<PluginItem> INTEGRATION_PLUGIN = ITEMS.register(
            "integration_plugin",
            () -> new ModelPluginItem(new Item.Properties().stacksTo(1),
                    PluginSpec.builder()
                            .put(PluginStat.APPRAISAL_LEVEL, 1.0D)
                            .put(PluginStat.ENERGY_CONSUMPTION, 1.0D)
                            .installLimit(1)
                            .build(), ChatFormatting.BLUE, ModelFamily.INTEGRATION, 0.7D));

    public static final DeferredItem<PluginItem> LAYER_FEATURE_PLUGIN = ITEMS.register(
            "layer_feature_plugin",
            () -> new ModelPluginItem(new Item.Properties().stacksTo(1),
                    PluginSpec.builder()
                            .put(PluginStat.APPRAISAL_LEVEL, 1.0D)
                            .put(PluginStat.ENERGY_CONSUMPTION, 1.0D)
                            .installLimit(1)
                            .build(), ChatFormatting.GOLD, ModelFamily.LAYER_FEATURE, 0.85D));

    public static final DeferredItem<PluginItem> ATTENTION_PLUGIN = ITEMS.register(
            "attention_plugin",
            () -> new ModelPluginItem(new Item.Properties().stacksTo(1),
                    PluginSpec.builder()
                            .put(PluginStat.APPRAISAL_LEVEL, 1.0D)
                            .put(PluginStat.ENERGY_CONSUMPTION, 1.0D)
                            .installLimit(1)
                            .build(), ChatFormatting.LIGHT_PURPLE, ModelFamily.ATTENTION, 1.0D));

    private static DeferredItem<PluginItem> registerPlugin(String name, PluginSpec spec) {
        return ITEMS.register(name, () -> new PluginItem(new Item.Properties().stacksTo(1), spec));
    }

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
