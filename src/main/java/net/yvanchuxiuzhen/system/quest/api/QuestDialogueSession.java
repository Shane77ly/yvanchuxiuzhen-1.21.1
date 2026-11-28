package net.yvanchuxiuzhen.system.quest.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 任务对话执行状态与任务列表界面投影，强类型持久化并同步给本人客户端。 */
public record QuestDialogueSession(DialogueState dialogue, JournalView journal) {
    public static final QuestDialogueSession EMPTY = new QuestDialogueSession(DialogueState.EMPTY, JournalView.EMPTY);

    public static final Codec<QuestDialogueSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DialogueState.CODEC.fieldOf("dialogue").forGetter(QuestDialogueSession::dialogue),
            JournalView.CODEC.fieldOf("journal").forGetter(QuestDialogueSession::journal)
    ).apply(instance, QuestDialogueSession::new));

    public static final StreamCodec<ByteBuf, QuestDialogueSession> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isEmpty() {
        return equals(EMPTY);
    }

    public Optional<QuestTemplateDefinition> template() {
        return dialogue.template();
    }

    public int cursor() {
        return dialogue.cursor();
    }

    public boolean dialogueBegin() {
        return dialogue.dialogueBegin();
    }

    public boolean taskBegin() {
        return dialogue.taskBegin();
    }

    public boolean taskSustained() {
        return dialogue.taskSustained();
    }

    public int taskTick() {
        return dialogue.taskTick();
    }

    public boolean choicePending() {
        return dialogue.choicePending();
    }

    public int playerChoice() {
        return dialogue.playerChoice();
    }

    public int chatChoiceCursor() {
        return dialogue.chatChoiceCursor();
    }

    public String questStatus() {
        return dialogue.questStatus();
    }

    public String displayText() {
        return dialogue.displayText();
    }

    public String choiceText(int index1To4) {
        if (index1To4 < 1 || index1To4 > dialogue.choiceTexts().size()) return "";
        return dialogue.choiceTexts().get(index1To4 - 1);
    }

    public int choiceTarget(int index1To4) {
        if (index1To4 < 1 || index1To4 > dialogue.choiceTargets().size()) return 0;
        return dialogue.choiceTargets().get(index1To4 - 1);
    }

    public JournalRow row(int index1To8) {
        if (index1To8 < 1 || index1To8 > journal.rows().size()) return JournalRow.EMPTY;
        return journal.rows().get(index1To8 - 1);
    }

    public String detailLine(int index1To8) {
        if (index1To8 < 1 || index1To8 > journal.detailLines().size()) return "";
        return journal.detailLines().get(index1To8 - 1);
    }

    public QuestDialogueSession withTemplate(QuestTemplateDefinition template) {
        return toBuilder().template(Optional.ofNullable(template)).build();
    }

    public QuestDialogueSession withoutTemplate() {
        return toBuilder().template(Optional.empty()).build();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public record DialogueState(
            Optional<QuestTemplateDefinition> template,
            int cursor,
            boolean dialogueBegin,
            boolean taskBegin,
            boolean taskSustained,
            int taskTick,
            boolean choicePending,
            int playerChoice,
            int chatChoiceCursor,
            String questStatus,
            String displayText,
            List<String> choiceTexts,
            List<Integer> choiceTargets) {
        public static final DialogueState EMPTY = new DialogueState(
                Optional.empty(), 0, false, false, false, 0, false, 0, 0, "", "",
                List.of("", "", "", ""), List.of(0, 0, 0, 0));

        public static final Codec<DialogueState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                QuestTemplateDefinition.CODEC.optionalFieldOf("template").forGetter(DialogueState::template),
                Codec.INT.fieldOf("cursor").forGetter(DialogueState::cursor),
                Codec.BOOL.fieldOf("dialogue_begin").forGetter(DialogueState::dialogueBegin),
                Codec.BOOL.fieldOf("task_begin").forGetter(DialogueState::taskBegin),
                Codec.BOOL.fieldOf("task_sustained").forGetter(DialogueState::taskSustained),
                Codec.INT.fieldOf("task_tick").forGetter(DialogueState::taskTick),
                Codec.BOOL.fieldOf("choice_pending").forGetter(DialogueState::choicePending),
                Codec.INT.fieldOf("player_choice").forGetter(DialogueState::playerChoice),
                Codec.INT.fieldOf("chat_choice_cursor").forGetter(DialogueState::chatChoiceCursor),
                Codec.STRING.fieldOf("quest_status").forGetter(DialogueState::questStatus),
                Codec.STRING.fieldOf("display_text").forGetter(DialogueState::displayText),
                Codec.STRING.listOf().fieldOf("choice_texts").forGetter(DialogueState::choiceTexts),
                Codec.INT.listOf().fieldOf("choice_targets").forGetter(DialogueState::choiceTargets)
        ).apply(instance, DialogueState::new));
    }

    public record JournalRow(String name, String uuid, String description, String requirement, String reward, boolean high) {
        public static final JournalRow EMPTY = new JournalRow("", "", "", "", "", false);

        public static final Codec<JournalRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(JournalRow::name),
                Codec.STRING.fieldOf("uuid").forGetter(JournalRow::uuid),
                Codec.STRING.fieldOf("description").forGetter(JournalRow::description),
                Codec.STRING.fieldOf("requirement").forGetter(JournalRow::requirement),
                Codec.STRING.fieldOf("reward").forGetter(JournalRow::reward),
                Codec.BOOL.fieldOf("high").forGetter(JournalRow::high)
        ).apply(instance, JournalRow::new));
    }

    public record JournalView(
            String category,
            int page,
            int totalPages,
            String selectedTitle,
            String selectedUuid,
            String selectedRequirement,
            String selectedReward,
            boolean selectedHigh,
            List<String> detailLines,
            List<JournalRow> rows) {
        public static final JournalView EMPTY = new JournalView(
                "", 0, 0, "", "", "", "", false,
                List.of("", "", "", "", "", "", "", ""),
                Collections.nCopies(8, JournalRow.EMPTY));

        public static final Codec<JournalView> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("category").forGetter(JournalView::category),
                Codec.INT.fieldOf("page").forGetter(JournalView::page),
                Codec.INT.fieldOf("total_pages").forGetter(JournalView::totalPages),
                Codec.STRING.fieldOf("selected_title").forGetter(JournalView::selectedTitle),
                Codec.STRING.fieldOf("selected_uuid").forGetter(JournalView::selectedUuid),
                Codec.STRING.fieldOf("selected_requirement").forGetter(JournalView::selectedRequirement),
                Codec.STRING.fieldOf("selected_reward").forGetter(JournalView::selectedReward),
                Codec.BOOL.fieldOf("selected_high").forGetter(JournalView::selectedHigh),
                Codec.STRING.listOf().fieldOf("detail_lines").forGetter(JournalView::detailLines),
                JournalRow.CODEC.listOf().fieldOf("rows").forGetter(JournalView::rows)
        ).apply(instance, JournalView::new));
    }

    public static final class Builder {
        private Optional<QuestTemplateDefinition> template;
        private int cursor;
        private boolean dialogueBegin;
        private boolean taskBegin;
        private boolean taskSustained;
        private int taskTick;
        private boolean choicePending;
        private int playerChoice;
        private int chatChoiceCursor;
        private String questStatus;
        private String displayText;
        private final List<String> choiceTexts;
        private final List<Integer> choiceTargets;
        private String category;
        private int page;
        private int totalPages;
        private String selectedTitle;
        private String selectedUuid;
        private String selectedRequirement;
        private String selectedReward;
        private boolean selectedHigh;
        private final List<String> detailLines;
        private final List<JournalRow> rows;

        private Builder(QuestDialogueSession source) {
            DialogueState d = source.dialogue();
            this.template = d.template();
            this.cursor = d.cursor();
            this.dialogueBegin = d.dialogueBegin();
            this.taskBegin = d.taskBegin();
            this.taskSustained = d.taskSustained();
            this.taskTick = d.taskTick();
            this.choicePending = d.choicePending();
            this.playerChoice = d.playerChoice();
            this.chatChoiceCursor = d.chatChoiceCursor();
            this.questStatus = d.questStatus();
            this.displayText = d.displayText();
            this.choiceTexts = new ArrayList<>(d.choiceTexts());
            while (this.choiceTexts.size() < 4) this.choiceTexts.add("");
            this.choiceTargets = new ArrayList<>(d.choiceTargets());
            while (this.choiceTargets.size() < 4) this.choiceTargets.add(0);

            JournalView j = source.journal();
            this.category = j.category();
            this.page = j.page();
            this.totalPages = j.totalPages();
            this.selectedTitle = j.selectedTitle();
            this.selectedUuid = j.selectedUuid();
            this.selectedRequirement = j.selectedRequirement();
            this.selectedReward = j.selectedReward();
            this.selectedHigh = j.selectedHigh();
            this.detailLines = new ArrayList<>(j.detailLines());
            while (this.detailLines.size() < 8) this.detailLines.add("");
            this.rows = new ArrayList<>(j.rows());
            while (this.rows.size() < 8) this.rows.add(JournalRow.EMPTY);
        }

        public Builder template(Optional<QuestTemplateDefinition> template) { this.template = template; return this; }
        public Builder cursor(int cursor) { this.cursor = cursor; return this; }
        public Builder dialogueBegin(boolean value) { this.dialogueBegin = value; return this; }
        public Builder taskBegin(boolean value) { this.taskBegin = value; return this; }
        public Builder taskSustained(boolean value) { this.taskSustained = value; return this; }
        public Builder taskTick(int value) { this.taskTick = value; return this; }
        public Builder choicePending(boolean value) { this.choicePending = value; return this; }
        public Builder playerChoice(int value) { this.playerChoice = value; return this; }
        public Builder chatChoiceCursor(int value) { this.chatChoiceCursor = value; return this; }
        public Builder questStatus(String value) { this.questStatus = value; return this; }
        public Builder displayText(String value) { this.displayText = value; return this; }
        public Builder choice(int index1To4, String text, int target) {
            this.choiceTexts.set(index1To4 - 1, text);
            this.choiceTargets.set(index1To4 - 1, target);
            return this;
        }
        public Builder choiceTarget(int index1To4, int target) {
            this.choiceTargets.set(index1To4 - 1, target);
            return this;
        }
        public Builder clearChoices() {
            for (int i = 0; i < 4; i++) {
                this.choiceTexts.set(i, "");
                this.choiceTargets.set(i, 0);
            }
            return this;
        }
        public Builder category(String value) { this.category = value; return this; }
        public Builder page(int value) { this.page = value; return this; }
        public Builder totalPages(int value) { this.totalPages = value; return this; }
        public Builder selectedTitle(String value) { this.selectedTitle = value; return this; }
        public Builder selectedUuid(String value) { this.selectedUuid = value; return this; }
        public Builder selectedRequirement(String value) { this.selectedRequirement = value; return this; }
        public Builder selectedReward(String value) { this.selectedReward = value; return this; }
        public Builder selectedHigh(boolean value) { this.selectedHigh = value; return this; }
        public Builder detailLine(int index1To8, String value) { this.detailLines.set(index1To8 - 1, value); return this; }
        public Builder row(int index1To8, JournalRow row) { this.rows.set(index1To8 - 1, row); return this; }

        public QuestDialogueSession build() {
            return new QuestDialogueSession(
                    new DialogueState(template, cursor, dialogueBegin, taskBegin, taskSustained, taskTick,
                            choicePending, playerChoice, chatChoiceCursor, questStatus, displayText,
                            List.copyOf(choiceTexts), List.copyOf(choiceTargets)),
                    new JournalView(category, page, totalPages, selectedTitle, selectedUuid, selectedRequirement,
                            selectedReward, selectedHigh, List.copyOf(detailLines), List.copyOf(rows)));
        }
    }
}
