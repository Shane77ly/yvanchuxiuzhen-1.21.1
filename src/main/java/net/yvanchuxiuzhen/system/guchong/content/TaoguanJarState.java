package net.yvanchuxiuzhen.system.guchong.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 地上蛊罐强类型状态（UUID、团队、友方团队与释放状态），取代旧版 {@code CompoundTag} 载体。 */
public final class TaoguanJarState {
    public static final Codec<TaoguanJarState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("uuid").forGetter(s -> s.uuid),
            Codec.STRING.fieldOf("team").forGetter(s -> s.team),
            Codec.BOOL.fieldOf("released").forGetter(s -> s.released),
            Codec.INT.fieldOf("released_count").forGetter(s -> s.releasedCount),
            Codec.STRING.listOf().fieldOf("friend_teams").forGetter(s -> List.copyOf(s.friendTeams))
    ).apply(instance, TaoguanJarState::new));

    public String uuid = "";
    public String team = "";
    public boolean released;
    public int releasedCount;
    public final List<String> friendTeams = new ArrayList<>();

    public TaoguanJarState() {
    }

    private TaoguanJarState(String uuid, String team, boolean released, int releasedCount, List<String> friendTeams) {
        this.uuid = uuid;
        this.team = team;
        this.released = released;
        this.releasedCount = releasedCount;
        this.friendTeams.addAll(friendTeams);
    }

    public boolean isEmpty() {
        return uuid.isEmpty() && team.isEmpty() && !released && releasedCount == 0 && friendTeams.isEmpty();
    }

    public void decrementReleased() {
        releasedCount--;
        if (releasedCount < 1) {
            releasedCount = 0;
            released = false;
        }
    }

    public TaoguanJarState copy() {
        return new TaoguanJarState(uuid, team, released, releasedCount, friendTeams);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TaoguanJarState other)) return false;
        return released == other.released && releasedCount == other.releasedCount
                && uuid.equals(other.uuid) && team.equals(other.team) && friendTeams.equals(other.friendTeams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, team, released, releasedCount, friendTeams);
    }
}
