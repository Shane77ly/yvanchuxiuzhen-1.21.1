package net.yvanchuxiuzhen.flow;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.combat.api.ElementStatistics;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.world.api.WorldCommands;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 原初管理命令按当前维度显示名选择玩家，各子命令只调用所属系统的公开操作。 */
final class PlayerCommandFlow {
    private PlayerCommandFlow() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("yvanchu").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(PlayerCommandFlow::execute))
                .executes(PlayerCommandFlow::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String[] words = context.getInput().split("\\s+");
        String target = argument(words, 0);
        for (ServerPlayer player : new ArrayList<>(source.getLevel().players())) {
            if (!player.getDisplayName().getString().equals(target)) continue;
            String action = argument(words, 1);
            String subject = argument(words, 2);
            String operation = argument(words, 3);
            if (!Professions.command(player, action, subject, operation, argument(words, 4), argument(words, 5))
                    && !Cultivation.command(source.getLevel(), player, action, subject, operation, argument(words, 4), argument(words, 5))
                    && !ElementStatistics.command(player, action, subject, operation, argument(words, 4), argument(words, 5))) {
                WorldCommands.execute(source, player, action, subject, operation, argument(words, 4));
            }
            return 0;
        }
        Entity executor = source.getEntity();
        if (executor == null) executor = FakePlayerFactory.getMinecraft(source.getLevel());
        if (executor instanceof Player player) {
            player.displayClientMessage(Component.literal("§4未找到名为“" + target + "”的玩家"), false);
        }
        return 0;
    }

    private static String argument(String[] words, int index) {
        return index + 1 < words.length ? words[index + 1] : "";
    }
}
