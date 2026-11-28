package net.yvanchuxiuzhen.system.quest;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;

/** 结构释放后的任务方块配置，从 {@code content/quest/structure_presets.json} 装载。 */
final class QuestStructureData {
    private static List<QuestStructurePreset> presets;

    private QuestStructureData() {
    }

    static void apply(ServerLevel level, String structureName) {
        if (presets == null) {
            presets = ContentFiles.read("content/quest/structure_presets.json", QuestStructurePreset.CODEC);
        }
        for (QuestStructurePreset preset : presets) {
            if (!preset.structureName().equals(structureName)) continue;
            if (!(level.getBlockEntity(preset.pos()) instanceof DisguisedQuestBlockEntity block)) continue;
            preset.displayDirection().ifPresent(block::setDisplayDirection);
            preset.dialogueId().ifPresent(id -> block.setAdvancedQuest(1, id));
            preset.shopCatalogId().ifPresent(id -> block.setShopId(1, id));
            level.sendBlockUpdated(preset.pos(), block.getBlockState(), block.getBlockState(), 3);
        }
    }
}
