package net.yvanchuxiuzhen.system.npc.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.system.npc.NpcData;
import net.yvanchuxiuzhen.system.npc.NpcFollowState;

/** 剧情实体跟随状态由修士系统写入，脚本调用方不直接修改其持久化字段。 */
public final class NpcFollowers {
    private NpcFollowers() {
    }

    public static void follow(Entity follower, ServerPlayer player) {
        follower.setData(NpcData.FOLLOW, new NpcFollowState(true, player.getStringUUID(),
                follower.getData(NpcData.FOLLOW).tick()));
    }

    public static void stopFollowing(Entity follower) {
        follower.setData(NpcData.FOLLOW, new NpcFollowState(false, "",
                follower.getData(NpcData.FOLLOW).tick()));
    }
}
