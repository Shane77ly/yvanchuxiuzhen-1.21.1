package net.yvanchuxiuzhen.system.liandan;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;

/** 来源 {@code YvanchuCommand} 的药草解锁分支，按当前维度显示名称选择玩家。 */
public final class LiandanHerbCommand {
    private LiandanHerbCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("yvanchuxitong").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString())
                        .executes(context -> run(context.getSource(), StringArgumentType.getString(context, "arguments"))))
                .executes(context -> run(context.getSource(), "")));
    }

    private static int run(CommandSourceStack source, String raw) {
        List<String> arguments = raw.isBlank() ? List.of() : Arrays.asList(raw.trim().split("\\s+"));
        if (arguments.size() < 3 || !arguments.get(1).equals("unlock") || !arguments.get(2).equals("yaocai")) return 0;
        ServerLevel level = source.getLevel();
        ServerPlayer target = null;
        for (ServerPlayer player : level.players()) {
            if (player.getDisplayName().getString().equals(arguments.get(0))) {
                target = player;
                break;
            }
        }
        if (target == null) {
            source.sendFailure(Component.literal("§4未找到名为“" + arguments.get(0) + "”的玩家"));
            return 0;
        }
        Liandan.unlockHerbs(target);
        if (arguments.size() > 3 && arguments.get(3).equals("true")) {
            source.sendSuccess(() -> Component.literal("§6已解锁所有炼丹材料的属性显示。"), false);
        }
        return 0;
    }
}
