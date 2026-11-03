package net.yvanchuxiuzhen.system.relation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.yvanchuxiuzhen.system.relation.content.TeamRequestMenu;
import net.yvanchuxiuzhen.system.relation.data.FriendEntry;
import net.yvanchuxiuzhen.system.relation.data.PlayerRelation;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;

/**
 * 好友申请（来源 {@code Ling15RightClickEntity}、{@code ZuduiteamYes}、{@code ZuduiteamNo}）。
 * 申请者主手持团队招募令右键另一名玩家；对方收到申请界面，同意后双方各在列表末尾加一格并把对方团队加入友方集合。
 * 来源的重复判定写错了比较对象（Q19），按来源保留：申请时打印循环次数与逐格名字，同意时可能重复添加。
 */
public final class TeamRequest {
    /** 按钮编号与来源 {@code ZuduiteamGuiButtonMessage} 一致：0 同意，1 拒绝。 */
    public static final int BUTTONS = 2;

    private TeamRequest() {
    }

    public static void request(ServerPlayer source, ServerPlayer target) {
        PlayerRelation relation = source.getData(RelationAttachments.PLAYER);
        String targetName = target.getDisplayName().getString();
        double count = relation.friendCount();
        boolean already = false;
        if (count >= 1) {
            source.displayClientMessage(Component.literal("循环：" + count), false);
            for (int slot = 1; slot <= relation.friendCount(); slot++) {
                FriendEntry entry = relation.friend(slot);
                source.displayClientMessage(Component.literal("名字：" + entry.name()), false);
                if (targetName.equals(entry.name()) && !entry.black()) {
                    already = true;
                    source.displayClientMessage(Component.literal("不可以"), false);
                    break;
                }
            }
        }
        if (already) {
            source.displayClientMessage(Component.literal(targetName + "已成为您的好友，无需重复申请。"), false);
            return;
        }
        source.displayClientMessage(Component.literal("已向" + targetName + "发送好友申请。"), false);
        target.getData(RelationAttachments.PLAYER).setRequest(source.getDisplayName().getString(), relation.allegiance().team());
        target.syncData(RelationAttachments.PLAYER);
        target.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new TeamRequestMenu(id, inventory), Component.literal("ZuduiteamGui")));
    }

    public static void press(ServerPlayer player, int button) {
        switch (button) {
            case 0 -> accept(player);
            case 1 -> {
                player.getData(RelationAttachments.PLAYER).setRequest("", "");
                player.syncData(RelationAttachments.PLAYER);
            }
            default -> throw new IllegalArgumentException("好友申请界面没有按钮 " + button);
        }
        player.closeContainer();
    }

    /**
     * 同意申请。申请者须在本人所在维度在线；申请者一侧的重复判定比较的是申请者自己的名字，
     * 本人一侧的重复判定按申请者的好友数遍历本人的列表（来源如此）。
     */
    private static void accept(ServerPlayer player) {
        PlayerRelation relation = player.getData(RelationAttachments.PLAYER);
        String requestName = relation.requestName();
        String requestTeam = relation.requestTeam();
        String myName = player.getDisplayName().getString();
        String myTeam = relation.allegiance().team();
        int requesterCount = 0;
        boolean online = false;
        for (ServerPlayer requester : player.serverLevel().players()) {
            String requesterName = requester.getDisplayName().getString();
            if (requesterName.equals(requestName)) {
                PlayerRelation other = requester.getData(RelationAttachments.PLAYER);
                requesterCount = other.friendCount();
                if (!listed(other, requesterName, requesterCount)) {
                    other.addFriend(myName, myTeam);
                    other.allegiance().setFriendly(myTeam, true);
                    requester.syncData(RelationAttachments.PLAYER);
                }
                requester.displayClientMessage(Component.literal("§6成功添加" + myName + "为好友！"), false);
                online = true;
                break;
            }
        }
        if (!online) {
            player.displayClientMessage(Component.literal("§4对方已下线或不在此位面，无法添加好友！"), false);
            return;
        }
        if (!listed(relation, requestName, requesterCount)) {
            relation.addFriend(requestName, requestTeam);
            relation.allegiance().setFriendly(requestTeam, true);
            relation.setRequest("", "");
            player.syncData(RelationAttachments.PLAYER);
        }
        player.displayClientMessage(Component.literal("§6成功添加" + requestName + "为好友！"), false);
    }

    /** 列表前 {@code scanned} 格中有未删除的 {@code name}。 */
    private static boolean listed(PlayerRelation relation, String name, int scanned) {
        for (int slot = 1; slot <= scanned; slot++) {
            FriendEntry entry = relation.friend(slot);
            if (entry != null && entry.name().equals(name) && !entry.black()) {
                return true;
            }
        }
        return false;
    }
}
