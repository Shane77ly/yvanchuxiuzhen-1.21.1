package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 雪妖兔王暴风雪效果逐刻生成雪粒并按来源间隔冻结敌对实体。 */
public final class Yaoshou053BlizzardEffect implements EffectBehavior {
    private static final String EFFECT_ID = "yaoshou_053baofengxve";

    @Override
    public boolean tick(LivingEntity entity, int amplifier) {
        ServerLevel level = (ServerLevel) entity.level();
        level.sendParticles(Particles.type("snow"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                entity.getZ(), 24, 12, 6, 12, 0);
        Vec3 center = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ());
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(32, 16, 32))
                .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
            if (!Relations.isFriendly(entity, target)) {
                double ticks = target.getPersistentData().getDouble("yaoshou053tick") + 1;
                target.getPersistentData().putDouble("yaoshou053tick", ticks);
                if (ticks >= 10) {
                    target.setTicksFrozen(target.getTicksFrozen() + (StatusEffects.amplifier(entity, EFFECT_ID) + 1) * 4);
                    target.getPersistentData().putDouble("yaoshou053tick", 0);
                }
            }
        }
        return true;
    }
}
