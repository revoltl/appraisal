package com.ruivo.appraisal.client;

import com.ruivo.appraisal.registry.ModBlockEntities;
import com.ruivo.appraisal.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 客户端专用注册。只有在 Dist.CLIENT 下才会被加载。
 */
public final class ClientSetup {

    private ClientSetup() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientSetup::registerGuiLayers);
        modBus.addListener(ClientSetup::registerScreens);
        modBus.addListener(ClientSetup::registerBlockEntityRenderers);
        NeoForge.EVENT_BUS.addListener(AppraisalOverlay::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(AppraisalOverlay::onScroll);
        NeoForge.EVENT_BUS.addListener(AppraisalToolTooltip::onTooltip);
    }

    private static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.APPRAISAL_MACHINE.get(),
                AppraisalMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NEUTRON_GUIDE_TOWER.get(),
                NeutronGuideTowerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DECAY_GENERATOR.get(),
                DecayGeneratorRenderer::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath("appraisal", "appraisal_info"),
                AppraisalOverlay::render);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.APPRAISAL_MACHINE.get(), AppraisalMachineScreen::new);
        event.register(ModMenus.BLOCK_DATA_CARD.get(), BlockDataCardScreen::new);
        event.register(ModMenus.MODEL_TRAINER.get(), ModelTrainerScreen::new);
        event.register(ModMenus.TOOL_PLUGINS.get(), ToolPluginScreen::new);
    }
}
