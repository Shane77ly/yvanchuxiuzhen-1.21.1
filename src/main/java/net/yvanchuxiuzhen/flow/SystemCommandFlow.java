package net.yvanchuxiuzhen.flow;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;

/** 原初系统共享命令仅注册一次，具体知识操作交各数据所有者处理。 */
public final class SystemCommandFlow {
    private SystemCommandFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SystemCommandFlow::commands);
    }

    private static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("yvanchuxitong").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString())
                        .executes(context -> dispatch(context.getSource(), StringArgumentType.getString(context, "arguments"))))
                .executes(context -> dispatch(context.getSource(), "")));
    }

    private static int dispatch(CommandSourceStack source, String arguments) {
        return Fulu.command(source, arguments) ? 0 : Liandan.command(source, arguments);
    }
}
