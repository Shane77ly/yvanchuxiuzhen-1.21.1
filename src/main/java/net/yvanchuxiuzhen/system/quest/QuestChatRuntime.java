package net.yvanchuxiuzhen.system.quest;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.world.api.StructureSearch;

/** 连续任务在聊天栏输出，按 60 刻节拍推进强类型节点；条件与扩展动作在本次调用内继续。 */
final class QuestChatRuntime {
    private QuestChatRuntime() {
    }

    static void run(ServerPlayer player) {
        CompoundTag state = DialogueActions.snapshot(player);
        if (state.getInt("playerxvanze") != 0) {
            state.putInt("renwuweizhi", state.getInt("playerxvanze"));
            state.putInt("playerxvanze", 0);
        }
        while (true) {
            QuestTemplateDefinition template = DialogueActions.readTemplate(state).orElse(null);
            if (template == null) return;
            int cursor = state.getInt("renwuweizhi");
            DialogueNodeDefinition node = template.node(cursor).orElse(null);
            if (node == null) {
                player.setData(QuestData.SESSION, state);
                return;
            }
            switch (node) {
                case DialogueNodeDefinition.TextNode text -> {
                    String prefix = text.playerSpeak() ? player.getDisplayName().getString() + "：" : "";
                    player.displayClientMessage(Component.literal(prefix + text.text()), false);
                    state.putInt("renwuweizhi", cursor + 1);
                    player.setData(QuestData.SESSION, state.copy());
                    // 来源主过程在文字后只调用第二、第三扩展过程，下一条主过程文字留到下一节拍。
                    if (!isChatExtended(template.node(cursor + 1).orElse(null))) return;
                }
                case DialogueNodeDefinition.ChoiceNode choice -> {
                    prepareOptions(player);
                    player.displayClientMessage(Component.literal(choice.prompt()), false);
                    StringBuilder command = new StringBuilder("tellraw " + player.getDisplayName().getString() + " [");
                    int count = Math.min(4, choice.options().size());
                    for (int index = 0; index < count; index++) {
                        DialogueNodeDefinition.ChoiceOption option = choice.options().get(index);
                        if (index > 0) command.append(',');
                        command.append("{\"text\":\"<").append(option.label()).append(" >\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/taskduihua ")
                                .append(player.getDisplayName().getString()).append(" renwuweizhi ").append(index + 1).append("\"}}");
                        state.putInt("daan" + (index + 1), option.target());
                    }
                    state.putBoolean("xvanxiang", true);
                    player.setData(QuestData.SESSION, state);
                    command(player, command.append(']').toString());
                    return;
                }
                case DialogueNodeDefinition.InsertChatOptionNode insert -> {
                    prepareOptions(player);
                    state.putBoolean("xvanxiang", true);
                    state.putInt("renwuxvanzeweizhi", insert.target());
                    player.setData(QuestData.SESSION, state);
                    command(player, "tellraw " + player.getDisplayName().getString() + " [{\"text\":\"" + insert.prefix()
                            + "\",\"color\":\"white\"},{\"text\":\"<" + insert.option()
                            + ">\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/scoreboard players set -2\"}},{\"text\":\""
                            + insert.suffix() + "\",\"color\":\"white\"}]");
                    return;
                }
                case DialogueNodeDefinition.ModifyFlagNode modify -> {
                    DialogueRuntime.changeFlag(player, state, modify);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.CheckFlagNode check ->
                        state.putInt("renwuweizhi", DialogueRuntime.compareFlag(player, check));
                case DialogueNodeDefinition.CheckItemNode check -> state.putInt("renwuweizhi",
                        DialogueRuntime.count(player, DialogueRuntime.item(check.item())) >= check.count()
                                ? check.passTarget() : check.failTarget());
                case DialogueNodeDefinition.CheckAdvancedItemNode check -> state.putInt("renwuweizhi",
                        DialogueRuntime.countAdvancedItems(player, check) >= check.count()
                                ? check.passTarget() : check.failTarget());
                case DialogueNodeDefinition.ItemActionNode itemAction -> {
                    if (itemAction.give()) DialogueRuntime.giveItem(player, itemAction);
                    else player.getInventory().clearOrCountMatchingItems(
                            stack -> stack.is(DialogueRuntime.item(itemAction.item())),
                            itemAction.count(), player.inventoryMenu.getCraftSlots());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.CheckPosNode checkPos -> {
                    Vec3 center = new Vec3(checkPos.x(), checkPos.y(), checkPos.z());
                    boolean present = player.serverLevel().getEntitiesOfClass(Player.class,
                            new AABB(center, center).inflate(checkPos.size() / 2), candidate -> candidate == player).contains(player);
                    state.putInt("renwuweizhi", present ? checkPos.insideTarget() : checkPos.outsideTarget());
                }
                case DialogueNodeDefinition.CheckDimensionNode checkDim -> {
                    String dimension = "ResourceKey[minecraft:dimension / ]" + checkDim.dimension() + "]";
                    state.putInt("renwuweizhi", player.level().dimension().toString().equals(dimension)
                            ? checkDim.matchTarget() : checkDim.mismatchTarget());
                }
                case DialogueNodeDefinition.CheckQuestNode checkQuest -> {
                    String status = DialogueActions.journal(player).getString(checkQuest.questId() + "zhuangtai");
                    state.putInt("renwuweizhi", status.equals("完成") ? checkQuest.completedTarget()
                            : status.equals("失败") ? checkQuest.failedTarget() : checkQuest.otherwiseTarget());
                }
                case DialogueNodeDefinition.EntityControlNode entityControl -> {
                    DialogueRuntime.controlEntities(player, entityControl);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.TeleportNode teleport -> {
                    player.connection.teleport(teleport.x(), teleport.y(), teleport.z(), player.getYRot(), player.getXRot());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.EffectNode effect -> {
                    String command = effect.op().equals("加")
                            ? "effect give " + player.getDisplayName().getString() + " " + effect.effectId() + " " + effect.duration() + " " + effect.amplifier()
                            : "effect clear " + player.getDisplayName().getString() + " " + effect.effectId();
                    command(player, command);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.CommandNode cmd -> {
                    command(player, cmd.command());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.JumpNode jump -> state.putInt("renwuweizhi", jump.target());
                case DialogueNodeDefinition.SetQuestStatusNode status -> {
                    state.putString("renwuzhuangtai", status.status());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.SaveQuestNode save -> {
                    if (state.getString("renwuzhuangtai").isEmpty()) state.putString("renwuzhuangtai", "未完成");
                    DialogueRuntime.saveTask(player, state, template, save.resumeCursor());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.ModifyQuestHeaderNode modify -> {
                    QuestTemplateDefinition updated = modify.field().equals("名称")
                            ? template.withHeader(modify.value(), template.description())
                            : modify.field().equals("说明")
                                    ? template.withHeader(template.title(), modify.value())
                                    : template;
                    DialogueActions.writeTemplate(state, updated);
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.UnsealRiftNode ignored -> {
                    StructureSearch.search(player.serverLevel(), player.position(), player.getStringUUID());
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.RemoveBlocksNode remove -> {
                    for (BlockPos blockPos : remove.positions()) {
                        player.serverLevel().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    state.putInt("renwuweizhi", cursor + 1);
                }
                case DialogueNodeDefinition.EndNode ignored -> {
                    DialogueActions.clearTemplate(state);
                    state.putInt("renwuweizhi", 0);
                    state.putBoolean("tasksustained", false);
                    state.putBoolean("xvanxiang", false);
                    state.putInt("tasktick", 0);
                    player.setData(QuestData.SESSION, state);
                    return;
                }
                case DialogueNodeDefinition.OpenMenuNode ignored -> {
                    player.setData(QuestData.SESSION, state);
                    return;
                }
                case DialogueNodeDefinition.TransferQuestNode ignored -> {
                    player.setData(QuestData.SESSION, state);
                    return;
                }
            }
            player.setData(QuestData.SESSION, state.copy());
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
        command(player, "scoreboard objectives add renwuweizhi dummy \"任务选项位置\"");
        player.serverLevel().getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(false, player.server);
    }

    private static void command(ServerPlayer player, String command) {
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO, player.serverLevel(), 4,
                "", Component.empty(), player.server, null).withSuppressedOutput();
        player.server.getCommands().performPrefixedCommand(source, command);
    }
}
