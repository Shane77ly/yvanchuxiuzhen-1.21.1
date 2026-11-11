package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** Yaoshou040skill0301TouZhiWuFeiXingTick来源回调。 */
final class Yaoshou040skill0301TouZhiWuFeiXingTick {
    private Yaoshou040skill0301TouZhiWuFeiXingTick() { }
    static void execute(LevelAccessor world, Entity entity, Entity immediatesourceentity) {
        if (entity == null || immediatesourceentity == null)
            return;
        Entity linshientity = null;
        if (!immediatesourceentity.isNoGravity()) {
            immediatesourceentity.getPersistentData().putString("mubiaouid", (entity.getPersistentData().getString("gongjizhe")));
            immediatesourceentity.setNoGravity((true));
        }
        linshientity = Yaoshou039040Support.target(world, immediatesourceentity.getPersistentData().getString("mubiaouid"));
        if (!(linshientity == null)) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("yaoshou_040skill_03"), (immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()), 1, 0, 0, 0, 0);
            immediatesourceentity.getPersistentData().putDouble("flytick", (immediatesourceentity.getPersistentData().getDouble("flytick") + 1));
            if (immediatesourceentity.getPersistentData().getDouble("flytick") >= 200) {
                immediatesourceentity.getPersistentData().putDouble("flytick", 0);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(ParticleTypes.FLASH, (immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()), 1, 0, 0, 0, 0);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("smallexplosionlightyellow"), (immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()), 1, 0, 0, 0, 0);
                {
                    final Vec3 _center = new Vec3((immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "光" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1"));
                            }
                        }
                    }
                }
                if (!immediatesourceentity.level().isClientSide())
                    immediatesourceentity.discard();
            } else if (immediatesourceentity.getPersistentData().getDouble("flytick") >= 10 && !immediatesourceentity.getPersistentData().getBoolean("kaishixvnzhao")) {
                immediatesourceentity.getPersistentData().putBoolean("kaishixvnzhao", (true));
            } else if (immediatesourceentity.getPersistentData().getBoolean("kaishixvnzhao")) {
                immediatesourceentity.getPersistentData().putDouble("flytick2", (immediatesourceentity.getPersistentData().getDouble("flytick2") + 1));
                if (immediatesourceentity.getPersistentData().getDouble("flytick2") >= 10) {
                    {
                        final Vec3 _center = new Vec3((immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()));
                        List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(32 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                .toList();
                        for (Entity entityiterator : _entfound) {
                            if (entityiterator.isAlive()) {
                                if (linshientity == entityiterator) {
                                    immediatesourceentity.setDeltaMovement(
                                            new Vec3(((entityiterator.getX() - immediatesourceentity.getX()) * 0.1), ((entityiterator.getY() - immediatesourceentity.getY()) * 0.1), ((entityiterator.getZ() - immediatesourceentity.getZ()) * 0.1)));
                                    break;
                                }
                            }
                        }
                    }
                    immediatesourceentity.getPersistentData().putDouble("flytick2", 0);
                }
            }
        } else {
            immediatesourceentity.getPersistentData().putDouble("flytick", (immediatesourceentity.getPersistentData().getDouble("flytick") + 1));
            if (immediatesourceentity.getPersistentData().getDouble("flytick") >= 20) {
                if (!immediatesourceentity.level().isClientSide())
                    immediatesourceentity.discard();
            }
        }
    
    }
}
