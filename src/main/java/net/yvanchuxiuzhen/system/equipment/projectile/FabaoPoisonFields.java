package net.yvanchuxiuzhen.system.equipment.projectile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 三号法宝命中后的十次毒域；服务端刻结束时推进，独立于弹体存活且不写入存档。 */
public final class FabaoPoisonFields {
    private static final List<Field> FIELDS = new ArrayList<>();

    private FabaoPoisonFields() {
    }

    static void start(ServerLevel level, Vec3 center, FabaoProjectile projectile, Entity owner) {
        FIELDS.add(new Field(level, center, projectile, owner));
    }

    public static void tick(ServerTickEvent.Post event) {
        Iterator<Field> iterator = FIELDS.iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            field.ticks++;
            if (field.ticks == 1 || field.ticks % 10 == 0) field.pulse();
            if (field.ticks == 90) iterator.remove();
        }
    }

    public static void stopped(ServerStoppedEvent event) {
        FIELDS.clear();
    }

    private static final class Field {
        private final ServerLevel level;
        private final Vec3 center;
        private final FabaoProjectile projectile;
        private final Entity owner;
        private int ticks;

        private Field(ServerLevel level, Vec3 center, FabaoProjectile projectile, Entity owner) {
            this.level = level;
            this.center = center;
            this.projectile = projectile;
            this.owner = owner;
        }

        private void pulse() {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1, center.z, 20, 0, 0, 0, 1);
            level.sendParticles(ParticleTypes.FALLING_HONEY, center.x, center.y + 1, center.z, 20, 0, 0, 0, 1);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(5));
            targets.sort(Comparator.comparingDouble(target -> target.distanceToSqr(center)));
            for (LivingEntity target : targets) {
                if (!Relations.isFriendly(owner, target)) projectile.applyFieldPoison(target);
            }
        }
    }
}
