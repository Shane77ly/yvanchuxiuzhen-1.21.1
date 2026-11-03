package net.yvanchuxiuzhen.system.relation;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.relation.content.FriendMenu;
import net.yvanchuxiuzhen.system.relation.data.FriendEntry;
import net.yvanchuxiuzhen.system.relation.data.FriendRow;
import net.yvanchuxiuzhen.system.relation.data.PlayerRelation;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;

/**
 * 好友界面的服务端操作（来源 {@code Friendshuaxin}、{@code Playeruixvanze<n>}、{@code PlayeruiPageup/down}、
 * {@code Playershoucang}、{@code Playeruipailieup/down}、{@code Playeruidel}）。每页 10 格，
 * 刷新时先列出本页有星标的格，再列出本页没有星标且未删除的格（含好友数之外的空格），最多显示 10 行。
 * 在线与属性只在本人所在维度的玩家中按显示名查找。
 */
public final class FriendList {
    /** 按钮编号与来源 {@code FriendguiButtonMessage} 一致：0–9 选中行，10 上一页，11 下一页，12 星标，13 上移，14 下移，15 删除。 */
    public static final int BUTTONS = 16;
    private static final int PAGE_SIZE = 10;
    /** xiulian 尚未提供境界查询；所有玩家处于来源 {@code LEVELname} 的初值“凡人”。 */
    private static final String REALM = "凡人";

    private FriendList() {
    }

