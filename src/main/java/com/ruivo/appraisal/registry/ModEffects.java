package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import com.ruivo.appraisal.effect.MendeleevInsightEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, AppraisalMod.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> MENDELEEV_INSIGHT =
            EFFECTS.register("mendeleev_insight", MendeleevInsightEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
