package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** Yaoshou040skill02DangShiTiGengXinKeShi来源回调。 */
final class Yaoshou040skill02DangShiTiGengXinKeShi {
    private Yaoshou040skill02DangShiTiGengXinKeShi() { }
    static void execute(LevelAccessor world, Entity entity) {
        if (world instanceof ServerLevel _level)
            _level.sendParticles(Particles.type("yaoshou_040skill_0201"), (entity.getX()), (entity.getY()), (entity.getZ()), 5, 1.8, 1.8, 1.8, 0);
        if (!entity.getPersistentData().getBoolean("kaishi")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, (false), (false)));
            entity.getPersistentData().putBoolean("kaishi", (true));
        }
        if (entity.onGround()) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, (entity.getX()), (entity.getY()), (entity.getZ()), 3, 0.5, 0.5, 0.5, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("cailiao_229particle"), (entity.getX()), (entity.getY()), (entity.getZ()), 20, 0, 0, 0, 1);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("bigbanglightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 1, 0, 0, 0, 0);
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("entity.generic.explode"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("entity.generic.explode"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "光" + " " + entity.getStringUUID() + " " + "false" + " " + "8" + " " + "2"));
                        }
                    }
                }
            }
            if (!entity.level().isClientSide())
                entity.discard();
        }

    }
}
