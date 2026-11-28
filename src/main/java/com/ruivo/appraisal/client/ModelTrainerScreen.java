package com.ruivo.appraisal.client;

import com.ruivo.appraisal.machine.ModelTrainerBlockEntity;
import com.ruivo.appraisal.machine.ModelTrainerMenu;
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
 * 模型训练机界面。主页面放四个功能槽和开始按钮，插件页沿用鉴定机的九宫格布局。
 */
public class ModelTrainerScreen extends AbstractContainerScreen<ModelTrainerMenu> {

    private static final ResourceLocation TEX_MAIN =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/model_trainer_main.png");
    private static final ResourceLocation TEX_PLUGIN =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/machine_plugin.png");
    private static final ResourceLocation PROGRESS_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/train_progress_back.png");
    private static final ResourceLocation PROGRESS_FULL =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/train_progress_full.png");
    private static final ResourceLocation START_BUTTON =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/train_start_button.png");
    private static final ResourceLocation ENERGY_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/energy_back.png");
    private static final ResourceLocation ENERGY_FULL =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/energy_full.png");
    private static final ResourceLocation COOLANT_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/coolant_back.png");
    private static final ResourceLocation COOLANT_FULL =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/coolant_full.png");
    private static final ResourceLocation BUTTON_BASE_9 =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_base_9.png");
    private static final ResourceLocation BUTTON_ICON_PLUGIN =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_plugin_icon.png");
    private static final ResourceLocation BUTTON_ICON_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/button_back_icon.png");

    private static final int ENERGY_X = 7;
    private static final int COOLANT_X = 20;
    private static final int BAR_Y = 14;
    private static final int BAR_WIDTH = 11;
    private static final int BAR_HEIGHT = 58;
    private static final int PROGRESS_X = 62;
    private static final int PROGRESS_Y = 64;
    private static final int PROGRESS_WIDTH = 54;
    private static final int PROGRESS_HEIGHT = 5;
    private static final int START_X = 136;
    private static final int START_Y = 60;
    private static final int START_WIDTH = 32;
    private static final int START_HEIGHT = 18;
    private static final int PLUGIN_X = 159;
    private static final int PLUGIN_Y = 4;

