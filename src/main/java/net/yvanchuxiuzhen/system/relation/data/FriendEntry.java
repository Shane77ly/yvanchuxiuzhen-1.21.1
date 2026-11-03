package net.yvanchuxiuzhen.system.relation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 好友列表的一格（来源 {@code friendname<n>}、{@code friendteamname<n>}、{@code friendstar<n>}、{@code friendblack<n>}）。
 * 删除好友只把该格标为 {@code black}，格子保留，好友数不减少。
 */
public final class FriendEntry {
    public static final Codec<FriendEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(FriendEntry::name),
            Codec.STRING.fieldOf("team").forGetter(FriendEntry::team),
            Codec.BOOL.fieldOf("star").forGetter(FriendEntry::star),
            Codec.BOOL.fieldOf("black").forGetter(FriendEntry::black)
    ).apply(instance, FriendEntry::new));

    private String name;
    private String team;
    private boolean star;
    private boolean black;

    public FriendEntry(String name, String team, boolean star, boolean black) {
        this.name = name;
        this.team = team;
        this.star = star;
        this.black = black;
    }

    public FriendEntry copy() {
        return new FriendEntry(name, team, star, black);
    }

    /** 与另一格交换全部内容（排列上移、下移）。 */
    public void swap(FriendEntry other) {
        FriendEntry kept = copy();
        name = other.name;
        team = other.team;
        star = other.star;
        black = other.black;
        other.name = kept.name;
        other.team = kept.team;
        other.star = kept.star;
        other.black = kept.black;
    }

    public String name() {
        return name;
    }

    public String team() {
        return team;
    }

    public boolean star() {
        return star;
    }

    public boolean black() {
        return black;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public void setBlack(boolean black) {
        this.black = black;
    }
}
