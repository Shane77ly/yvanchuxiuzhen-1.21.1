package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.Lightning;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.fulu.api.NpcTalismans;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 修者0101来源六种符箓技能，符箓状态与召唤物由所属系统公开接口写入。 */
public final class NpcTalismanSkills {
    private NpcTalismanSkills() { }

    /** 来源 Npcskill0301Procedure：保留36刻的蓄力、施放与结束阶段。 */
    public static void Npcskill0301(LevelAccessor world, Entity entity) {
        double Xzuobiao = 0;
        double Yzuobiao = 0;
        double Zzuobiao = 0;
        Entity linshientity = null;
        linshientity = Xiuzhe0101Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 16) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("fulubagua"), (entity.getX()), (entity.getY() + 1.5), (entity.getZ()), 1, 0, 0, 0, 0);
            CreatureData.state(entity).setPhase("释放");
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 20) {
            if (EntityAttributes.level(entity) <= 10) {
                CreatureData.state(entity).setTalismanLevel(1);
            } else if (EntityAttributes.level(entity) <= 20) {
                CreatureData.state(entity).setTalismanLevel(2);
            } else if (EntityAttributes.level(entity) <= 30) {
                CreatureData.state(entity).setTalismanLevel(3);
            } else {
                CreatureData.state(entity).setTalismanLevel(4);
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1.5, entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + 1.5), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            Yzuobiao = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(7)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            Xzuobiao = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(7)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            Zzuobiao = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(7)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world.isEmptyBlock(BlockPos.containing(Xzuobiao, Yzuobiao - 2, Zzuobiao))) {
                Yzuobiao = Yzuobiao - 2;
            } else if (world.isEmptyBlock(BlockPos.containing(Xzuobiao, Yzuobiao - 1, Zzuobiao))) {
                Yzuobiao = Yzuobiao - 1;
            }
            if (world instanceof ServerLevel serverLevel) {
                NpcTalismans.summonThunderPull(serverLevel, entity, new Vec3(Xzuobiao, Yzuobiao, Zzuobiao), CreatureData.state(entity).talismanLevel());
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 36) {
            CreatureData.state(entity).setSkill("");
            CreatureData.state(entity).setPhase("");
            CreatureData.state(entity).setTick(0);
        }
    }

    /** 来源 Npcskill0302Procedure：保留36刻的蓄力、施放与结束阶段。 */
    public static void Npcskill0302(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Xiuzhe0101Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 16) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("fulubagua"), (entity.getX()), (entity.getY() + 1.5), (entity.getZ()), 1, 0, 0, 0, 0);
            CreatureData.state(entity).setPhase("释放");
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 20) {
            if (EntityAttributes.level(entity) <= 10) {
                CreatureData.state(entity).setTalismanLevel(1);
            } else if (EntityAttributes.level(entity) <= 20) {
                CreatureData.state(entity).setTalismanLevel(2);
            } else if (EntityAttributes.level(entity) <= 30) {
                CreatureData.state(entity).setTalismanLevel(3);
            } else {
                CreatureData.state(entity).setTalismanLevel(4);
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1.5, entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + 1.5), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            NpcTalismans.activateFlameJet(entity, CreatureData.state(entity).talismanLevel());
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 36) {
            CreatureData.state(entity).setSkill("");
            CreatureData.state(entity).setPhase("");
            CreatureData.state(entity).setTick(0);
        }
    }

    /** 来源 Npcskill0303Procedure：保留36刻的蓄力、施放与结束阶段。 */
    public static void Npcskill0303(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Xiuzhe0101Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 16) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("fulubagua"), (entity.getX()), (entity.getY() + 1.5), (entity.getZ()), 1, 0, 0, 0, 0);
            CreatureData.state(entity).setPhase("释放");
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 20) {
            if (EntityAttributes.level(entity) <= 10) {
                CreatureData.state(entity).setTalismanLevel(1);
            } else if (EntityAttributes.level(entity) <= 20) {
                CreatureData.state(entity).setTalismanLevel(2);
            } else if (EntityAttributes.level(entity) <= 30) {
                CreatureData.state(entity).setTalismanLevel(3);
            } else {
                CreatureData.state(entity).setTalismanLevel(4);
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1.5, entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + 1.5), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            NpcTalismans.activateWindPressure(entity, CreatureData.state(entity).talismanLevel());
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 36) {
            CreatureData.state(entity).setSkill("");
            CreatureData.state(entity).setPhase("");
            CreatureData.state(entity).setTick(0);
        }
    }

    /** 来源 Npcskill0304Procedure：保留36刻的蓄力、施放与结束阶段。 */
    public static void Npcskill0304(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Xiuzhe0101Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 16) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("fulubagua"), (entity.getX()), (entity.getY() + 1.5), (entity.getZ()), 1, 0, 0, 0, 0);
            CreatureData.state(entity).setPhase("释放");
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 20) {
            if (EntityAttributes.level(entity) <= 10) {
                CreatureData.state(entity).setTalismanLevel(1);
            } else if (EntityAttributes.level(entity) <= 20) {
                CreatureData.state(entity).setTalismanLevel(2);
            } else if (EntityAttributes.level(entity) <= 30) {
                CreatureData.state(entity).setTalismanLevel(3);
            } else {
                CreatureData.state(entity).setTalismanLevel(4);
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1.5, entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + 1.5), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel serverLevel) {
                NpcTalismans.shootQiSword(serverLevel, entity, CreatureData.state(entity).talismanLevel());
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 36) {
            CreatureData.state(entity).setSkill("");
            CreatureData.state(entity).setPhase("");
            CreatureData.state(entity).setTick(0);
        }
    }

    /** 来源 Npcskill0305Procedure：保留36刻的蓄力、施放与结束阶段。 */
    public static void Npcskill0305(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        Entity linshientity = null;
        linshientity = Xiuzhe0101Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 16) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("fulubagua"), (entity.getX()), (entity.getY() + 1.5), (entity.getZ()), 1, 0, 0, 0, 0);
            CreatureData.state(entity).setPhase("释放");
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 20) {
            if (EntityAttributes.level(entity) <= 10) {
                CreatureData.state(entity).setTalismanLevel(1);
            } else if (EntityAttributes.level(entity) <= 20) {
                CreatureData.state(entity).setTalismanLevel(2);
            } else if (EntityAttributes.level(entity) <= 30) {
                CreatureData.state(entity).setTalismanLevel(3);
            } else {
                CreatureData.state(entity).setTalismanLevel(4);
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1.5, entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + 1.5), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(7)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(7)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(7)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world.isEmptyBlock(BlockPos.containing(shijiaoX, shijiaoY - 3, shijiaoZ))) {
                shijiaoY = shijiaoY - 3;
            } else if (world.isEmptyBlock(BlockPos.containing(shijiaoX, shijiaoY - 2, shijiaoZ))) {
                shijiaoY = shijiaoY - 2;
            } else if (world.isEmptyBlock(BlockPos.containing(shijiaoX, shijiaoY - 1, shijiaoZ))) {
                shijiaoY = shijiaoY - 1;
            }
            if (world instanceof ServerLevel _level)
                Lightning.cast(_level, new Vec3(shijiaoX, shijiaoY, shijiaoZ), entity, 30 + CreatureData.state(entity).talismanLevel() * 20, 1, Relations.entityTeam(entity), 4, "红色", true);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 36) {
            CreatureData.state(entity).setSkill("");
            CreatureData.state(entity).setPhase("");
            CreatureData.state(entity).setTick(0);
        }
    }

    /** 来源 Npcskill0306Procedure：保留36刻的蓄力、施放与结束阶段。 */
    public static void Npcskill0306(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        Entity linshientity = null;
        linshientity = Xiuzhe0101Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 16) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("fulubagua"), (entity.getX()), (entity.getY() + 1.5), (entity.getZ()), 1, 0, 0, 0, 0);
            CreatureData.state(entity).setPhase("释放");
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 20) {
            if (EntityAttributes.level(entity) <= 10) {
                CreatureData.state(entity).setTalismanLevel(1);
            } else if (EntityAttributes.level(entity) <= 20) {
                CreatureData.state(entity).setTalismanLevel(2);
            } else if (EntityAttributes.level(entity) <= 30) {
                CreatureData.state(entity).setTalismanLevel(3);
            } else {
                CreatureData.state(entity).setTalismanLevel(4);
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1.5, entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + 1.5), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            for (int index0 = 0; index0 < (int) (20); index0++) {
                world.addParticle(ParticleTypes.FLAME, (entity.getX()), (entity.getY() + 1), (entity.getZ()), (entity.getLookAngle().x * Xiuzhe0101Support.randomDouble(0, 2)), (entity.getLookAngle().y * Xiuzhe0101Support.randomDouble(0, 2)),
                        (entity.getLookAngle().z * Xiuzhe0101Support.randomDouble(0, 2)));
            }
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(6)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(6)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(6)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(6 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.FLAME, (entityiterator.getX()), (entityiterator.getY() + 1), (entityiterator.getZ()), 20, 0.5, 0.8, 0.5, 0.1);
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_023"), (int) (400 + CreatureData.state(entity).talismanLevel() * 200),
                                    (int) ((CreatureData.state(entity).talismanLevel() - 1) * 2)));
                    }
                }
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 36) {
            CreatureData.state(entity).setSkill("");
            CreatureData.state(entity).setPhase("");
            CreatureData.state(entity).setTick(0);
        }
    }
}
