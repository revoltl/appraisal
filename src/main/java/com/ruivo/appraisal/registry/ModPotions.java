package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 门捷列夫药水走原版药水注册，物品是 minecraft:potion，通过药水组件区分种类。
 * 这样 JEI 的酿造分类可以正常读到。
 */
public final class ModPotions {

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, AppraisalMod.MOD_ID);

    public static final DeferredHolder<Potion, Potion> MENDELEEV = POTIONS.register(
            "mendeleev",
            () -> new Potion("mendeleev",
                    new MobEffectInstance(ModEffects.MENDELEEV_INSIGHT, 3600, 0)));

    public static final DeferredHolder<Potion, Potion> STRONG_MENDELEEV = POTIONS.register(
            "strong_mendeleev",
            () -> new Potion("strong_mendeleev",
                    new MobEffectInstance(ModEffects.MENDELEEV_INSIGHT, 1800, 1)));

    private ModPotions() {
    }

    public static void register(IEventBus modBus) {
        POTIONS.register(modBus);
    }
}
