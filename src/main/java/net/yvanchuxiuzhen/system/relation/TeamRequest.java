package net.yvanchuxiuzhen.system.relation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.yvanchuxiuzhen.system.relation.content.TeamRequestMenu;
import net.yvanchuxiuzhen.system.relation.data.FriendEntry;
import net.yvanchuxiuzhen.system.relation.data.PlayerRelation;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;

/**
 * 好友申请（来源 {@code Ling15RightClickEntity}、{@code ZuduiteamYes}、{@code ZuduiteamNo}）。
 * 申请者主手持团队招募令右键另一名玩家；对方收到申请界面，同意后双方各自去重添加好友并把对方团队加入友方集合（Q19）。
 */
public final class TeamRequest {
    /** 按钮编号与来源 {@code ZuduiteamGuiButtonMessage} 一致：0 同意，1 拒绝。 */
    public static final int BUTTONS = 2;

    private TeamRequest() {
    }

    public static void request(ServerPlayer source, ServerPlayer target) {
        PlayerRelation relation = source.getData(RelationAttachments.PLAYER);
        String targetName = target.getDisplayName().getString();
        boolean already = listed(relation, targetName, relation.friendCount());
        if (already) {
            source.displayClientMessage(Component.literal(targetName + "已成为您的好友，无需重复申请。"), false);
            return;
        }
        source.displayClientMessage(Component.literal("已向" + targetName + "发送好友申请。"), false);
        target.getData(RelationAttachments.PLAYER).setRequest(source.getDisplayName().getString(), relation.allegiance().team());
        target.syncData(RelationAttachments.PLAYER);
        target.openMenu(new SeamlessMenuProvider((id, inventory, opener) -> new TeamRequestMenu(id, inventory), Component.literal("ZuduiteamGui")));
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
     * 同意申请。申请者须在本人所在维度在线；双方各自按对方姓名与自身好友数检查去重，并清空待处理申请（Q19）。
     */
    private static void accept(ServerPlayer player) {
        PlayerRelation relation = player.getData(RelationAttachments.PLAYER);
        String requestName = relation.requestName();
        String requestTeam = relation.requestTeam();
        String myName = player.getDisplayName().getString();
        String myTeam = relation.allegiance().team();
        boolean online = false;
        for (ServerPlayer requester : player.serverLevel().players()) {
            String requesterName = requester.getDisplayName().getString();
            if (requesterName.equals(requestName)) {
                PlayerRelation other = requester.getData(RelationAttachments.PLAYER);
                if (!listed(other, myName, other.friendCount())) {
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
        if (!listed(relation, requestName, relation.friendCount())) {
            relation.addFriend(requestName, requestTeam);
            relation.allegiance().setFriendly(requestTeam, true);
        }
        relation.setRequest("", "");
        player.syncData(RelationAttachments.PLAYER);
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
