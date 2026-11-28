package net.yvanchuxiuzhen.system.quest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession.JournalRow;
import net.yvanchuxiuzhen.system.quest.api.QuestJournalState;
import net.yvanchuxiuzhen.system.quest.api.QuestJournalState.QuestJournalEntry;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplates;
import net.yvanchuxiuzhen.system.quest.content.QuestJournalMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 任务列表由服务端会话保存分类、原始目录页码、八行强类型投影及当前选择。 */
public final class QuestJournalRuntime {
    private static final String UNFINISHED = "未完成任务";
    private static final String FINISHED = "已完成任务";

    private QuestJournalRuntime() {
    }

    public static void open(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        player.openMenu(new SeamlessMenuProvider((id, inventory, ignored) -> new QuestJournalMenu(id, inventory, pos),
                Component.literal("任务列表")), pos);
        QuestDialogueSession state = DialogueActions.snapshot(player);
        QuestDialogueSession.Builder builder = state.toBuilder();
        String category = state.journal().category();
        if (category.isEmpty()) {
            category = UNFINISHED;
            builder.category(category);
        }
        builder.selectedTitle("").selectedUuid("").displayText("").selectedRequirement("").selectedReward("");
        for (int row = 1; row <= 8; row++) {
            builder.row(row, JournalRow.EMPTY).detailLine(row, "");
        }
        QuestJournalState journal = DialogueActions.journal(player);
        List<String> ids = journal.questIds();
        int totalPages = ids.size() / 8;
        builder.totalPages(totalPages);
        int row = 1;
        for (int index = state.journal().page() * 8; index < ids.size() && row <= 8; index++) {
            String id = ids.get(index);
            QuestJournalEntry entry = journal.entry(id);
            if (entry == null) continue;
            boolean unfinished = entry.status().equals("未完成");
            boolean showingUnfinished = category.equals(UNFINISHED);
            if (!(unfinished && showingUnfinished) && !(!unfinished && category.equals(FINISHED))) continue;
            String displayName = unfinished ? entry.title() + "（进行中）" : entry.title();
            boolean high = unfinished && entry.highQuest();
            builder.row(row, new JournalRow(displayName, entry.uuid(), entry.description(),
                    entry.requirement(), entry.rewardSummary(), high));
            row++;
        }
        player.setData(QuestData.SESSION, builder.build());
    }

    public static boolean click(ServerPlayer player, int button) {
        if (button < 0 || button > 12) return false;
        QuestDialogueSession state = DialogueActions.snapshot(player);
        if (button <= 1) {
            String nextCategory = state.journal().category().equals(UNFINISHED) ? FINISHED : UNFINISHED;
            player.setData(QuestData.SESSION, state.toBuilder().category(nextCategory).build());
            open(player);
        } else if (button <= 3) {
            int total = state.journal().totalPages();
            int page = state.journal().page() + (button == 2 ? -1 : 1);
            if (page < 0) page = total;
            if (page > total) page = 0;
            player.setData(QuestData.SESSION, state.toBuilder().page(page).build());
            open(player);
        } else if (button <= 11) {
            select(player, state, button - 3);
        } else {
            continueTask(player, state);
        }
        return true;
    }

    private static void select(ServerPlayer player, QuestDialogueSession state, int rowIndex) {
        JournalRow selected = state.row(rowIndex);
        String description = selected.description();
        QuestDialogueSession.Builder builder = state.toBuilder()
                .selectedTitle(selected.name())
                .selectedUuid(selected.uuid())
                .displayText(description)
                .selectedRequirement(selected.requirement())
                .selectedReward(selected.reward())
                .selectedHigh(selected.high());
        for (int line = 1; line <= 8; line++) builder.detailLine(line, "");
        int lines = Math.max(1, (int) Math.ceil(description.length() / 18.0));
        for (int line = 0; line < lines && line < 8; line++) {
            builder.detailLine(line + 1, description.substring(line * 18, Math.min(description.length(), (line + 1) * 18)));
        }
        player.setData(QuestData.SESSION, builder.build());
    }

    private static void continueTask(ServerPlayer player, QuestDialogueSession state) {
        QuestDialogueSession.Builder builder = state.toBuilder();
        if (!state.journal().category().equals(FINISHED)) {
            String id = state.journal().selectedUuid();
            if (id.isEmpty()) {
                player.displayClientMessage(Component.literal("???任务是空的"), false);
            } else {
                QuestJournalEntry entry = DialogueActions.journal(player).entry(id);
                QuestTemplateDefinition template = entry == null ? null : entry.template()
                        .or(() -> QuestTemplates.find(entry.templateId()))
                        .orElse(null);
                if (template == null) {
                    player.displayClientMessage(Component.literal("???任务模板缺失：" + id), false);
                } else {
                    builder.template(java.util.Optional.of(template)).cursor(entry.progress());
                    if (state.journal().selectedHigh()) builder.dialogueBegin(true);
                    else builder.taskBegin(true);
                }
            }
        }
        player.setData(QuestData.SESSION, builder.build());
    }
}
