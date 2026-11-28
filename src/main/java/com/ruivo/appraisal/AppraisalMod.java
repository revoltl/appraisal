package com.ruivo.appraisal;

import com.mojang.logging.LogUtils;
import com.ruivo.appraisal.brewing.MendeleevBrewing;
import com.ruivo.appraisal.client.ClientSetup;
import com.ruivo.appraisal.command.AppraisalCommands;
import com.ruivo.appraisal.compat.Integrations;
import com.ruivo.appraisal.compat.actuallyadditions.ActuallyAdditionsIntegration;
import com.ruivo.appraisal.compat.ae2.AppliedEnergisticsIntegration;
import com.ruivo.appraisal.compat.alltheores.AllTheOresIntegration;
import com.ruivo.appraisal.compat.create.CreateIntegration;
import com.ruivo.appraisal.compat.ftbultimine.FTBUltimineCompat;
import com.ruivo.appraisal.compat.immersiveengineering.ImmersiveEngineeringIntegration;
import com.ruivo.appraisal.compat.ironsspellbooks.IronsSpellbooksIntegration;
import com.ruivo.appraisal.compat.mekanism.MekanismIntegration;
import com.ruivo.appraisal.compat.modernindustrialization.ModernIndustrializationIntegration;
import com.ruivo.appraisal.compat.mysticalagriculture.MysticalAgricultureIntegration;
import com.ruivo.appraisal.compat.powah.PowahIntegration;
import com.ruivo.appraisal.compat.rftools.RFToolsIntegration;
import com.ruivo.appraisal.item.ScannerEnergyStorage;
import com.ruivo.appraisal.machine.AppraisalMachineBlockEntity;
import com.ruivo.appraisal.machine.DecayGeneratorBlockEntity;
import com.ruivo.appraisal.machine.ModelTrainerBlockEntity;
import com.ruivo.appraisal.machine.NeutronGuideTowerBlockEntity;
import com.ruivo.appraisal.plugin.PluginTooltipHandler;
import com.ruivo.appraisal.registry.ModAttachments;
import com.ruivo.appraisal.registry.ModBlockEntities;
import com.ruivo.appraisal.registry.ModBlocks;
import com.ruivo.appraisal.registry.ModDataComponents;
import com.ruivo.appraisal.registry.ModEffects;
import com.ruivo.appraisal.registry.ModItems;
import com.ruivo.appraisal.registry.ModMenus;
import com.ruivo.appraisal.registry.ModPotions;
import com.ruivo.appraisal.registry.ModRecipes;
import com.ruivo.appraisal.registry.ModTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(AppraisalMod.MOD_ID)
public final class AppraisalMod {

    public static final String MOD_ID = "appraisal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AppraisalMod(IEventBus modBus, ModContainer container) {
        modBus.addListener(AppraisalMachineBlockEntity::registerCapabilities);
        modBus.addListener(NeutronGuideTowerBlockEntity::registerCapabilities);
        modBus.addListener(DecayGeneratorBlockEntity::registerCapabilities);
        modBus.addListener(ModelTrainerBlockEntity::registerCapabilities);
        com.ruivo.appraisal.network.ModNetwork.register(modBus);
        ModDataComponents.register(modBus);
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        modBus.addListener(RegisterCapabilitiesEvent.class, event ->
                event.registerItem(Capabilities.EnergyStorage.ITEM,
                        (stack, context) -> new ScannerEnergyStorage(stack),
                        ModItems.ELECTRIC_SCANNER.get()));
        ModBlockEntities.register(modBus);
        ModMenus.register(modBus);
        ModRecipes.register(modBus);
        ModTabs.register(modBus);
        ModAttachments.register(modBus);
        ModEffects.register(modBus);
        ModPotions.register(modBus);

        NeoForge.EVENT_BUS.addListener(AppraisalCommands::register);
        NeoForge.EVENT_BUS.addListener(MendeleevBrewing::register);
        NeoForge.EVENT_BUS.addListener(PluginTooltipHandler::onTooltip);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.init(modBus);
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        if (ModList.get().isLoaded("alltheores")) {
            Integrations.register(new AllTheOresIntegration());
        }
        if (ModList.get().isLoaded("mekanism")) {
            // 两个模组的同种矿石共享 c:ores 标签，后注册的一方会被标签去重跳过。
            Integrations.register(new MekanismIntegration());
        }
        if (ModList.get().isLoaded("create")) {
            Integrations.register(new CreateIntegration());
        }
        if (ModList.get().isLoaded("actuallyadditions")) {
            Integrations.register(new ActuallyAdditionsIntegration());
        }
        if (ModList.get().isLoaded("ae2")) {
            Integrations.register(new AppliedEnergisticsIntegration());
        }
        if (ModList.get().isLoaded("immersiveengineering")) {
            Integrations.register(new ImmersiveEngineeringIntegration());
        }
        if (ModList.get().isLoaded("powah")) {
            Integrations.register(new PowahIntegration());
        }
        if (ModList.get().isLoaded("modern_industrialization")) {
            Integrations.register(new ModernIndustrializationIntegration());
        }
        if (ModList.get().isLoaded("rftoolsbase")) {
            Integrations.register(new RFToolsIntegration());
        }
        if (ModList.get().isLoaded("mysticalagriculture")) {
            Integrations.register(new MysticalAgricultureIntegration());
        }
        if (ModList.get().isLoaded("irons_spellbooks")) {
            Integrations.register(new IronsSpellbooksIntegration());
        }

        FTBUltimineCompat.init();

        container.registerConfig(ModConfig.Type.COMMON, AppraisalConfig.SPEC, "appraisal-common.toml");
    }
}
