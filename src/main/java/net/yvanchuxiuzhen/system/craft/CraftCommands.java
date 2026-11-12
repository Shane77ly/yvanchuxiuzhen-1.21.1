package net.yvanchuxiuzhen.system.craft;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** 研磨命令按来源保留无权限门槛与无参数时先清理成品的入口。 */
final class CraftCommands {
    private CraftCommands() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("grind")
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(CraftCommands::execute))
                .executes(CraftCommands::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        String[] parameters = context.getInput().split("\\s+");
        CommandSourceStack source = context.getSource();
        Grinding.grind(source.getLevel(), source.getPosition(), parameters.length > 1 ? parameters[1] : "");
        return 0;
    }
}
