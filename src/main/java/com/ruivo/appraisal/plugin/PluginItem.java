package com.ruivo.appraisal.plugin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 带参数的插件物品。机器读取插件槽时只认这个类型，再通过 {@link PluginSpec} 取参数。
 */
public class PluginItem extends Item {

    private final PluginSpec spec;
    private final ChatFormatting nameColor;

    public PluginItem(Properties properties, PluginSpec spec) {
        this(properties, spec, null);
    }

    public PluginItem(Properties properties, PluginSpec spec, ChatFormatting nameColor) {
        super(properties);
        this.spec = spec;
        this.nameColor = nameColor;
    }

    public PluginSpec spec() {
        return spec;
    }

    public int installLimit() {
        return spec.installLimit();
    }

    public ChatFormatting nameColor() {
        return nameColor;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        if (nameColor == null) {
            return name;
        }
        return name.copy().withStyle(nameColor);
    }
}