    public ModelTrainerScreen(ModelTrainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;

        if (menu.getPage() == ModelTrainerMenu.PAGE_PLUGIN) {
            graphics.blit(TEX_PLUGIN, left, top, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
            drawIconButton(graphics, left + PLUGIN_X, top + PLUGIN_Y, 9, 9,
                    BUTTON_BASE_9, BUTTON_ICON_BACK, mouseX, mouseY);
            renderPluginStats(graphics, left, top);
            return;
        }

        graphics.blit(TEX_MAIN, left, top, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        renderBars(graphics, left, top);
        renderLevel(graphics, left, top);
        renderProgress(graphics, left, top);
        renderStartButton(graphics, left, top, mouseX, mouseY);
        drawIconButton(graphics, left + PLUGIN_X, top + PLUGIN_Y, 9, 9,
                BUTTON_BASE_9, BUTTON_ICON_PLUGIN, mouseX, mouseY);
    }

    private void renderBars(GuiGraphics graphics, int left, int top) {
        graphics.blit(ENERGY_BACK, left + ENERGY_X, top + BAR_Y, 0, 0,
                BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        int energy = menu.getData(ModelTrainerMenu.DATA_ENERGY);
        int energyMax = Math.max(1, menu.getData(ModelTrainerMenu.DATA_ENERGY_MAX));
        int energyHeight = BAR_HEIGHT * energy / energyMax;
        if (energyHeight > 0) {
            graphics.blit(ENERGY_FULL, left + ENERGY_X, top + BAR_Y + BAR_HEIGHT - energyHeight,
                    0, BAR_HEIGHT - energyHeight, BAR_WIDTH, energyHeight, BAR_WIDTH, BAR_HEIGHT);
        }

        graphics.blit(COOLANT_BACK, left + COOLANT_X, top + BAR_Y, 0, 0,
                BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        int coolant = menu.getData(ModelTrainerMenu.DATA_COOLANT);
        if (coolant > 0) {
            int coolantMax = Math.max(1, menu.getData(ModelTrainerMenu.DATA_COOLANT_MAX));
            int coolantHeight = BAR_HEIGHT * coolant / coolantMax;
            graphics.blit(COOLANT_FULL, left + COOLANT_X, top + BAR_Y + BAR_HEIGHT - coolantHeight,
                    0, BAR_HEIGHT - coolantHeight, BAR_WIDTH, coolantHeight, BAR_WIDTH, BAR_HEIGHT);
        }
    }

    private void renderLevel(GuiGraphics graphics, int left, int top) {
        int levelValue = menu.getData(ModelTrainerMenu.DATA_LEVEL);
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
        renderStatus(graphics, left, top);
    }

    private void renderStatus(GuiGraphics graphics, int left, int top) {
        int status = menu.getData(ModelTrainerMenu.DATA_STATUS);
        if (status == ModelTrainerBlockEntity.STATUS_TRAINING) {
            return;
        }
        Component text = Component.translatable(statusKey(status));
        graphics.drawString(font, text, left + 88 - font.width(text) / 2,
                top + 48, statusColor(status), false);
    }

    private static String statusKey(int status) {
        return switch (status) {
            case ModelTrainerBlockEntity.STATUS_MISSING_MODEL_PLUGIN ->
                    "gui.appraisal.model_trainer.status.missing_model_plugin";
            case ModelTrainerBlockEntity.STATUS_MISSING_TARGET ->
                    "gui.appraisal.model_trainer.status.missing_target";
            case ModelTrainerBlockEntity.STATUS_LEVEL_TOO_LOW ->
                    "gui.appraisal.model_trainer.status.level_too_low";
            case ModelTrainerBlockEntity.STATUS_MISSING_BLOCK_CARD ->
                    "gui.appraisal.model_trainer.status.missing_block_card";
            case ModelTrainerBlockEntity.STATUS_NO_TRAINING_DATA ->
                    "gui.appraisal.model_trainer.status.no_training_data";
            case ModelTrainerBlockEntity.STATUS_MISSING_EXP_CARD ->
                    "gui.appraisal.model_trainer.status.missing_exp_card";
            case ModelTrainerBlockEntity.STATUS_DIMENSION_BREACH ->
                    "gui.appraisal.model_trainer.dimension_breach";
            case ModelTrainerBlockEntity.STATUS_NO_ENERGY ->
                    "gui.appraisal.model_trainer.status.no_energy";
            case ModelTrainerBlockEntity.STATUS_NO_COOLANT ->
                    "gui.appraisal.model_trainer.status.no_coolant";
            default -> "gui.appraisal.model_trainer.ready";
        };
    }

    private static int statusColor(int status) {
        return status == ModelTrainerBlockEntity.STATUS_READY ? 0x55FF55 : 0xFF5555;
    }

    private void renderProgress(GuiGraphics graphics, int left, int top) {
        graphics.blit(PROGRESS_BACK, left + PROGRESS_X, top + PROGRESS_Y, 0, 0,
                PROGRESS_WIDTH, PROGRESS_HEIGHT, PROGRESS_WIDTH, PROGRESS_HEIGHT);
        int progress = menu.getData(ModelTrainerMenu.DATA_PROGRESS);
        int total = Math.max(1, menu.getData(ModelTrainerMenu.DATA_PROGRESS_TOTAL));
        int fillWidth = PROGRESS_WIDTH * progress / total;
        if (fillWidth > 0) {
            graphics.blit(PROGRESS_FULL, left + PROGRESS_X, top + PROGRESS_Y, 0, 0,
                    fillWidth, PROGRESS_HEIGHT, PROGRESS_WIDTH, PROGRESS_HEIGHT);
        }
    }

    private void renderStartButton(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        graphics.blit(START_BUTTON, left + START_X, top + START_Y, 0, 0,
                START_WIDTH, START_HEIGHT, START_WIDTH, START_HEIGHT);
        boolean training = menu.getData(ModelTrainerMenu.DATA_TRAINING) != 0;
        boolean enabled = menu.getData(ModelTrainerMenu.DATA_CAN_START) != 0;
        if (!enabled) {
            graphics.fill(left + START_X, top + START_Y,
                    left + START_X + START_WIDTH, top + START_Y + START_HEIGHT, 0x66000000);
        }
        Component label = Component.translatable(training
                ? "gui.appraisal.model_trainer.stop"
                : "gui.appraisal.model_trainer.start");
        graphics.drawString(font, label,
                left + START_X + START_WIDTH / 2 - font.width(label) / 2,
                top + START_Y + 5, enabled ? 0x404040 : 0x808080, false);
        if (inside(mouseX, mouseY, left + START_X, top + START_Y, START_WIDTH, START_HEIGHT)) {
            graphics.fill(left + START_X, top + START_Y,
                    left + START_X + START_WIDTH, top + START_Y + START_HEIGHT, 0x30FFFFFF);
        }
    }

    private void drawIconButton(GuiGraphics graphics, int x, int y, int width, int height,
                                ResourceLocation base, ResourceLocation icon, int mouseX, int mouseY) {
        graphics.blit(base, x, y, 0, 0, width, height, width, height);
        if (icon != null) {
            graphics.blit(icon, x, y, 0, 0, width, height, width, height);
        }
        if (inside(mouseX, mouseY, x, y, width, height)) {
            graphics.fill(x, y, x + width, y + height, 0x40FFFFFF);
        }
    }

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
            if (menu.getPage() == ModelTrainerMenu.PAGE_MAIN) {
                if (inside(mouseX, mouseY, left + PLUGIN_X, top + PLUGIN_Y, 9, 9)) {
                    sendButton(ModelTrainerMenu.BUTTON_PAGE_PLUGIN);
                    return true;
                }
                if (inside(mouseX, mouseY, left + START_X, top + START_Y,
                        START_WIDTH, START_HEIGHT)) {
                    if (menu.getData(ModelTrainerMenu.DATA_CAN_START) != 0) {
                        sendButton(ModelTrainerMenu.BUTTON_START);
                    }
                    return true;
                }
            } else if (inside(mouseX, mouseY, left + PLUGIN_X, top + PLUGIN_Y, 9, 9)) {
                sendButton(ModelTrainerMenu.BUTTON_PAGE_MAIN);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendButton(int id) {
        playClick();
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
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
        if (menu.getPage() == ModelTrainerMenu.PAGE_MAIN) {
            if (inside(mouseX, mouseY, left + ENERGY_X, top + BAR_Y, BAR_WIDTH, BAR_HEIGHT)) {
                return List.of(
                        Component.translatable("gui.appraisal.energy",
                                menu.getData(ModelTrainerMenu.DATA_ENERGY),
                                menu.getData(ModelTrainerMenu.DATA_ENERGY_MAX)),
                        Component.translatable("gui.appraisal.energy.transfer",
                                menu.getMachine().getEnergyTransfer()));
            }
            if (inside(mouseX, mouseY, left + COOLANT_X, top + BAR_Y, BAR_WIDTH, BAR_HEIGHT)) {
                return List.of(Component.translatable("gui.appraisal.coolant",
                        menu.getData(ModelTrainerMenu.DATA_COOLANT),
                        menu.getData(ModelTrainerMenu.DATA_COOLANT_MAX)));
            }
            if (inside(mouseX, mouseY, left + PROGRESS_X, top + PROGRESS_Y,
                    PROGRESS_WIDTH, PROGRESS_HEIGHT)) {
                int progress = menu.getData(ModelTrainerMenu.DATA_PROGRESS);
                int total = Math.max(1, menu.getData(ModelTrainerMenu.DATA_PROGRESS_TOTAL));
                return List.of(Component.translatable("gui.appraisal.progress",
                        (progress * 100 / total) + "%"));
            }
            if (menu.getMachine().getInventory()
                    .getStackInSlot(ModelTrainerBlockEntity.SLOT_BLOCK_CARD).isEmpty()
                    && inside(mouseX, mouseY, left + 62, top + 30, 18, 18)) {
                return List.of(Component.translatable(
                        "gui.appraisal.model_trainer.slot.block_card"));
            }
            if (menu.getMachine().getInventory()
                    .getStackInSlot(ModelTrainerBlockEntity.SLOT_MODEL_PLUGIN).isEmpty()
                    && inside(mouseX, mouseY, left + 80, top + 30, 18, 18)) {
                return List.of(Component.translatable(
                        "gui.appraisal.model_trainer.slot.model_plugin"));
            }
            if (menu.getMachine().getInventory()
                    .getStackInSlot(ModelTrainerBlockEntity.SLOT_EXP_CARD).isEmpty()
                    && inside(mouseX, mouseY, left + 98, top + 30, 18, 18)) {
                return List.of(Component.translatable(
                        "gui.appraisal.model_trainer.slot.exp_card"));
            }
            if (menu.getMachine().getInventory()
                    .getStackInSlot(ModelTrainerBlockEntity.SLOT_TARGET).isEmpty()
                    && inside(mouseX, mouseY, left + 131, top + 30, 18, 18)) {
                return List.of(Component.translatable(
                        "gui.appraisal.model_trainer.slot.target"));
            }
            if (inside(mouseX, mouseY, left + START_X, top + START_Y,
                    START_WIDTH, START_HEIGHT)) {
                return List.of(Component.translatable("gui.appraisal.model_trainer.start"));
            }
            if (inside(mouseX, mouseY, left + PLUGIN_X, top + PLUGIN_Y, 9, 9)) {
                return List.of(Component.translatable("gui.appraisal.plugin"));
            }
        } else if (inside(mouseX, mouseY, left + PLUGIN_X, top + PLUGIN_Y, 9, 9)) {
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
