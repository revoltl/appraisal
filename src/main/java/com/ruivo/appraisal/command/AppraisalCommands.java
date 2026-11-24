package com.ruivo.appraisal.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.ruivo.appraisal.appraisal.AppraisalLevels;
import com.ruivo.appraisal.item.MagnifyingGlassItem;
import com.ruivo.appraisal.registry.ModAttachments;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class AppraisalCommands {

    private AppraisalCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("appraisal")
                .then(Commands.literal("level")
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int base = player.getData(ModAttachments.APPRAISAL_LEVEL.get());
                                    ItemStack tool = player.getMainHandItem();
                                    if (!(tool.getItem() instanceof MagnifyingGlassItem)) {
                                        tool = player.getOffhandItem();
                                    }
                                    int effective = AppraisalLevels.of(player, tool);
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.appraisal.level.get", effective, base), false);
                                    return effective;
                                }))
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int value = IntegerArgumentType.getInteger(context, "value");
                                            player.setData(ModAttachments.APPRAISAL_LEVEL.get(), value);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.appraisal.level.set", value), true);
                                            return value;
                                        })))));
    }
}
