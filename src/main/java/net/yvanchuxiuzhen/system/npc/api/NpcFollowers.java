package net.yvanchuxiuzhen.system.npc.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** 剧情实体跟随状态由修士系统写入，脚本调用方不直接修改其持久化字段。 */
public final class NpcFollowers {
    private NpcFollowers() {
    }

    public static void follow(Entity follower, ServerPlayer player) {
        follower.getPersistentData().putBoolean("gensuiplayer", true);
        follower.getPersistentData().putString("gensuiplayeruid", player.getStringUUID());
    }

    public static void stopFollowing(Entity follower) {
        follower.getPersistentData().putBoolean("gensuiplayer", false);
        follower.getPersistentData().putString("gensuiplayeruid", "");
    }
}
