package net.yvanchuxiuzhen.system.relation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;

/**
 * 实体的阵营：所属团队名与视为友方的团队名集合（来源实体 NBT {@code team} 与 {@code friendlist} 中值为 true 的键）。
 * 玩家的阵营在 {@link PlayerRelation} 中，其他实体单独作为附件保存，不同步。
 */
public final class Allegiance {
    public static final Codec<Allegiance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("team").forGetter(Allegiance::team),
            Codec.STRING.listOf().fieldOf("friendly_teams").forGetter(allegiance -> allegiance.friendlyTeams.getAllKeys().stream()
                    .filter(name -> allegiance.friendlyTeams.contains(name, 1) && allegiance.friendlyTeams.getBoolean(name)).toList()),
            CompoundTag.CODEC.optionalFieldOf("friend_data", new CompoundTag()).forGetter(allegiance -> allegiance.friendlyTeams),
            Codec.BOOL.optionalFieldOf("friend_data_present", true).forGetter(allegiance -> allegiance.friendDataPresent)
    ).apply(instance, Allegiance::new));

    private String team;
    private CompoundTag friendlyTeams;
    private boolean friendDataPresent;

    public Allegiance() {
        this("", List.of(), new CompoundTag(), false);
    }

    private Allegiance(String team, List<String> friendlyTeams, CompoundTag friendData, boolean friendDataPresent) {
        this.team = team;
        this.friendlyTeams = friendData.copy();
        this.friendDataPresent = friendDataPresent || !friendlyTeams.isEmpty();
        friendlyTeams.forEach(name -> this.friendlyTeams.putBoolean(name, true));
    }

    public boolean hasFriendKey(String key) {
        return friendlyTeams.contains(key);
    }

    public int friendInt(String key) {
        return friendlyTeams.getInt(key);
    }

    public void setFriendInt(String key, int value) {
        friendDataPresent = true;
        friendlyTeams.putInt(key, value);
    }

    public String friendString(String key) {
        return friendlyTeams.getString(key);
    }

    public void setFriendString(String key, String value) {
        friendDataPresent = true;
        friendlyTeams.putString(key, value);
    }

    public double friendDouble(String key) {
        return friendlyTeams.getDouble(key);
    }

    public void setFriendDouble(String key, double value) {
        friendDataPresent = true;
        friendlyTeams.putDouble(key, value);
    }

    public boolean friendBoolean(String key) {
        return friendlyTeams.getBoolean(key);
    }

    public void setFriendBoolean(String key, boolean value) {
        friendDataPresent = true;
        friendlyTeams.putBoolean(key, value);
    }

    public String team() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    /** 整体替换集合并断开既有共享引用；驯服时使用目标能力好友表的独立副本。 */
    public void replace(String team, Set<String> friends) {
        this.team = team;
        friendlyTeams = new CompoundTag();
        friendDataPresent = true;
        friends.forEach(name -> friendlyTeams.putBoolean(name, true));
    }

    public boolean isFriendly(String otherTeam) {
        return friendlyTeams.getBoolean(otherTeam);
    }

    public Set<String> friendlyTeams() {
        CompoundTag shared = friendlyTeams;
        return Collections.unmodifiableSet(new AbstractSet<>() {
            @Override
            public Iterator<String> iterator() {
                return shared.getAllKeys().stream().filter(shared::getBoolean).iterator();
            }

            @Override
            public int size() {
                return (int) shared.getAllKeys().stream().filter(shared::getBoolean).count();
            }

            @Override
            public boolean contains(Object value) {
                return value instanceof String name && shared.getBoolean(name);
            }
        });
    }

    public void setFriendly(String otherTeam, boolean friendly) {
        friendDataPresent = true;
        if (friendly) {
            friendlyTeams.putBoolean(otherTeam, true);
        } else {
            friendlyTeams.remove(otherTeam);
        }
    }

    /** 整体换成另一个阵营的团队与友方集合（来源召唤物复制施放者的 team 与 friendlist）。 */
    public void copyFrom(Allegiance other) {
        team = other.team;
        friendlyTeams = other.friendlyTeams.copy();
        friendDataPresent = other.friendDataPresent;
    }

    /** 来源直接赋入同一个friendlist子标签；运行期共享修改，独立解码后各自持有集合。 */
    public void shareFrom(Allegiance other) {
        team = other.team;
        friendlyTeams = other.friendlyTeams;
        friendDataPresent = other.friendDataPresent;
    }

    /** 来源读取缺失子标签时取得独立临时表，再把该表赋给新实体。 */
    public void shareSourceFrom(Allegiance other) {
        team = other.team;
        friendlyTeams = other.friendDataPresent ? other.friendlyTeams : new CompoundTag();
        friendDataPresent = true;
    }

    /** 返回所有视为友方的团队名列表快照。 */
    public List<String> friendlyTeamList() {
        return friendlyTeams.getAllKeys().stream().filter(friendlyTeams::getBoolean).toList();
    }
}
