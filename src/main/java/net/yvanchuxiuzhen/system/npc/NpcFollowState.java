package net.yvanchuxiuzhen.system.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 剧情实体跟随玩家的状态与 20 刻寻路计数。 */
public record NpcFollowState(boolean following, String playerUuid, int tick) {
    public static final NpcFollowState EMPTY = new NpcFollowState(false, "", 0);
    public static final Codec<NpcFollowState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("following", false).forGetter(NpcFollowState::following),
            Codec.STRING.optionalFieldOf("player_uuid", "").forGetter(NpcFollowState::playerUuid),
            Codec.INT.optionalFieldOf("tick", 0).forGetter(NpcFollowState::tick)
    ).apply(instance, NpcFollowState::new));

    public NpcFollowState {
        playerUuid = playerUuid == null ? "" : playerUuid;
    }

    public NpcFollowState withTick(int nextTick) {
        return new NpcFollowState(following, playerUuid, nextTick);
    }
}
