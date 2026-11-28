package net.yvanchuxiuzhen.system.quest.api;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 对话与任务目录的公开入口；调用方提交强类型模板或模板 ID，客户端只读强类型投影。 */
public final class DialogueActions {
    private DialogueActions() {
    }

    public static void request(ServerPlayer player, String templateId) {
        request(player, QuestTemplates.require(templateId));
    }

    public static void request(ServerPlayer player, QuestTemplateDefinition template) {
        QuestDialogueSession session = snapshot(player).toBuilder()
                .template(Optional.ofNullable(template))
                .cursor(0)
                .dialogueBegin(true)
                .build();
        player.setData(QuestData.SESSION, session);
    }

    public static Optional<QuestTemplateDefinition> activeTemplate(Player player) {
        return player.getData(QuestData.SESSION).template();
    }

    public static QuestDialogueSession snapshot(Player player) {
        return player.getData(QuestData.SESSION);
    }

    public static QuestJournalState journal(Player player) {
        return player.getData(QuestData.JOURNAL);
    }

    public static boolean finished(Player player, String id) {
        String state = journal(player).status(id);
        return state.equals("完成") || state.equals("失败");
    }
}
