package net.yvanchuxiuzhen.system.quest;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.world.api.StructureSearch;

/** 连续任务在聊天栏输出，按 60 刻节拍推进强类型节点；条件与扩展动作在本次调用内继续。 */
final class QuestChatRuntime {
    private QuestChatRuntime() {
    }

    static void run(ServerPlayer player) {
        QuestDialogueSession state = DialogueActions.snapshot(player);
        QuestDialogueSession.Builder builder = state.toBuilder();
        int cursor = state.cursor();
        if (state.playerChoice() != 0) {
            cursor = state.playerChoice();
            builder.cursor(cursor).playerChoice(0);
        }
        QuestTemplateDefinition template = state.template().orElse(null);
        while (true) {
            if (template == null) return;
            DialogueNodeDefinition node = template.node(cursor).orElse(null);
            if (node == null) {
                player.setData(QuestData.SESSION, builder.build());
                return;
            }
            switch (node) {
                case DialogueNodeDefinition.TextNode text -> {
                    String prefix = text.playerSpeak() ? player.getDisplayName().getString() + "：" : "";
                    player.displayClientMessage(Component.literal(prefix + text.text()), false);
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                    player.setData(QuestData.SESSION, builder.build());
                    if (!isChatExtended(template.node(cursor).orElse(null))) return;
                }
                case DialogueNodeDefinition.ChoiceNode choice -> {
                    prepareOptions(player);
                    player.displayClientMessage(Component.literal(choice.prompt()), false);
                    StringBuilder message = new StringBuilder("[");
                    int count = Math.min(4, choice.options().size());
                    for (int index = 0; index < count; index++) {
                        DialogueNodeDefinition.ChoiceOption option = choice.options().get(index);
                        if (index > 0) message.append(',');
                        message.append("{\"text\":\"<").append(option.label()).append(" >\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/taskduihua ")
                                .append(player.getDisplayName().getString()).append(" renwuweizhi ").append(index + 1).append("\"}}");
                        builder.choiceTarget(index + 1, option.target());
                    }
                    builder.choicePending(true);
                    player.setData(QuestData.SESSION, builder.build());
                    QuestScriptActions.tell(player, message.append(']').toString());
                    return;
                }
                case DialogueNodeDefinition.InsertChatOptionNode insert -> {
                    prepareOptions(player);
                    builder.choicePending(true).chatChoiceCursor(insert.target());
                    player.setData(QuestData.SESSION, builder.build());
                    QuestScriptActions.tell(player, "[{\"text\":\"" + insert.prefix()
                            + "\",\"color\":\"white\"},{\"text\":\"<" + insert.option()
                            + ">\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/scoreboard players set -2\"}},{\"text\":\""
                            + insert.suffix() + "\",\"color\":\"white\"}]");
                    return;
                }
                case DialogueNodeDefinition.ModifyFlagNode modify -> {
                    DialogueRuntime.changeFlag(player, modify);
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.CheckFlagNode check -> {
                    cursor = DialogueRuntime.compareFlag(player, check);
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.CheckItemNode check -> {
                    cursor = DialogueRuntime.count(player, DialogueRuntime.item(check.item())) >= check.count()
                            ? check.passTarget() : check.failTarget();
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.CheckAdvancedItemNode check -> {
                    cursor = DialogueRuntime.countAdvancedItems(player, check) >= check.count()
                            ? check.passTarget() : check.failTarget();
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.ItemActionNode itemAction -> {
                    if (itemAction.give()) DialogueRuntime.giveItem(player, itemAction);
                    else player.getInventory().clearOrCountMatchingItems(
                            stack -> stack.is(DialogueRuntime.item(itemAction.item())),
                            itemAction.count(), player.inventoryMenu.getCraftSlots());
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.CheckPosNode checkPos -> {
                    Vec3 center = new Vec3(checkPos.x(), checkPos.y(), checkPos.z());
                    boolean present = player.serverLevel().getEntitiesOfClass(Player.class,
                            new AABB(center, center).inflate(checkPos.size() / 2), candidate -> candidate == player).contains(player);
                    cursor = present ? checkPos.insideTarget() : checkPos.outsideTarget();
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.CheckDimensionNode checkDim -> {
                    cursor = DialogueRuntime.matchesDimension(player, checkDim.dimension())
                            ? checkDim.matchTarget() : checkDim.mismatchTarget();
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.CheckQuestNode checkQuest -> {
                    String status = DialogueActions.journal(player).status(checkQuest.questId());
                    cursor = status.equals("完成") ? checkQuest.completedTarget()
                            : status.equals("失败") ? checkQuest.failedTarget() : checkQuest.otherwiseTarget();
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.EntityControlNode entityControl -> {
                    DialogueRuntime.controlEntities(player, entityControl);
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.TeleportNode teleport -> {
                    player.connection.teleport(teleport.x(), teleport.y(), teleport.z(), player.getYRot(), player.getXRot());
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.EffectNode effect -> {
                    QuestScriptActions.effect(player, effect);
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.CommandNode cmd -> {
                    QuestScriptActions.command(player, cmd.command());
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.JumpNode jump -> {
                    cursor = jump.target();
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.SetQuestStatusNode status -> {
                    builder.questStatus(status.status());
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.SaveQuestNode save -> {
                    QuestDialogueSession built = builder.build();
                    if (built.questStatus().isEmpty()) {
                        builder.questStatus("未完成");
                        built = builder.build();
                    }
                    DialogueRuntime.saveTask(player, built, template, save.resumeCursor());
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.ModifyQuestHeaderNode modify -> {
                    template = modify.field().equals("名称")
                            ? template.withHeader(modify.value(), template.description())
                            : modify.field().equals("说明")
                                    ? template.withHeader(template.title(), modify.value())
                                    : template;
                    builder.template(Optional.of(template));
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.UnsealRiftNode ignored -> {
                    StructureSearch.search(player.serverLevel(), player.position(), player.getStringUUID());
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.RemoveBlocksNode remove -> {
                    for (BlockPos blockPos : remove.positions()) {
                        player.serverLevel().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    cursor = cursor + 1;
                    builder.cursor(cursor);
                }
                case DialogueNodeDefinition.EndNode ignored -> {
                    builder.template(Optional.empty()).cursor(0).taskSustained(false).choicePending(false).taskTick(0);
                    player.setData(QuestData.SESSION, builder.build());
                    return;
                }
                case DialogueNodeDefinition.OpenMenuNode ignored -> {
                    player.setData(QuestData.SESSION, builder.build());
                    return;
                }
                case DialogueNodeDefinition.TransferQuestNode ignored -> {
                    player.setData(QuestData.SESSION, builder.build());
                    return;
                }
            }
            player.setData(QuestData.SESSION, builder.build());
        }
    }

    private static boolean isChatExtended(DialogueNodeDefinition node) {
        return node instanceof DialogueNodeDefinition.SetQuestStatusNode
                || node instanceof DialogueNodeDefinition.SaveQuestNode
                || node instanceof DialogueNodeDefinition.ItemActionNode
                || node instanceof DialogueNodeDefinition.InsertChatOptionNode
                || node instanceof DialogueNodeDefinition.CheckQuestNode
                || node instanceof DialogueNodeDefinition.CheckAdvancedItemNode
                || node instanceof DialogueNodeDefinition.CheckPosNode
                || node instanceof DialogueNodeDefinition.CheckDimensionNode
                || node instanceof DialogueNodeDefinition.EntityControlNode
                || node instanceof DialogueNodeDefinition.ModifyQuestHeaderNode
                || node instanceof DialogueNodeDefinition.UnsealRiftNode
                || node instanceof DialogueNodeDefinition.RemoveBlocksNode;
    }

    private static void prepareOptions(ServerPlayer player) {
        var board = player.server.getScoreboard();
        if (board.getObjective("renwuweizhi") == null) {
            board.addObjective("renwuweizhi", ObjectiveCriteria.DUMMY, Component.literal("任务选项位置"), ObjectiveCriteria.RenderType.INTEGER, false, null);
        }
        player.serverLevel().getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(false, player.server);
    }
}
