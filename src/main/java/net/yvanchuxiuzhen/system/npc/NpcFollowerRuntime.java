package net.yvanchuxiuzhen.system.npc;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** 来源剧情跟随每20刻重新寻路，只寻找当前维度内登记UUID对应的玩家。 */
public final class NpcFollowerRuntime {
    private NpcFollowerRuntime() {
    }

    public static void tick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity follower) || !(follower.level() instanceof ServerLevel level)) return;
        NpcFollowState state = follower.getData(NpcData.FOLLOW);
        if (!state.following()) return;
        int tick = state.tick() + 1;
        if (tick < 20) {
            follower.setData(NpcData.FOLLOW, state.withTick(tick));
            return;
        }
        if (follower instanceof Mob mob) {
            for (Player player : level.players()) {
                if (player.getStringUUID().equals(state.playerUuid())) {
                    mob.getNavigation().moveTo(player.getX() - 2 + new Random().nextDouble() * 4,
                            player.getY(), player.getZ() - 2 + new Random().nextDouble() * 4, 1);
                }
            }
        }
        follower.setData(NpcData.FOLLOW, state.withTick(0));
    }
}
