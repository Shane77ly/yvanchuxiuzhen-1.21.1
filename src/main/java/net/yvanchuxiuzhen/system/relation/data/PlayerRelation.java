package net.yvanchuxiuzhen.system.relation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 玩家的关系数据，对应 1.18.2 版本的 {@code team} 字段与 {@code friendlist} 载体物品的标签：
 * 阵营、好友格、待处理的好友申请（{@code zuduiname}、{@code zuduiteamname}）、好友界面的页码与选中行。
 * 界面行 {@link #rows()} 只同步不存档。对象可变，修改后由调用方 {@code syncData} 同步给本人。
 */
public final class PlayerRelation {
    private static final Codec<List<FriendEntry>> FRIENDS = FriendEntry.CODEC.listOf();
    public static final Codec<PlayerRelation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Allegiance.CODEC.fieldOf("allegiance").forGetter(PlayerRelation::allegiance),
            FRIENDS.fieldOf("friends").forGetter(relation -> relation.friends),
            Codec.STRING.fieldOf("request_name").forGetter(PlayerRelation::requestName),
            Codec.STRING.fieldOf("request_team").forGetter(PlayerRelation::requestTeam),
            Codec.INT.fieldOf("page").forGetter(PlayerRelation::page),
            Codec.INT.fieldOf("selected").forGetter(PlayerRelation::selected)
    ).apply(instance, (allegiance, friends, requestName, requestTeam, page, selected) ->
            new PlayerRelation(allegiance, friends, requestName, requestTeam, page, selected, List.of())));
    private static final Codec<PlayerRelation> SYNC_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CODEC.fieldOf("data").forGetter(relation -> relation),
            FriendRow.CODEC.listOf().fieldOf("rows").forGetter(PlayerRelation::rows)
    ).apply(instance, (data, rows) -> new PlayerRelation(data.allegiance, data.friends, data.requestName, data.requestTeam,
            data.page, data.selected, rows)));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerRelation> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(SYNC_CODEC);

    private final Allegiance allegiance;
    private final List<FriendEntry> friends;
    private String requestName;
    private String requestTeam;
    private int page;
    private int selected;
    private List<FriendRow> rows;

    public PlayerRelation() {
        this(new Allegiance(), List.of(), "", "", 0, 0, List.of());
    }

    private PlayerRelation(Allegiance allegiance, List<FriendEntry> friends, String requestName, String requestTeam,
                           int page, int selected, List<FriendRow> rows) {
        this.allegiance = allegiance;
        this.friends = new ArrayList<>(friends);
        this.requestName = requestName;
        this.requestTeam = requestTeam;
        this.page = page;
        this.selected = selected;
        this.rows = List.copyOf(rows);
    }

    public Allegiance allegiance() {
        return allegiance;
    }

    /** 好友数（来源 {@code friendnumber}），含已删除的格。 */
    public int friendCount() {
        return friends.size();
    }

    /** 第 {@code slot} 格（从 1 起）；超出范围时为 null，对应来源读到的空字符串与 false。 */
    public FriendEntry friend(int slot) {
        return slot >= 1 && slot <= friends.size() ? friends.get(slot - 1) : null;
    }

    public void addFriend(String name, String team) {
        friends.add(new FriendEntry(name, team, false, false));
    }

    /** 整体换成另一名玩家的好友格与友方集合（来源删除好友时把本人列表写入对方的分支）。 */
    public void copyFriendsFrom(PlayerRelation other) {
        List<FriendEntry> copied = other.friends.stream().map(FriendEntry::copy).toList();
        friends.clear();
        friends.addAll(copied);
        String team = allegiance.team();
        allegiance.copyFrom(other.allegiance);
        allegiance.setTeam(team);
    }

    public String requestName() {
        return requestName;
    }

    public String requestTeam() {
        return requestTeam;
    }

    public void setRequest(String name, String team) {
        requestName = name;
        requestTeam = team;
    }

    public int page() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    /** 选中的界面行（1–10），0 为未选中（来源 {@code displayxvanzenumber}）。 */
    public int selected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public List<FriendRow> rows() {
        return rows;
    }

    /** 第 {@code index} 行（从 1 起）；没有该行时为空行。 */
    public FriendRow row(int index) {
        return index >= 1 && index <= rows.size() ? rows.get(index - 1) : FriendRow.EMPTY;
    }

    public void setRows(List<FriendRow> rows) {
        this.rows = List.copyOf(rows);
    }
}
