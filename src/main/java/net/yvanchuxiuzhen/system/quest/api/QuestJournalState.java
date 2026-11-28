package net.yvanchuxiuzhen.system.quest.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 玩家任务目录强类型存档，按接取顺序索引全部已接与已完结任务。 */
public record QuestJournalState(List<String> questIds, Map<String, QuestJournalEntry> entries) {
    public static final QuestJournalState EMPTY = new QuestJournalState(List.of(), Map.of());

    public static final Codec<QuestJournalState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("quest_ids").forGetter(QuestJournalState::questIds),
            Codec.unboundedMap(Codec.STRING, QuestJournalEntry.CODEC).fieldOf("entries").forGetter(QuestJournalState::entries)
    ).apply(instance, QuestJournalState::new));

    public static final StreamCodec<ByteBuf, QuestJournalState> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean contains(String questId) {
        return entries.containsKey(questId);
    }

    public QuestJournalEntry entry(String questId) {
        return entries.get(questId);
    }

    public String status(String questId) {
        QuestJournalEntry entry = entries.get(questId);
        return entry == null ? "" : entry.status();
    }

    public int progress(String questId) {
        QuestJournalEntry entry = entries.get(questId);
        return entry == null ? 0 : entry.progress();
    }

    public String title(String questId) {
        QuestJournalEntry entry = entries.get(questId);
        return entry == null ? "" : entry.title();
    }

    public QuestJournalState withEntry(String questId, QuestJournalEntry entry) {
        List<String> updatedIds = new ArrayList<>(questIds);
        if (!entries.containsKey(questId)) updatedIds.add(questId);
        Map<String, QuestJournalEntry> updatedEntries = new LinkedHashMap<>(entries);
        updatedEntries.put(questId, entry);
        return new QuestJournalState(List.copyOf(updatedIds), Map.copyOf(updatedEntries));
    }

    public QuestJournalState withStatus(String questId, String status) {
        QuestJournalEntry existing = entries.get(questId);
        QuestJournalEntry updated = existing == null
                ? new QuestJournalEntry("", questId, "", 0, status, "", "", "", Optional.empty(), false)
                : new QuestJournalEntry(existing.title(), existing.uuid(), existing.description(), existing.progress(),
                        status, existing.requirement(), existing.rewardSummary(), existing.templateId(),
                        existing.template(), existing.highQuest());
        return withEntry(questId, updated);
    }

    public record QuestJournalEntry(
            String title,
            String uuid,
            String description,
            int progress,
            String status,
            String requirement,
            String rewardSummary,
            String templateId,
            Optional<QuestTemplateDefinition> template,
            boolean highQuest) {
        public static final Codec<QuestJournalEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("title").forGetter(QuestJournalEntry::title),
                Codec.STRING.fieldOf("uuid").forGetter(QuestJournalEntry::uuid),
                Codec.STRING.fieldOf("description").forGetter(QuestJournalEntry::description),
                Codec.INT.fieldOf("progress").forGetter(QuestJournalEntry::progress),
                Codec.STRING.fieldOf("status").forGetter(QuestJournalEntry::status),
                Codec.STRING.fieldOf("requirement").forGetter(QuestJournalEntry::requirement),
                Codec.STRING.fieldOf("reward_summary").forGetter(QuestJournalEntry::rewardSummary),
                Codec.STRING.fieldOf("template_id").forGetter(QuestJournalEntry::templateId),
                QuestTemplateDefinition.CODEC.optionalFieldOf("template").forGetter(QuestJournalEntry::template),
                Codec.BOOL.fieldOf("high_quest").forGetter(QuestJournalEntry::highQuest)
        ).apply(instance, QuestJournalEntry::new));
    }
}
