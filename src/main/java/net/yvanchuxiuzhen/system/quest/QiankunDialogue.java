package net.yvanchuxiuzhen.system.quest;

import java.util.Map;
import net.yvanchuxiuzhen.system.npc.api.QiankunDialogueRequestEvent;
import net.yvanchuxiuzhen.system.npc.api.QiankunQuestRequestEvent;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;

/** 原初系统的初见乾坤道人任务；按模板 ID 触发结构化对话树。 */
final class QiankunDialogue {
    private static final Map<Integer, String> QUEST_IDS = Map.of(
            1, "qiankun_1",
            2, "qiankun_2",
            3, "qiankun_3",
            4, "qiankun_4",
            5, "qiankun_5",
            6, "qiankun_6",
            7, "qiankun_7",
            8, "qiankun_8"
    );

    private QiankunDialogue() {
    }

    static void request(QiankunDialogueRequestEvent event) {
        DialogueActions.request(event.player(), "qiankun_intro");
    }

    static void requestQuest(QiankunQuestRequestEvent event) {
        String templateId = QUEST_IDS.get(event.questIndex());
        if (templateId != null) {
            DialogueActions.request(event.player(), templateId);
        }
    }
}
