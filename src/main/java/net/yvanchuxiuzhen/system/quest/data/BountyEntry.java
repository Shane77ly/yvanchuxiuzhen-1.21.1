package net.yvanchuxiuzhen.system.quest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 悬赏告示牌单行悬赏目标与追踪状态强类型快照。 */
public record BountyEntry(
        String entityName,
        String entityId,
        int levelNumber,
        int entityNum,
        int weizhiX,
        int weizhiY,
        int weizhiZ,
        int zuobiaoX,
        int zuobiaoY,
        int zuobiaoZ,
        int distance,
        int rewardMoney,
        boolean tracking,
        String trackerUuid) {

    public static final BountyEntry EMPTY = new BountyEntry("", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, "");

    public static final Codec<BountyEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("entity_name").forGetter(BountyEntry::entityName),
            Codec.STRING.fieldOf("entity_id").forGetter(BountyEntry::entityId),
            Codec.INT.fieldOf("level").forGetter(BountyEntry::levelNumber),
            Codec.INT.fieldOf("entity_num").forGetter(BountyEntry::entityNum),
            Codec.INT.fieldOf("offset_x").forGetter(BountyEntry::weizhiX),
            Codec.INT.fieldOf("offset_y").forGetter(BountyEntry::weizhiY),
            Codec.INT.fieldOf("offset_z").forGetter(BountyEntry::weizhiZ),
            Codec.INT.fieldOf("target_x").forGetter(BountyEntry::zuobiaoX),
            Codec.INT.fieldOf("target_y").forGetter(BountyEntry::zuobiaoY),
            Codec.INT.fieldOf("target_z").forGetter(BountyEntry::zuobiaoZ),
            Codec.INT.fieldOf("distance").forGetter(BountyEntry::distance),
            Codec.INT.fieldOf("reward_money").forGetter(BountyEntry::rewardMoney),
            Codec.BOOL.fieldOf("tracking").forGetter(BountyEntry::tracking),
            Codec.STRING.fieldOf("tracker_uuid").forGetter(BountyEntry::trackerUuid)
    ).apply(instance, BountyEntry::new));

    public BountyEntry withTracking(boolean tracking, String uuid) {
        return new BountyEntry(entityName, entityId, levelNumber, entityNum, weizhiX, weizhiY, weizhiZ,
                zuobiaoX, zuobiaoY, zuobiaoZ, distance, rewardMoney, tracking, uuid);
    }

    public BountyEntry withTracking(boolean tracking) {
        return withTracking(tracking, trackerUuid);
    }
}
