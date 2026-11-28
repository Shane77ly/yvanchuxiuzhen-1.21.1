package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.quest.DialogueRuntime;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition.EffectNode;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 两种解释器在同一服务端刻内消费受控模板，直接读取效果时长与任务游标。 */
final class QuestEffectCheck extends ActionCheck {
    QuestEffectCheck(List<String> report, String levelId) {
        super("任务对话效果", report, levelId);
        script(new Action(1, player -> {
            QuestDialogueSession session = DialogueActions.snapshot(player);
            MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
            MobEffectInstance healing = player.getEffect(MobEffects.HEAL);
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.HEAL);
            try {
                for (boolean chat : List.of(false, true)) check(player, chat);
            } finally {
                player.removeEffect(MobEffects.MOVEMENT_SPEED);
                player.removeEffect(MobEffects.HEAL);
                if (speed != null) player.addEffect(speed);
                if (healing != null) player.addEffect(healing);
                player.setData(QuestData.SESSION, session);
            }
        }));
    }

    private void check(ServerPlayer player, boolean chat) {
        String mode = chat ? "连续聊天" : "界面对话";
        run(player, chat, new EffectNode("加", "speed", 2, 7), new EffectNode("加", "minecraft:instant_health", 1, 3));
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        MobEffectInstance heal = player.getEffect(MobEffects.HEAL);
        expectTrue(mode + "普通效果七秒等于140刻且显示粒子", speed != null && speed.getDuration() == 140 && speed.getAmplifier() == 2 && speed.isVisible());
        expectTrue(mode + "瞬时效果三秒参数只写三刻", heal != null && heal.getDuration() == 3 && heal.getAmplifier() == 1);
        run(player, chat, new EffectNode("减", "speed", -1, -1), new EffectNode("减", "instant_health", 0, 0),
                new EffectNode("加", "speed", 0, 0), new EffectNode("加", "speed", 256, 10),
                new EffectNode("加", "missing:absent", 0, 10), new EffectNode("加", "speed", 0, 1_000_001));
        expectTrue(mode + "清除忽略时长等级，无效效果不生效且游标继续", !player.hasEffect(MobEffects.MOVEMENT_SPEED)
                && !player.hasEffect(MobEffects.HEAL) && DialogueActions.snapshot(player).cursor() == 7);
        Scoreboard board = player.getScoreboard();
        PlayerTeam oldTeam = player.getTeam();
        PlayerTeam team = board.addPlayerTeam("smoke_quest_fx");
        try {
            team.setPlayerPrefix(Component.literal("测试 "));
            board.addPlayerToTeam(player.getScoreboardName(), team);
            run(player, chat, new EffectNode("加", "speed", 0, 10));
            expectTrue(mode + "带空格的显示名不能选中本人", player.getDisplayName().getString().contains(" ")
                    && !player.hasEffect(MobEffects.MOVEMENT_SPEED) && DialogueActions.snapshot(player).cursor() == 2);
        } finally {
            board.removePlayerTeam(team);
            if (oldTeam != null) board.addPlayerToTeam(player.getScoreboardName(), oldTeam);
        }
    }

    private static void run(ServerPlayer player, boolean chat, DialogueNodeDefinition... nodes) {
        QuestDialogueSession state = QuestDialogueSession.EMPTY.toBuilder()
                .template(Optional.of(new QuestTemplateDefinition("smoke_effect", "smoke_effect", "", "", 0, "", "", "", List.of(nodes))))
                .cursor(1)
                .taskSustained(chat)
                .taskTick(59)
                .build();
        player.setData(QuestData.SESSION, state);
        if (chat) DialogueRuntime.tick(new PlayerTickEvent.Post(player));
        else DialogueRuntime.run(player, player.blockPosition());
    }

    @Override protected void cleanup(ServerPlayer player) {}
}
