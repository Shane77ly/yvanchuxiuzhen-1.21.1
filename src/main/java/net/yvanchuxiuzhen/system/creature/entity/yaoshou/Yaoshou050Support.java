package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 050随风实体的独立生成入口。 */
final class Yaoshou050Support {
    private Yaoshou050Support() { }

    static void spawn(Entity owner) {
        ServerLevel level = (ServerLevel) owner.level();
        Yaoshou050safengentityEntity follower = CreatureSetup.<Yaoshou050safengentityEntity>entityType("yaoshou_050safengentity").get().create(level);
        follower.getPersistentData().put("friendlist", owner.getPersistentData().getCompound("friendlist"));
        Relations.shareEntityAllegiance(owner, follower);
        follower.moveTo(owner.getX(), owner.getY() + owner.getBbHeight() / 2, owner.getZ(), level.getRandom().nextFloat() * 360F, 0);
        follower.setYRot(owner.getYRot());
        follower.setXRot(owner.getXRot());
        follower.setYBodyRot(follower.getYRot());
        follower.setYHeadRot(follower.getYRot());
        follower.yRotO = follower.getYRot();
        follower.xRotO = follower.getXRot();
        follower.yBodyRotO = follower.getYRot();
        follower.yHeadRotO = follower.getYRot();
        follower.getPersistentData().putDouble("headjiaodu", owner.getXRot());
        follower.getPersistentData().putDouble("jiaodu", owner.getYRot());
        follower.finalizeSpawn(level, level.getCurrentDifficultyAt(follower.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(follower);
    }
}
