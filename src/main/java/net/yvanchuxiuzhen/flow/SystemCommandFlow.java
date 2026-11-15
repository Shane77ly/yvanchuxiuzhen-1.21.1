package net.yvanchuxiuzhen.flow;

import java.util.HashMap;
import java.util.Map;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.economy.api.EconomyCurrency;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.zhenfa.api.FormationInspection;

/** 原初系统共享命令仅注册一次，具体知识操作交各数据所有者处理。 */
public final class SystemCommandFlow {
    private SystemCommandFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SystemCommandFlow::commands);
        NeoForge.EVENT_BUS.addListener(PlayerCommandFlow::register);
    }

    private static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("yvanchuxitong").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString())
                        .executes(context -> dispatch(context.getSource(), StringArgumentType.getString(context, "arguments"))))
                .executes(context -> dispatch(context.getSource(), "")));
        event.getDispatcher().register(Commands.literal("zizhijiancefankui")
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(context -> formationFeedback(
                        context.getSource().getLevel(), context.getSource().getEntity(), context.getInput())))
                .executes(context -> formationFeedback(context.getSource().getLevel(), context.getSource().getEntity(), context.getInput())));
    }

    private static int dispatch(CommandSourceStack source, String arguments) {
        if (Materials.command(source, arguments)) return 0;
        if (EconomyCurrency.command(source, arguments)) return 0;
        return Fulu.command(source, arguments) ? 0 : Liandan.command(source, arguments);
    }

    private static int formationFeedback(ServerLevel level, Entity source, String input) {
        Entity inspected = source == null ? FakePlayerFactory.getMinecraft(level) : source;
        Map<String, String> arguments = new HashMap<>();
        int index = -1;
        for (String part : input.split("\\s+")) {
            if (index >= 0) arguments.put(Integer.toString(index), part);
            index++;
        }
        FormationInspection.report(level, inspected, number(arguments, "0"),
                new Vec3(number(arguments, "1"), number(arguments, "2"), number(arguments, "3")));
        return 0;
    }

    private static double number(Map<String, String> arguments, String key) {
        try {
            return Double.parseDouble(arguments.getOrDefault(key, "").trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
