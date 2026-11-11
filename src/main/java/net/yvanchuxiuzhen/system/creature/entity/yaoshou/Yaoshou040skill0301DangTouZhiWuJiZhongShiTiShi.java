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

/** Yaoshou040skill0301DangTouZhiWuJiZhongShiTiShi来源回调。 */
final class Yaoshou040skill0301DangTouZhiWuJiZhongShiTiShi {
    private Yaoshou040skill0301DangTouZhiWuJiZhongShiTiShi() { }
    static void execute(LevelAccessor world, Entity immediatesourceentity, Entity sourceentity) {
        if (immediatesourceentity == null || sourceentity == null)
            return;
        if (world instanceof ServerLevel _level)
            _level.sendParticles(ParticleTypes.FLASH, (immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()), 1, 0, 0, 0, 0);
        if (world instanceof ServerLevel _level)
            _level.sendParticles(Particles.type("smallexplosionlightyellow"), (immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()), 1, 0, 0, 0, 0);
        {
            final Vec3 _center = new Vec3((immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()));
            List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
            for (Entity entityiterator : _entfound) {
                if (!Relations.isFriendly(sourceentity, entityiterator)) {
                    {
                        Entity _ent = entityiterator;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null)
                            _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                    ("shanghai" + " " + "光" + " " + sourceentity.getStringUUID() + " " + "false" + " " + "2" + " " + "1"));
                    }
                }
            }
        }
    
    }
}
