package net.yvanchuxiuzhen.system.quest.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 对话与任务模板目录（装载自 {@code content/quest/dialogues.json}），供 NPC、伪装方块与出生道具共用。 */
public final class QuestTemplates {
    private static Map<String, QuestTemplateDefinition> templates;

    private QuestTemplates() {
    }

    public static Optional<QuestTemplateDefinition> find(String id) {
        if (id == null || id.isEmpty()) return Optional.empty();
        return Optional.ofNullable(all().get(id));
    }

    public static QuestTemplateDefinition require(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("未知对话模板：" + id));
    }

    public static List<QuestTemplateDefinition> allList() {
        return List.copyOf(all().values());
    }

    private static Map<String, QuestTemplateDefinition> all() {
        if (templates == null) {
            Map<String, QuestTemplateDefinition> loaded = new LinkedHashMap<>();
            for (QuestTemplateDefinition entry : ContentFiles.read("content/quest/dialogues.json", QuestTemplateDefinition.CODEC)) {
                loaded.put(entry.id(), entry);
            }
            templates = Map.copyOf(loaded);
        }
        return templates;
    }
}
