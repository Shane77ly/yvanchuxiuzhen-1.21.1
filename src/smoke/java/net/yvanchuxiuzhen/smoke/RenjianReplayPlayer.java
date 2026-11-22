package net.yvanchuxiuzhen.smoke;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Smoke 玩家导航与跨端实体身份核对。 */
final class RenjianReplayPlayer {
    private RenjianReplayPlayer() { }

    static Entity mirror(Minecraft minecraft, Target target) {
        if (target == null || minecraft.level == null) return null;
        Entity entity = minecraft.level.getEntity(target.id());
        return entity != null && entity.isAlive() && entity.getUUID().equals(target.uuid()) ? entity : null;
    }

    static void look(Minecraft minecraft, Vec3 position) {
        minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, position);
        minecraft.gameRenderer.pick(1.0F);
    }

    static boolean safe(ServerLevel level, ServerPlayer player, Vec3 position) {
        BlockPos feet = BlockPos.containing(position);
        return level.isPositionEntityTicking(feet) && level.hasChunkAt(feet)
                && level.getBlockState(feet.below()).isCollisionShapeFullBlock(level, feet.below())
                && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.noCollision(player, player.getBoundingBox().move(position.subtract(player.position())));
    }

    record Target(int id, UUID uuid) {
        Target(Entity entity) { this(entity.getId(), entity.getUUID()); }
    }
}
