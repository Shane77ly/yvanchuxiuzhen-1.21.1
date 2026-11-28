package net.yvanchuxiuzhen.system.quest;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 来源聊天选择命令兼容显示名、renwuweizhi标记和序号，并把选择交回当前任务会话。 */
final class QuestChatCommand {
    private QuestChatCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("taskduihua")
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(QuestChatCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        Selection selection = Selection.parse(StringArgumentType.getString(context, "arguments"));
        if (selection == null) return 0;
        for (ServerPlayer player : context.getSource().getLevel().players()) {
            if (!player.getDisplayName().getString().equals(selection.name()) && !player.getName().getString().equals(selection.name())) continue;
            writeScoreboard(player, selection.number());
            QuestDialogueSession state = DialogueActions.snapshot(player);
            int target = state.choiceTarget(selection.number());
            if (!state.choicePending() || target <= 0) continue;
            player.setData(QuestData.SESSION, state.toBuilder().playerChoice(target).choicePending(false).build());
            QuestChatRuntime.run(player);
            break;
        }
        return 0;
    }

    private static void writeScoreboard(ServerPlayer player, int number) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("renwuweizhi");
        if (objective == null) objective = scoreboard.addObjective("renwuweizhi", ObjectiveCriteria.DUMMY,
                Component.literal("renwuweizhi"), ObjectiveCriteria.RenderType.INTEGER, false, null);
        scoreboard.getOrCreatePlayerScore(player, objective).set(number);
    }

    private record Selection(String name, int number) {
        private static Selection parse(String raw) {
            String value = raw.trim();
            if (value.isEmpty()) return null;
            int separator = value.lastIndexOf(' ');
            if (separator < 0) return null;
            String numberText = value.substring(separator + 1).trim();
            int number;
            try {
                number = (int) Double.parseDouble(numberText);
            } catch (NumberFormatException exception) {
                return null;
            }
            String nameAndMarker = value.substring(0, separator).trim();
            if (nameAndMarker.endsWith(" renwuweizhi")) nameAndMarker = nameAndMarker.substring(0, nameAndMarker.length() - " renwuweizhi".length()).trim();
            if (nameAndMarker.length() >= 2 && nameAndMarker.startsWith("\"") && nameAndMarker.endsWith("\"")) {
                nameAndMarker = nameAndMarker.substring(1, nameAndMarker.length() - 1);
            }
            return nameAndMarker.isEmpty() ? null : new Selection(nameAndMarker, number);
        }
    }
}
