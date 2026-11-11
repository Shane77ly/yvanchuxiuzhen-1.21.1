package net.yvanchuxiuzhen.system.world;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** 来源原初管理命令中的随机结构开关，按当前维度玩家显示名定位操作对象。 */
public final class WorldGenerationCommands {
    private WorldGenerationCommands() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(WorldGenerationCommands::commands);
    }

    private static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("yvanchu").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString())
                        .executes(WorldGenerationCommands::execute))
                .executes(WorldGenerationCommands::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Entity executor = source.getEntity();
        if (executor == null) {
            executor = FakePlayerFactory.getMinecraft(source.getLevel());
        }
        String[] words = context.getInput().split("\\s+");
        String target = argument(words, 0);
        for (Player player : new ArrayList<>(source.getLevel().players())) {
            if (!player.getDisplayName().getString().equals(target)) {
                continue;
            }
            String action = argument(words, 1);
            if (argument(words, 2).equals("jiegoushengcheng")) {
                if (action.equals("unlock")) {
                    WorldGeneration.setRandomStructuresAllowed(source.getServer(), true);
                    if (argument(words, 3).equals("true")) {
                        player.displayClientMessage(Component.literal("已开启结构随机生成。"), false);
                    }
                } else if (action.equals("lock")) {
                    WorldGeneration.setRandomStructuresAllowed(source.getServer(), false);
                    if (argument(words, 3).equals("true")) {
                        player.displayClientMessage(Component.literal("已关闭结构随机生成。"), false);
                    }
                }
            }
            return 0;
        }
        if (executor instanceof Player player) {
            player.displayClientMessage(Component.literal("§4未找到名为“" + target + "”的玩家"), false);
        }
        return 0;
    }

    private static String argument(String[] words, int index) {
        return index + 1 < words.length ? words[index + 1] : "";
    }
}
