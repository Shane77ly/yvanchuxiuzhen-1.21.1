package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.Objective;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.quest.DialogueRuntime;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition.ChoiceNode;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition.ChoiceOption;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition.InsertChatOptionNode;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

final class QuestChatOptionsCheck extends SystemCheck {
    private final List<Component> received = new ArrayList<>();
    private final Consumer<ClientChatReceivedEvent.System> chat = event -> received.add(event.getMessage());
    private String name;

    QuestChatOptionsCheck(List<String> report, String levelId) { super("任务聊天选项", report, levelId); }

    @Override protected boolean step(int step, int local) {
        if (step == 0) {
            NeoForge.EVENT_BUS.addListener(chat);
            onServer(this::send);
            return true;
        }
        if (received.stream().noneMatch(c -> c.getString().equals("样本结束")) && local < 120) return false;
        try {
            List<String> messages = received.stream().map(Component::getString).toList();
            expectTrue("客户端收到末尾标记和两个问题提示", messages.containsAll(List.of("样本结束", "四择一", "坏格式")));
            expectTrue("非法引号不产生可点击行", messages.stream().noneMatch(s -> s.contains("非法选项")));
            List<String> spans = new ArrayList<>();
            for (Component message : received) {
                if (!List.of("<甲 ><乙 ><丙 ><丁 >", "起<内嵌>止").contains(message.getString())) continue;
                message.visit((style, text) -> {
                    var click = style.getClickEvent();
                    spans.add(text + ":" + (style.getColor() == null ? null : style.getColor().getValue()) + ":" + (click == null ? "" : click.getAction() + ":" + click.getValue()));
                    return Optional.empty();
                }, Style.EMPTY);
            }
            List<String> expected = new ArrayList<>();
            for (String label : List.of("甲", "乙", "丙", "丁")) expected.add("<" + label + " >:" + ChatFormatting.AQUA.getColor() + ":" + ClickEvent.Action.RUN_COMMAND + ":/taskduihua " + name + " renwuweizhi " + (expected.size() + 1));
            expected.add("起:" + ChatFormatting.WHITE.getColor() + ":");
            expected.add("<内嵌>:" + ChatFormatting.AQUA.getColor() + ":" + ClickEvent.Action.RUN_COMMAND + ":/scoreboard players set -2");
            expected.add("止:" + ChatFormatting.WHITE.getColor() + ":");
            expect("客户端两种选项文字、颜色与点击动作", expected, spans);
        } finally { NeoForge.EVENT_BUS.unregister(chat); }
        finish();
        return true;
    }

    private void send(ServerPlayer player) {
        var board = player.getScoreboard();
        var feedback = player.serverLevel().getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK);
        boolean wasEnabled = feedback.get();
        CompoundTag session = DialogueActions.snapshot(player);
        Objective existing = board.getObjective("renwuweizhi");
        Component oldLabel = existing == null ? null : existing.getDisplayName();
        try {
            name = player.getDisplayName().getString();
            feedback.set(true, player.server);
            run(player, new ChoiceNode("四择一", List.of(new ChoiceOption("甲", 8), new ChoiceOption("乙", 9), new ChoiceOption("丙", 10), new ChoiceOption("丁", 11), new ChoiceOption("戊", 12))));
            Objective objective = board.getObjective("renwuweizhi");
            var state = DialogueActions.snapshot(player);
            expectTrue("创建目标并关闭反馈，只写前四个选项", objective != null && (existing != null || objective.getDisplayName().getString().equals("任务选项位置")) && !feedback.get() && state.getInt("daan1") == 8 && state.getInt("daan4") == 11 && !state.contains("daan5"));
            if (objective != null) objective.setDisplayName(Component.literal("管理员自定义"));
            run(player, new InsertChatOptionNode("起", "内嵌", "止", 37));
            expectTrue("保留旧目标及内嵌跳转位", objective != null && board.getObjective("renwuweizhi") == objective && objective.getDisplayName().getString().equals("管理员自定义") && DialogueActions.snapshot(player).getInt("renwuxvanzeweizhi") == 37);
            run(player, new ChoiceNode("坏格式", List.of(new ChoiceOption("非法选项\"", 13))));
            state = DialogueActions.snapshot(player);
            expectTrue("无效文字仍锁定选择并保留目标", state.getBoolean("xvanxiang") && state.getInt("daan1") == 13);
            player.sendSystemMessage(Component.literal("样本结束"));
        } finally {
            if (existing != null) existing.setDisplayName(oldLabel);
            else if (board.getObjective("renwuweizhi") != null) board.removeObjective(board.getObjective("renwuweizhi"));
            feedback.set(wasEnabled, player.server);
            player.setData(QuestData.SESSION, session);
        }
    }

    private static void run(ServerPlayer player, DialogueNodeDefinition node) {
        CompoundTag state = new CompoundTag();
        DialogueActions.writeTemplate(state, new QuestTemplateDefinition("options", "options", "", "", 0, "", "", "", List.of(node)));
        state.putInt("renwuweizhi", 1);
        state.putInt("tasktick", 59);
        state.putBoolean("tasksustained", true);
        player.setData(QuestData.SESSION, state);
        DialogueRuntime.tick(new PlayerTickEvent.Post(player));
    }
}
