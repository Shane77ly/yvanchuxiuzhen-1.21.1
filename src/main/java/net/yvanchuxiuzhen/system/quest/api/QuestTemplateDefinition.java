package net.yvanchuxiuzhen.system.quest.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

/**
 * 强类型对话与任务模板定义，彻底替代旧版 {@code title/questId/...%step1%step2} 分隔符脚本。
 * 节点索引采用 1-based（{@code nodes.get(cursor - 1)}）以与来源跳转序号一一对应。
 */
public record QuestTemplateDefinition(String id,
                                      String questId,
                                      String title,
                                      String description,
                                      int initialProgress,
                                      String initialStatus,
                                      String requirement,
                                      String rewardSummary,
                                      List<DialogueNodeDefinition> nodes) {
    public static final Codec<QuestTemplateDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(QuestTemplateDefinition::id),
            Codec.STRING.fieldOf("quest_id").forGetter(QuestTemplateDefinition::questId),
            Codec.STRING.fieldOf("title").forGetter(QuestTemplateDefinition::title),
            Codec.STRING.fieldOf("description").forGetter(QuestTemplateDefinition::description),
            Codec.INT.fieldOf("initial_progress").forGetter(QuestTemplateDefinition::initialProgress),
            Codec.STRING.fieldOf("initial_status").forGetter(QuestTemplateDefinition::initialStatus),
            Codec.STRING.fieldOf("requirement").forGetter(QuestTemplateDefinition::requirement),
            Codec.STRING.fieldOf("reward_summary").forGetter(QuestTemplateDefinition::rewardSummary),
            DialogueNodeDefinition.CODEC.listOf().fieldOf("nodes").forGetter(QuestTemplateDefinition::nodes)
    ).apply(instance, QuestTemplateDefinition::new));

    public QuestTemplateDefinition {
        id = id == null ? "" : id;
        questId = questId == null ? "" : questId;
        title = title == null ? "" : title;
        description = description == null ? "" : description;
        initialStatus = initialStatus == null ? "未完成" : initialStatus;
        requirement = requirement == null ? "无" : requirement;
        rewardSummary = rewardSummary == null ? "无" : rewardSummary;
        nodes = List.copyOf(nodes);
    }

    /** 读取 1-based 对话或动作节点；越界时返回空。 */
    public Optional<DialogueNodeDefinition> node(int cursor) {
        int index = cursor - 1;
        return index >= 0 && index < nodes.size() ? Optional.of(nodes.get(index)) : Optional.empty();
    }

    public QuestTemplateDefinition withHeader(String nextTitle, String nextDescription) {
        return new QuestTemplateDefinition(id, questId, nextTitle, nextDescription,
                initialProgress, initialStatus, requirement, rewardSummary, nodes);
    }
}
