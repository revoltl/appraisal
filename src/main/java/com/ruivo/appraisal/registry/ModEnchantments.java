package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {

    public static final ResourceKey<Enchantment> LEEUWENHOEK = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "leeuwenhoek"));

    private ModEnchantments() {
    }
}
