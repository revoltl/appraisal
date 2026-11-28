package com.ruivo.appraisal.client;

import com.ruivo.appraisal.machine.AppraisalMachineMenu;
import com.ruivo.appraisal.plugin.PluginStat;
import com.ruivo.appraisal.plugin.PluginStats;
import com.ruivo.appraisal.plugin.PluginTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Locale;

/**
 * 鉴定机界面。四个页面共用一个菜单，页面切换通过菜单按钮完成。
 * 贴图尚未绘制，底板与进度条先用代码绘制。
 */
public class AppraisalMachineScreen extends AbstractContainerScreen<AppraisalMachineMenu> {

    private static final ResourceLocation ARROW_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/arrow_back.png");
    private static final ResourceLocation ARROW_FULL =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/arrow_full.png");
    private static final ResourceLocation ENERGY_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/energy_back.png");
    private static final ResourceLocation ENERGY_FULL =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/energy_full.png");
    private static final ResourceLocation COOLANT_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/coolant_back.png");
    private static final ResourceLocation COOLANT_FULL =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/coolant_full.png");

    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/widgets.png");
    private static final ResourceLocation BUTTON_BASE_18 =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_base_18.png");
    private static final ResourceLocation BUTTON_ICON_STORAGE =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_storage_icon.png");
    private static final ResourceLocation BUTTON_BASE_9 =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_base_9.png");
    private static final ResourceLocation BUTTON_ICON_PLUGIN =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_plugin_icon.png");
    private static final ResourceLocation BUTTON_ICON_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_back_icon.png");
    private static final ResourceLocation BUTTON_ICON_PREV =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_prev_icon.png");
    private static final ResourceLocation BUTTON_ICON_NEXT =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_next_icon.png");

    private static final ResourceLocation TEX_MAIN =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/machine_main.png");
    private static final ResourceLocation TEX_STORAGE =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/machine_storage.png");
    private static final ResourceLocation TEX_CONFIG =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/machine_config.png");
    private static final ResourceLocation TEX_PLUGIN =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/machine_plugin.png");

    private static final int STORAGE_BUTTON_X = 105;
    private static final int STORAGE_BUTTON_Y = 32;
    private static final int NAV_BACK_X = 145;
    private static final int NAV_NEXT_X = 159;
    private static final int NAV_Y = 4;

    private static final int ENERGY_X = 7;
    private static final int ENERGY_Y = 14;
    private static final int BAR_WIDTH = 10;
    private static final int BAR_HEIGHT = 52;
    private static final int COOLANT_X = 20;
    private static final int ARROW_X = 76;
    private static final int ARROW_Y = 33;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    private static final int BAR_WIDTH_ACTUAL = 11;
    private static final int BAR_HEIGHT_ACTUAL = 58;

