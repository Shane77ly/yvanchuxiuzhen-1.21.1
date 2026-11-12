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
            Codec.STRING.listOf().fieldOf("friendly_teams").forGetter(allegiance -> List.copyOf(allegiance.friendlyTeams()))
    ).apply(instance, Allegiance::new));

    private String team;
    private CompoundTag friendlyTeams;

    public Allegiance() {
        this("", List.of());
    }

    private Allegiance(String team, List<String> friendlyTeams) {
        this.team = team;
        this.friendlyTeams = new CompoundTag();
        friendlyTeams.forEach(name -> this.friendlyTeams.putBoolean(name, true));
    }

    public String team() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
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
    }

    /** 来源直接赋入同一个friendlist子标签；运行期共享修改，独立解码后各自持有集合。 */
    public void shareFrom(Allegiance other) {
        team = other.team;
        friendlyTeams = other.friendlyTeams;
    }

    /** 蛊罐放虫只共享方块的友方子标签，虫的团队仍取其物品实例字段。 */
    public void shareFromBlock(CompoundTag blockData, String insectTeam) {
        team = insectTeam;
        friendlyTeams = blockData.getCompound("friendlist");
    }

    /** 方块接收主人友方标签的同一引用，保留来源放置后好友修改的可见性。 */
    public void shareToBlock(CompoundTag blockData) {
        blockData.put("friendlist", friendlyTeams);
    }
}
