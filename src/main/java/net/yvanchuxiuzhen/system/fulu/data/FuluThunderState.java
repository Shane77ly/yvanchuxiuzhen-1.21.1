package net.yvanchuxiuzhen.system.fulu.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 惊庭五雷符在受击实体上保存的施放者信息、当前雷轮阶段与计数。 */
public record FuluThunderState(String masterUuid, String masterTeam, String phase, double tick, double count) {
    public static final FuluThunderState EMPTY = new FuluThunderState("", "", "", 0, 0);
    public static final Codec<FuluThunderState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("master_uuid", "").forGetter(FuluThunderState::masterUuid),
            Codec.STRING.optionalFieldOf("master_team", "").forGetter(FuluThunderState::masterTeam),
            Codec.STRING.optionalFieldOf("phase", "").forGetter(FuluThunderState::phase),
            Codec.DOUBLE.optionalFieldOf("tick", 0.0).forGetter(FuluThunderState::tick),
            Codec.DOUBLE.optionalFieldOf("count", 0.0).forGetter(FuluThunderState::count)
    ).apply(instance, FuluThunderState::new));

    public FuluThunderState {
        masterUuid = masterUuid == null ? "" : masterUuid;
        masterTeam = masterTeam == null ? "" : masterTeam;
        phase = phase == null ? "" : phase;
    }

    public FuluThunderState withMaster(String uuid, String team) {
        return new FuluThunderState(uuid, team, phase, tick, count);
    }

    public FuluThunderState withProgress(String nextPhase, double nextTick, double nextCount) {
        return new FuluThunderState(masterUuid, masterTeam, nextPhase, nextTick, nextCount);
    }
}
