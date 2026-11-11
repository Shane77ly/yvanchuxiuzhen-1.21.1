package net.yvanchuxiuzhen.system.relation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 实体的阵营：所属团队名与视为友方的团队名集合（来源实体 NBT {@code team} 与 {@code friendlist} 中值为 true 的键）。
 * 玩家的阵营在 {@link PlayerRelation} 中，其他实体单独作为附件保存，不同步。
 */
public final class Allegiance {
    public static final Codec<Allegiance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("team").forGetter(Allegiance::team),
            Codec.STRING.listOf().fieldOf("friendly_teams").forGetter(allegiance -> List.copyOf(allegiance.friendlyTeams))
    ).apply(instance, Allegiance::new));

    private String team;
    private Set<String> friendlyTeams;

    public Allegiance() {
        this("", List.of());
    }

    private Allegiance(String team, List<String> friendlyTeams) {
        this.team = team;
        this.friendlyTeams = new LinkedHashSet<>(friendlyTeams);
    }

    public String team() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public boolean isFriendly(String otherTeam) {
        return friendlyTeams.contains(otherTeam);
    }

    public Set<String> friendlyTeams() {
        return Collections.unmodifiableSet(friendlyTeams);
    }

    public void setFriendly(String otherTeam, boolean friendly) {
        if (friendly) {
            friendlyTeams.add(otherTeam);
        } else {
            friendlyTeams.remove(otherTeam);
        }
    }

    /** 整体换成另一个阵营的团队与友方集合（来源召唤物复制施放者的 team 与 friendlist）。 */
    public void copyFrom(Allegiance other) {
        team = other.team;
        friendlyTeams = new LinkedHashSet<>(other.friendlyTeams);
    }

    /** 来源直接赋入同一个friendlist子标签；运行期共享修改，独立解码后各自持有集合。 */
    public void shareFrom(Allegiance other) {
        team = other.team;
        friendlyTeams = other.friendlyTeams;
    }
}
