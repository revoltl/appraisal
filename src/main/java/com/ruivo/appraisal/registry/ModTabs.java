package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import com.ruivo.appraisal.item.ElectricScannerItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AppraisalMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> APPRAISAL =
            TABS.register("appraisal", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.appraisal"))
                    .icon(() -> new ItemStack(ModItems.INSIGHT_ORB.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MAGNIFYING_GLASS.get());
                        output.accept(ModItems.AMETHYST_MAGNIFYING_GLASS.get());
                        output.accept(ModItems.ELECTRIC_SCANNER.get());
                        ItemStack fullScanner = new ItemStack(ModItems.ELECTRIC_SCANNER.get());
                        ElectricScannerItem.setEnergy(fullScanner, ElectricScannerItem.capacity(fullScanner));
                        output.accept(fullScanner);
                        output.accept(ModItems.INSIGHT_ORB.get());
                        output.accept(ModItems.URANIUM_TITANIUM_THALLIUM.get());
                        output.accept(ModItems.PLUTONIUM_TITANIUM_THALLIUM.get());
                        output.accept(ModItems.EXPERIENCE_DATA_CARD.get());
                        output.accept(ModItems.BLOCK_DATA_CARD.get());
                        output.accept(ModItems.APPRAISAL_MACHINE.get());
                        output.accept(ModItems.NEUTRON_GUIDE_TOWER.get());
                        output.accept(ModItems.DECAY_GENERATOR.get());
                        output.accept(ModItems.MODEL_TRAINER.get());
                        output.accept(ModItems.BLANK_PLUGIN.get());
                        output.accept(ModItems.LEVEL_PLUGIN.get());
                        output.accept(ModItems.SPEED_PLUGIN.get());
                        output.accept(ModItems.PRODUCTIVITY_PLUGIN.get());
                        output.accept(ModItems.ENERGY_SAVING_PLUGIN.get());
                        output.accept(ModItems.CAPACITOR_PLUGIN.get());
                        output.accept(ModItems.PHOTOVOLTAIC_PLUGIN.get());
                        output.accept(ModItems.EFFICIENCY_PLUGIN.get());
                        output.accept(ModItems.COOLANT_PLUGIN.get());
                        output.accept(ModItems.VOID_PLUGIN.get());
                        output.accept(ModItems.STATISTICS_PLUGIN.get());
                        output.accept(ModItems.INTEGRATION_PLUGIN.get());
                        output.accept(ModItems.LAYER_FEATURE_PLUGIN.get());
                        output.accept(ModItems.ATTENTION_PLUGIN.get());
                    })
                    .build());

    private ModTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
