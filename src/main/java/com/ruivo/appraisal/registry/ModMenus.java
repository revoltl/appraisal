package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import com.ruivo.appraisal.machine.AppraisalMachineMenu;
import com.ruivo.appraisal.machine.BlockDataCardMenu;
import com.ruivo.appraisal.machine.ModelTrainerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AppraisalMod.MOD_ID);

    public static final Supplier<MenuType<AppraisalMachineMenu>> APPRAISAL_MACHINE =
            MENUS.register("appraisal_machine",
                    () -> IMenuTypeExtension.create(AppraisalMachineMenu::new));

    public static final Supplier<MenuType<BlockDataCardMenu>> BLOCK_DATA_CARD =
            MENUS.register("block_data_card",
                    () -> IMenuTypeExtension.create(BlockDataCardMenu::new));

    public static final Supplier<MenuType<ModelTrainerMenu>> MODEL_TRAINER =
            MENUS.register("model_trainer",
                    () -> IMenuTypeExtension.create(ModelTrainerMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
