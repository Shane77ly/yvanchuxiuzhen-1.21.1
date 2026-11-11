package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 黑泽效果每刻显示墨雾，每二十刻对范围内敌方施加同等级减速。 */
public final class Yaoshou036BlackMarshEffect implements EffectBehavior {
    @Override
    public boolean tick(LivingEntity entity, int amplifier) {
        ServerLevel level = (ServerLevel) entity.level();
        level.sendParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY(), entity.getZ(), 40, 12, 0.5, 12, 0);
        level.sendParticles(ParticleTypes.MYCELIUM, entity.getX(), entity.getY(), entity.getZ(), 20, 12, 2, 12, 0);
        entity.getPersistentData().putDouble("heizetick", entity.getPersistentData().getDouble("heizetick") + 1);
        if (entity.getPersistentData().getDouble("heizetick") >= 20) {
            Vec3 center = entity.position();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(12, 2, 12)).stream()
                    .sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
                if (!Relations.isFriendly(entity, target)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, amplifier));
                }
            }
            entity.getPersistentData().putDouble("heizetick", 0);
        }
        return true;
    }
}
