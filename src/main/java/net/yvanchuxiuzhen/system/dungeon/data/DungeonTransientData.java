package net.yvanchuxiuzhen.system.dungeon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 玩家副本瞬态标识（当前传送门冷却防重入、等待回程同步、入场前模式快照），不随死亡复制。 */
public record DungeonTransientData(boolean portalOpened, boolean pendingReturn, String previousGameMode) {
    public static final DungeonTransientData EMPTY = new DungeonTransientData(false, false, "");

    public static final Codec<DungeonTransientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("portal_opened", false).forGetter(DungeonTransientData::portalOpened),
            Codec.BOOL.optionalFieldOf("pending_return", false).forGetter(DungeonTransientData::pendingReturn),
            Codec.STRING.optionalFieldOf("previous_game_mode", "").forGetter(DungeonTransientData::previousGameMode)
    ).apply(instance, DungeonTransientData::new));

    public DungeonTransientData withPortalOpened(boolean value) {
        return new DungeonTransientData(value, pendingReturn, previousGameMode);
    }

    public DungeonTransientData withPendingReturn(boolean value) {
        return new DungeonTransientData(portalOpened, value, previousGameMode);
    }

    public DungeonTransientData withPreviousGameMode(String value) {
        return new DungeonTransientData(portalOpened, pendingReturn, value);
    }
}