    /** 团队招募令潜行使用：刷新后打开好友界面。 */
    public static void open(ServerPlayer player) {
        refresh(player);
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new FriendMenu(id, inventory), Component.literal("Friendgui")));
    }

    public static void press(ServerPlayer player, int button) {
        PlayerRelation relation = player.getData(RelationAttachments.PLAYER);
        if (button < PAGE_SIZE) {
            relation.setSelected(button + 1);
            player.syncData(RelationAttachments.PLAYER);
            return;
        }
        switch (button) {
            case 10 -> relation.setPage(relation.page() > 0 ? relation.page() - 1 : pageCount(relation) - 1);
            case 11 -> relation.setPage(relation.page() < pageCount(relation) - 1 ? relation.page() + 1 : 0);
            case 12 -> {
                FriendEntry entry = selectedEntry(relation);
                if (entry == null) {
                    return;
                }
                entry.setStar(!entry.star());
            }
            case 13 -> {
                FriendRow row = relation.row(relation.selected());
                if (row.name().isEmpty() || row.slot() < 2) {
                    return;
                }
                relation.friend(row.slot()).swap(relation.friend(row.slot() - 1));
            }
            case 14 -> {
                FriendRow row = relation.row(relation.selected());
                if (row.name().isEmpty() || row.slot() >= relation.friendCount()) {
                    return;
                }
                relation.friend(row.slot()).swap(relation.friend(row.slot() + 1));
            }
            case 15 -> {
                if (!delete(player, relation)) {
                    return;
                }
            }
            default -> throw new IllegalArgumentException("好友界面没有按钮 " + button);
        }
        refresh(player);
    }

    /** 总页数 = ⌈好友数 / 10⌉；没有好友时为 0，翻页后页码为 -1（来源如此）。 */
    private static int pageCount(PlayerRelation relation) {
        return (int) Math.ceil(relation.friendCount() / (double) PAGE_SIZE);
    }

    /** 选中行名字非空时对应的好友格。 */
    private static FriendEntry selectedEntry(PlayerRelation relation) {
        FriendRow row = relation.row(relation.selected());
        return row.name().isEmpty() ? null : relation.friend(row.slot());
    }

    /**
     * 删除选中的好友：本人列表中该格去掉星标、标为删除，并把对方团队移出友方集合。
     * 对方在同一维度在线时，来源在本人列表里查找本人的名字（通常不存在）；找到时把那一格同样标为删除，
     * 再把本人的整份列表写给对方（来源 {@code Playeruidel} 读取对象写错，Q19）。选中行为空时返回 false，不刷新。
     */
    private static boolean delete(ServerPlayer player, PlayerRelation relation) {
        FriendRow row = relation.row(relation.selected());
        String myName = player.getDisplayName().getString();
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other.getDisplayName().getString().equals(row.name())) {
                for (int slot = 1; slot <= relation.friendCount(); slot++) {
                    FriendEntry entry = relation.friend(slot);
                    if (entry.name().equals(myName)) {
                        markDeleted(relation, entry);
                        PlayerRelation otherRelation = other.getData(RelationAttachments.PLAYER);
                        otherRelation.copyFriendsFrom(relation);
                        other.syncData(RelationAttachments.PLAYER);
                        break;
                    }
                }
                break;
            }
        }
        if (row.name().isEmpty()) {
            return false;
        }
        markDeleted(relation, relation.friend(row.slot()));
        return true;
    }

    private static void markDeleted(PlayerRelation relation, FriendEntry entry) {
        relation.allegiance().setFriendly(entry.team(), false);
        entry.setStar(false);
        entry.setBlack(true);
    }

    /** 重建本页的显示行；刷新前选中行的名字仍在本页时保持选中，否则取消选中。 */
    public static void refresh(ServerPlayer player) {
        PlayerRelation relation = player.getData(RelationAttachments.PLAYER);
        String selectedName = relation.row(relation.selected()).name();
        relation.setSelected(0);
        List<FriendRow> rows = new ArrayList<>();
        int first = relation.page() * PAGE_SIZE + 1;
        for (int slot = first; slot < first + PAGE_SIZE; slot++) {
            FriendEntry entry = relation.friend(slot);
            if (entry != null && entry.star()) {
                rows.add(row(player, slot, entry));
            }
        }
        for (int slot = first; slot < first + PAGE_SIZE; slot++) {
            FriendEntry entry = relation.friend(slot);
            if (entry == null || !entry.star() && !entry.black()) {
                rows.add(row(player, slot, entry));
            }
        }
        List<FriendRow> shown = rows.subList(0, Math.min(PAGE_SIZE, rows.size()));
        for (int index = 0; index < shown.size(); index++) {
            if (!selectedName.isEmpty() && shown.get(index).name().equals(selectedName)) {
                relation.setSelected(index + 1);
            }
        }
        relation.setRows(shown);
        player.syncData(RelationAttachments.PLAYER);
    }

    private static FriendRow row(ServerPlayer player, int slot, FriendEntry entry) {
        String name = entry == null ? "" : entry.name();
        String team = entry == null ? "" : entry.team();
        boolean star = entry != null && entry.star();
        for (ServerPlayer other : player.serverLevel().players()) {
            String otherName = other.getDisplayName().getString();
            if (otherName.equals(name)) {
                return new FriendRow(name, team, star, slot, true, "(" + REALM + ")" + otherName,
                        "血量上限:" + other.getMaxHealth() + "(恢复:" + PlayerAttributes.value(other, Stat.HEALTH_REC) + ")",
                        "灵力上限:" + PlayerAttributes.value(other, Stat.POWER_MAX) + "(恢复:" + PlayerAttributes.value(other, Stat.POWER_REC) + ")",
                        "精力上限:" + PlayerAttributes.value(other, Stat.SOUL_MAX) + "(恢复:" + PlayerAttributes.value(other, Stat.SOUL_REC) + ")",
                        "物理攻击:" + PlayerAttributes.value(other, Stat.ATTACK) + "(防御:" + PlayerAttributes.value(other, Stat.ATTACK_DEF) + ")",
                        "法术攻击:" + PlayerAttributes.value(other, Stat.MANA_ATT) + "(防御:" + PlayerAttributes.value(other, Stat.MANA_DEF) + ")",
                        "精神攻击:" + PlayerAttributes.value(other, Stat.SOUL_ATT) + "(防御:" + PlayerAttributes.value(other, Stat.SOUL_DEF) + ")");
            }
        }
        return new FriendRow(name, team, star, slot, false, name.isEmpty() ? "" : "(未上线)" + name, "", "", "", "", "", "");
    }
}
