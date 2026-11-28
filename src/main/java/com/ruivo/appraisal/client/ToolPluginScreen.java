package com.ruivo.appraisal.client;

import com.ruivo.appraisal.machine.ToolPluginMenu;
import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.plugin.PluginStat;
import com.ruivo.appraisal.plugin.PluginTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 工具插件配置面板，复用鉴定机的插件页背景。
 */
public class ToolPluginScreen extends AbstractContainerScreen<ToolPluginMenu> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/machine_plugin.png");

    private static final Set<PluginStat> TOOL_STATS = Set.of(
            PluginStat.APPRAISAL_LEVEL,
            PluginStat.ENERGY_CONSUMPTION,
            PluginStat.CAPACITY,
            PluginStat.GENERATOR_EFFICIENCY,
            PluginStat.SOLAR_GENERATION);

    public ToolPluginScreen(ToolPluginMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEX, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        PluginStatPanel.render(graphics, font, menu.getPluginStats(), TOOL_STATS, leftPos, topPos);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            if (stack.getItem() instanceof PluginItem plugin) {
                List<Component> lines = new ArrayList<>(getTooltipFromContainerItem(stack));
                for (Component line : PluginTooltip.lines(plugin.spec())) {
                    if (!lines.contains(line)) {
                        lines.add(line);
                    }
                }
                graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
