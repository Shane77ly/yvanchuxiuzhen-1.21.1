package net.yvanchuxiuzhen.system.world.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.world.StructureReleases;
import net.yvanchuxiuzhen.system.world.WorldGeneration;

/** 原初管理命令的世界分支；目标玩家由共享命令入口按当前维度显示名选择。 */
public final class WorldCommands {
    private WorldCommands() {
    }

    public static void execute(CommandSourceStack source, ServerPlayer player, String action, String subject, String value, String feedback) {
        if (subject.equals("jiegoushengcheng")) {
            if (action.equals("unlock")) {
                WorldGeneration.setRandomStructuresAllowed(source.getServer(), true);
                if (value.equals("true")) player.displayClientMessage(Component.literal("已开启结构随机生成。"), false);
            } else if (action.equals("lock")) {
                WorldGeneration.setRandomStructuresAllowed(source.getServer(), false);
                if (value.equals("true")) player.displayClientMessage(Component.literal("已关闭结构随机生成。"), false);
            }
        } else if (action.equals("unlock") && subject.equals("sealrelease")) {
            StructureReleases.release(source.getLevel(), source.getPosition(), value);
            if (feedback.equals("true")) player.displayClientMessage(Component.literal("§6解锁结构" + value), false);
        }
    }
}