    public AppraisalMachineScreen(AppraisalMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;

        ResourceLocation texture = switch (menu.getPage()) {
            case AppraisalMachineMenu.PAGE_STORAGE -> TEX_STORAGE;
            case AppraisalMachineMenu.PAGE_PLUGIN -> TEX_PLUGIN;
            default -> TEX_MAIN;
        };
        graphics.blit(texture, left, top, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        switch (menu.getPage()) {
            case AppraisalMachineMenu.PAGE_MAIN -> {
                drawIconButton(graphics, left + 105, top + 32, 18, 18, BUTTON_BASE_18, BUTTON_ICON_STORAGE, mouseX, mouseY);
                drawIconButton(graphics, left + 159, top + 4, 9, 9, BUTTON_BASE_9, BUTTON_ICON_PLUGIN, mouseX, mouseY);
                renderMain(graphics, left, top);
            }
            case AppraisalMachineMenu.PAGE_STORAGE -> {
                drawIconButton(graphics, left + 159, top + 4, 9, 9, BUTTON_BASE_9, BUTTON_ICON_BACK, mouseX, mouseY);
                drawIconButton(graphics, left + 148, top + 72, 9, 9, BUTTON_BASE_9, BUTTON_ICON_PREV, mouseX, mouseY);
                drawIconButton(graphics, left + 159, top + 72, 9, 9, BUTTON_BASE_9, BUTTON_ICON_NEXT, mouseX, mouseY);
                renderStorage(graphics, left, top);
            }
            case AppraisalMachineMenu.PAGE_PLUGIN -> {
                drawIconButton(graphics, left + 159, top + 4, 9, 9, BUTTON_BASE_9, BUTTON_ICON_BACK, mouseX, mouseY);
                renderPluginStats(graphics, left, top);
            }
            default -> {
            }
        }
    }

    private void drawIconButton(GuiGraphics graphics, int x, int y, int width, int height,
                                ResourceLocation base, ResourceLocation icon, int mouseX, int mouseY) {
        graphics.blit(base, x, y, 0, 0, width, height, width, height);
        if (icon != null) {
            graphics.blit(icon, x, y, 0, 0, width, height, width, height);
        }
        boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        if (hovered) {
            graphics.fill(x, y, x + width, y + height, 0x40FFFFFF);
        }
    }

    private void renderMain(GuiGraphics graphics, int left, int top) {
        graphics.blit(ENERGY_BACK, left + ENERGY_X, top + ENERGY_Y, 0, 0, BAR_WIDTH_ACTUAL, BAR_HEIGHT_ACTUAL, BAR_WIDTH_ACTUAL, BAR_HEIGHT_ACTUAL);
        int energy = menu.getData(AppraisalMachineMenu.DATA_ENERGY);
        int energyMax = Math.max(1, menu.getData(AppraisalMachineMenu.DATA_ENERGY_MAX));
        int energyHeight = BAR_HEIGHT_ACTUAL * energy / energyMax;
        if (energyHeight > 0) {
            graphics.blit(ENERGY_FULL, left + ENERGY_X, top + ENERGY_Y + BAR_HEIGHT_ACTUAL - energyHeight,
                    0, BAR_HEIGHT_ACTUAL - energyHeight, BAR_WIDTH_ACTUAL, energyHeight, BAR_WIDTH_ACTUAL, BAR_HEIGHT_ACTUAL);
        }

        graphics.blit(COOLANT_BACK, left + COOLANT_X, top + ENERGY_Y, 0, 0, BAR_WIDTH_ACTUAL, BAR_HEIGHT_ACTUAL, BAR_WIDTH_ACTUAL, BAR_HEIGHT_ACTUAL);
        int coolant = menu.getData(AppraisalMachineMenu.DATA_COOLANT);
        if (coolant > 0) {
            int coolantMax = Math.max(1, menu.getData(AppraisalMachineMenu.DATA_COOLANT_MAX));
            int coolantHeight = BAR_HEIGHT_ACTUAL * coolant / coolantMax;
            graphics.blit(COOLANT_FULL, left + COOLANT_X, top + ENERGY_Y + BAR_HEIGHT_ACTUAL - coolantHeight,
                    0, BAR_HEIGHT_ACTUAL - coolantHeight, BAR_WIDTH_ACTUAL, coolantHeight, BAR_WIDTH_ACTUAL, BAR_HEIGHT_ACTUAL);
        }

        int required = menu.getData(AppraisalMachineMenu.DATA_REQUIRED_LEVEL);
        int levelValue = menu.getData(AppraisalMachineMenu.DATA_LEVEL);
        int pluginBonus = menu.getPluginStats().levelBonus();
        int levelColor = LevelPalette.color(levelValue);
        Component levelText = Component.translatable("gui.appraisal.machine_level.prefix")
                .append(Component.translatable("gui.appraisal.machine_level.value",
                        Component.literal(String.valueOf(levelValue))
                                .withStyle(style -> style.withColor(levelColor))))
                .append(Component.translatable("gui.appraisal.machine_level.bonus",
                                Component.literal("+" + pluginBonus)
                                        .withStyle(style -> style.withColor(levelColor)))
                        .withStyle(style -> style.withColor(levelColor)));
        graphics.drawString(font, levelText, left + 89 - font.width(levelText) / 2, top + 18,
                0x404040, false);
        if (required > levelValue) {
            graphics.drawString(font, Component.translatable("gui.appraisal.level_too_low", required),
                    left + 52, top + 56, 0xFF5555, false);
        }

        graphics.blit(ARROW_BACK, left + ARROW_X, top + ARROW_Y, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
        int progress = menu.getData(AppraisalMachineMenu.DATA_PROGRESS);
        int total = Math.max(1, menu.getData(AppraisalMachineMenu.DATA_PROGRESS_TOTAL));
        int fillWidth = ARROW_WIDTH * progress / total;
        if (fillWidth > 0) {
            graphics.blit(ARROW_FULL, left + ARROW_X, top + ARROW_Y, 0, 0, fillWidth, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
        }
    }

    private void renderStorage(GuiGraphics graphics, int left, int top) {
        // 分页后不再需要滚动条，页码由翻页按钮控制。
    }

    /**
     * 插件页的属性面板。九宫格左侧放能量三项，右侧放鉴定六项。
     * 左栏右对齐、右栏左对齐，两栏到插件区中线的距离相等，也避开左上角的标题。
     */
    private void renderPluginStats(GuiGraphics graphics, int left, int top) {
        PluginStats stats = menu.getPluginStats();
        int leftAnchor = left + 60;
        int rightX = left + 118;
        int leftY = top + 16;
        int rightY = top + 16;
        if (!stats.hasAny()) {
            graphics.drawString(font, Component.translatable("gui.appraisal.plugin.none"),
                    rightX, rightY, 0x808080, false);
            return;
        }

        for (PluginStat stat : PluginStat.values()) {
            String value = statValue(stats, stat);
            if (value == null) {
                continue;
            }
            boolean leftGroup = stat.group() == PluginStat.Group.LEFT;
            Component line = Component.translatable("gui.appraisal.plugin.stat.line",
                    Component.translatable(stat.labelKey()), value);
            int x = leftGroup ? leftAnchor - font.width(line) : rightX;
            int y = leftGroup ? leftY : rightY;
            graphics.drawString(font, line, x, y, statColor(stats, stat), false);
            if (leftGroup) {
                leftY += 9;
            } else {
                rightY += 9;
            }
        }
    }

    private static String statValue(PluginStats stats, PluginStat stat) {
        return switch (stat) {
            case APPRAISAL_LEVEL -> stats.levelBonus() == 0
                    ? null
                    : String.format(Locale.ROOT, "%+d", stats.levelBonus());
            case APPRAISAL_SPEED -> stats.appraisalSpeed() == 0.0D
                    ? null
                    : percent(stats.appraisalSpeed(), true);
            case PRODUCT_CHANCE -> stats.productChance() == 0.0D
                    ? null
                    : percent(stats.productChance(), true);
            case BLOCK_CONSUMPTION -> stats.blockConsumption() == 0.0D
                    ? null
                    : percent(stats.blockConsumeChance(), false);
            case ENERGY_CONSUMPTION -> stats.energyConsumption() == 0.0D
                    ? null
                    : percent(stats.energyConsumption(), true);
            case CAPACITY -> stats.capacity() == 0.0D
                    ? null
                    : percent(stats.capacityMultiplier() - 1.0D, true);
            case TRANSFER -> stats.transfer() == 0.0D
                    ? null
                    : percent(stats.transferMultiplier() - 1.0D, true);
            case COOLANT_CONSUMPTION -> stats.coolantConsumption() == 0.0D
                    ? null
                    : percent(stats.coolantFraction(), false);
            case GENERATOR_EFFICIENCY -> stats.generatorEfficiency() == 0.0D
                    ? null
                    : percent(stats.generatorEfficiency(), true);
            case SOLAR_GENERATION -> stats.solarGeneration() == 0.0D
                    ? null
                    : String.format(Locale.ROOT, "%+.0f FE/t", stats.solarGeneration());
            case VOID -> stats.voidEnabled()
                    ? Component.translatable("gui.appraisal.plugin.void_on").getString()
                    : null;
        };
    }

    private static String percent(double value, boolean signed) {
        return String.format(Locale.ROOT, signed ? "%+.0f%%" : "%.0f%%", value * 100.0D);
    }

    private static int statColor(PluginStats stats, PluginStat stat) {
        return switch (stat) {
            case APPRAISAL_LEVEL -> stats.levelBonus() > 0
                    ? PluginTooltip.GOOD_COLOR
                    : stats.levelBonus() < 0 ? PluginTooltip.BAD_COLOR : 0x808080;
            case APPRAISAL_SPEED -> stats.appraisalSpeed() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case PRODUCT_CHANCE -> stats.productChance() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case BLOCK_CONSUMPTION -> stats.blockConsumption() < 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case ENERGY_CONSUMPTION -> stats.energyConsumption() < 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case CAPACITY -> stats.capacity() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case TRANSFER -> stats.transfer() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case COOLANT_CONSUMPTION -> stats.coolantConsumption() > 0
                    ? PluginTooltip.BAD_COLOR : PluginTooltip.GOOD_COLOR;
            case GENERATOR_EFFICIENCY -> stats.generatorEfficiency() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case SOLAR_GENERATION -> stats.solarGeneration() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case VOID -> PluginTooltip.GOOD_COLOR;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = leftPos;
            int top = topPos;
            int page = menu.getPage();
            if (page == AppraisalMachineMenu.PAGE_MAIN) {
                if (inside(mouseX, mouseY, left + STORAGE_BUTTON_X, top + STORAGE_BUTTON_Y, 18, 18)) {
                    sendButton(AppraisalMachineMenu.PAGE_STORAGE);
                    return true;
                }
                if (inside(mouseX, mouseY, left + NAV_NEXT_X, top + NAV_Y, 10, 10)) {
                    sendButton(AppraisalMachineMenu.PAGE_PLUGIN);
                    return true;
                }
            } else {
                if (inside(mouseX, mouseY, left + NAV_NEXT_X, top + NAV_Y, 10, 10)) {
                    sendButton(AppraisalMachineMenu.PAGE_MAIN);
                    return true;
                }
            }
            if (menu.getPage() == AppraisalMachineMenu.PAGE_STORAGE) {
                if (inside(mouseX, mouseY, left + 148, top + 72, 9, 9)) {
                    playClick();
                    packetPage(menu.getData(AppraisalMachineMenu.DATA_STORAGE_PAGE) - 1);
                    return true;
                }
                if (inside(mouseX, mouseY, left + 159, top + 72, 9, 9)) {
                    playClick();
                    packetPage(menu.getData(AppraisalMachineMenu.DATA_STORAGE_PAGE) + 1);
                    return true;
                }
            }
        }
        // 存储槽位的点击交给原版槽位处理，取出统一走容器扣减。
        return super.mouseClicked(mouseX, mouseY, button);
    }




    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.getPage() == AppraisalMachineMenu.PAGE_STORAGE && scrollY != 0.0D) {
            int current = menu.getData(AppraisalMachineMenu.DATA_STORAGE_PAGE);
            packetPage(current + (scrollY > 0.0D ? -1 : 1));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void sendButton(int id) {
        playClick();
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void packetPage(int page) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new com.ruivo.appraisal.network.StorageScrollPayload(page));
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }


    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        List<Component> lines = customTooltip(mouseX, mouseY);
        if (!lines.isEmpty()) {
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }

    private List<Component> customTooltip(int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        int page = menu.getPage();
        if (page == AppraisalMachineMenu.PAGE_MAIN) {
            if (inside(mouseX, mouseY, left + 7, top + 13, 11, 58)) {
                return List.of(
                        Component.translatable("gui.appraisal.energy",
                                menu.getData(AppraisalMachineMenu.DATA_ENERGY),
                                menu.getData(AppraisalMachineMenu.DATA_ENERGY_MAX)),
                        Component.translatable("gui.appraisal.energy.transfer",
                                menu.getMachine().getEnergyTransfer()));
            }
            if (inside(mouseX, mouseY, left + 20, top + 13, 11, 58)) {
                return List.of(Component.translatable("gui.appraisal.coolant",
                        menu.getData(AppraisalMachineMenu.DATA_COOLANT),
                        menu.getData(AppraisalMachineMenu.DATA_COOLANT_MAX)));
            }
            if (inside(mouseX, mouseY, left + ARROW_X, top + ARROW_Y,
                    ARROW_WIDTH, ARROW_HEIGHT)) {
                int progress = menu.getData(AppraisalMachineMenu.DATA_PROGRESS);
                int total = Math.max(1, menu.getData(AppraisalMachineMenu.DATA_PROGRESS_TOTAL));
                return List.of(Component.translatable("gui.appraisal.progress",
                        (progress * 100 / total) + "%"));
            }
            if (inside(mouseX, mouseY, left + 105, top + 32, 18, 18)) {
                return List.of(Component.translatable("gui.appraisal.storage"));
            }
            if (inside(mouseX, mouseY, left + 159, top + 4, 10, 10)) {
                return List.of(Component.translatable("gui.appraisal.plugin"));
            }
        }
        if (menu.getPage() == AppraisalMachineMenu.PAGE_STORAGE) {
            if (inside(mouseX, mouseY, left + 148, top + 72, 9, 9)) {
                return List.of(Component.translatable("gui.appraisal.prev_page"));
            }
            if (inside(mouseX, mouseY, left + 159, top + 72, 9, 9)) {
                return List.of(Component.translatable("gui.appraisal.next_page"));
            }
        } else if (inside(mouseX, mouseY, left + 159, top + 4, 10, 10)) {
            return List.of(Component.translatable("gui.appraisal.back"));
        }
        return List.of();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
