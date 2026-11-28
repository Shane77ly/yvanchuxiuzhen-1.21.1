package net.yvanchuxiuzhen.system.yihuo.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 实体身上附着的异火攻击来源与阴阳灼烧计时。 */
public final class YihuoBurnState {
    public static final Codec<YihuoBurnState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("fireteam", "").forGetter(YihuoBurnState::fireTeam),
            Codec.STRING.optionalFieldOf("fire_attack_uuid", "").forGetter(YihuoBurnState::fireAttackUuid),
            Codec.DOUBLE.optionalFieldOf("yanghuotick", 0.0).forGetter(YihuoBurnState::yangTick),
            Codec.DOUBLE.optionalFieldOf("yinhuotick", 0.0).forGetter(YihuoBurnState::yinTick)
    ).apply(instance, YihuoBurnState::new));

    private String fireTeam = "";
    private String fireAttackUuid = "";
    private double yangTick;
    private double yinTick;
    private double yangClientTick;

    public YihuoBurnState() {
    }

    public YihuoBurnState(String fireTeam, String fireAttackUuid, double yangTick, double yinTick) {
        this.fireTeam = fireTeam == null ? "" : fireTeam;
        this.fireAttackUuid = fireAttackUuid == null ? "" : fireAttackUuid;
        this.yangTick = yangTick;
        this.yinTick = yinTick;
    }

    public String fireTeam() { return fireTeam; }
    public String fireAttackUuid() { return fireAttackUuid; }
    public double yangTick() { return yangTick; }
    public double yinTick() { return yinTick; }
    public double yangClientTick() { return yangClientTick; }

    public void setBurnTarget(String team, String attackerUuid) {
        this.fireTeam = team == null ? "" : team;
        this.fireAttackUuid = attackerUuid == null ? "" : attackerUuid;
    }

    public void setYangTick(double value) { this.yangTick = value; }
    public void setYinTick(double value) { this.yinTick = value; }
    public void setYangClientTick(double value) { this.yangClientTick = value; }
}
