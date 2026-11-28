package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 本模组使用的物品标签。
 *
 * <p>插件槽按 {@link Items#PLUGINS} 接收物品，新增插件只要把物品加进这个标签，
 * 菜单不需要再改动。</p>
 */
public final class ModTags {

    public static final class Items {

        public static final TagKey<Item> PLUGINS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "plugins"));

        public static final TagKey<Item> APPRAISAL_TOOLS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "appraisal_tools"));

        public static final TagKey<Item> NEUTRON_GUIDE_INPUTS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "neutron_guide_inputs"));

        public static final TagKey<Item> NEUTRON_GUIDE_OUTPUTS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "neutron_guide_outputs"));

        public static final TagKey<Item> MODEL_TRAINING_PLUGINS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "model_training_plugins"));

        private Items() {
        }
    }

    private ModTags() {
    }
}
