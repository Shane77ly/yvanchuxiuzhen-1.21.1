package net.yvanchuxiuzhen.system.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

/** 结构释放后对 {@code renwuweizhuangfangkuai} 写入的强类型任务与商店预设。 */
record QuestStructurePreset(String structureName,
                                   BlockPos pos,
                                   Optional<String> displayDirection,
                                   Optional<String> dialogueId,
                                   Optional<String> shopCatalogId) {
    static final Codec<QuestStructurePreset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("structure_name").forGetter(QuestStructurePreset::structureName),
            BlockPos.CODEC.fieldOf("pos").forGetter(QuestStructurePreset::pos),
            Codec.STRING.optionalFieldOf("display_direction").forGetter(QuestStructurePreset::displayDirection),
            Codec.STRING.optionalFieldOf("dialogue_id").forGetter(QuestStructurePreset::dialogueId),
            Codec.STRING.optionalFieldOf("shop_catalog_id").forGetter(QuestStructurePreset::shopCatalogId)
    ).apply(instance, QuestStructurePreset::new));
}
