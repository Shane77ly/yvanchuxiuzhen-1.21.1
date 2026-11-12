package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 修者0101的十六种近战技能，保留来源阶段、命中范围、效果与清理顺序。 */
public final class NpcMeleeSkills {
    private NpcMeleeSkills() { }

    public static void Npcskill0101(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double attack_Num = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 8, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 2) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 4) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 1.2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 100, 0));
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0102(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double attack_Num = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 1.5;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0, 0, 0, 1);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.EXPLOSION, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 1, 0, 0, 0, 0);
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("entity.dragon_fireball.explode"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("entity.dragon_fireball.explode"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 0.7), 0.3, ((entityiterator.getZ() - entity.getZ()) * 0.7)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0103(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double attack_Num = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 48, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 48, 20, (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.getPersistentData().putDouble("shijiao", (entity.getYRot()));
            entity.getPersistentData().putString("jinengjinxing", "出拳");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("出拳") && entity.getPersistentData().getDouble("entitytick") >= 40) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("出拳")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 2, 0.1, 0.1, 0.1, 0);
            {
                Entity _ent = entity;
                _ent.setYRot((float) entity.getPersistentData().getDouble("shijiao"));
                _ent.setXRot(0);
                _ent.setYBodyRot(_ent.getYRot());
                _ent.setYHeadRot(_ent.getYRot());
                _ent.yRotO = _ent.getYRot();
                _ent.xRotO = _ent.getXRot();
                if (_ent instanceof LivingEntity _entity) {
                    _entity.yBodyRotO = _entity.getYRot();
                    _entity.yHeadRotO = _entity.getYRot();
                }
            }
            entity.setDeltaMovement(new Vec3((-(0.4 * Math.sin(entity.getPersistentData().getDouble("shijiao") * (Math.PI / 180)))), 0, (0.4 * Math.cos(entity.getPersistentData().getDouble("shijiao") * (Math.PI / 180)))));
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 0.5;
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + 1.5), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010103"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY()), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010103"), SoundSource.NEUTRAL, 1, 1,
                                        false);
                            }
                        }
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0104(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 8, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 20, (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.getPersistentData().putString("jinengjinxing", "开打");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("开打") && entity.getPersistentData().getDouble("entitytick") >= 45) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("开打")) {
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 4.8) {
                attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 0.5;
                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.2, 0.2, 0.2, 0);
                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                    } else {
                        _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                    }
                }
                {
                    final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .collect(Collectors.toList());
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof Level _level) {
                                if (!_level.isClientSide()) {
                                    _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010104"),
                                            SoundSource.NEUTRAL, 1, 1);
                                } else {
                                    _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010104"),
                                            SoundSource.NEUTRAL, 1, 1, false);
                                }
                            }
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 20, 0));
                            entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        }
                    }
                }
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 56) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            entity.getPersistentData().putDouble("entitytick2", 0);
        }
    }

    public static void Npcskill0105(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 56, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 56, 20, (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.getPersistentData().putString("jinengjinxing", "开打");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("开打") && entity.getPersistentData().getDouble("entitytick") >= 45) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("开打")) {
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 4.8) {
                attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 0.5;
                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.2, 0.2, 0.2, 0);
                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                    } else {
                        _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                    }
                }
                {
                    final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .collect(Collectors.toList());
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof Level _level) {
                                if (!_level.isClientSide()) {
                                    _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010104"),
                                            SoundSource.NEUTRAL, 1, 1);
                                } else {
                                    _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010104"),
                                            SoundSource.NEUTRAL, 1, 1, false);
                                }
                            }
                            entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        }
                    }
                }
                entity.getPersistentData().putDouble("entitytick2", 0);
                {
                    Entity _ent = entity;
                    _ent.setYRot((float) (entity.getYRot() + 100));
                    _ent.setXRot(0);
                    _ent.setYBodyRot(_ent.getYRot());
                    _ent.setYHeadRot(_ent.getYRot());
                    _ent.yRotO = _ent.getYRot();
                    _ent.xRotO = _ent.getXRot();
                    if (_ent instanceof LivingEntity _entity) {
                        _entity.yBodyRotO = _entity.getYRot();
                        _entity.yHeadRotO = _entity.getYRot();
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 56) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            entity.getPersistentData().putDouble("entitytick2", 0);
        }
    }

    public static void Npcskill0106(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 32, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 32, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 1.2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010101"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010101"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_001"), 100, 0));
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 32) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0107(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 1.5;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_003"), 100, 0));
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 1), 0.3, ((entityiterator.getZ() - entity.getZ()) * 1)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 36) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0108(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 1.5;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("dajigan_01"), shijiaoX, (entity.getY() + 1.5), shijiaoZ, 1, 0, 0, 0, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010107"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010107"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        if (entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_007")) : false) {
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_007"), 3600,
                                        (int) ((entityiterator instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_007")) ? _livEnt.getEffect(StatusEffects.holder("effect_007")).getAmplifier() : 0)
                                                + 1)));
                        } else {
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_007"), 3600, 0));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0109(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 56, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 56, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 38) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 40) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("dajigan_01"), shijiaoX, (entity.getY() + 1.5), shijiaoZ, 1, 0, 0, 0, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 100, 0));
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 56) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0110(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue();
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 0.1), 1.1, ((entityiterator.getZ() - entity.getZ()) * 0.1)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 36) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0111(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 32, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 32, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill0101huikong"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue();
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010101"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010101"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 0.1), (-1), ((entityiterator.getZ() - entity.getZ()) * 0.1)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 32) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0112(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;

        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:ice1"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:ice1"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosionlightblue"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0,
                                    0, 0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "金" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1.7"));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0113(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:ice1"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:ice1"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SPLASH, (entity.getX()), (entity.getY() + 1), (entity.getZ()), 5, 0.5, 0.5, 0.5, 0);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.BUBBLE, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 10, 0.5, 0.5, 0.5, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.FISHING, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 20, 0, 0, 0, 1);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosionblue"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0, 0,
                                    0);
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "水" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1.2"));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0114(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010104"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010104"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.REVERSE_PORTAL, (entity.getX()), (entity.getY() + 1), (entity.getZ()), 4, 0.5, 0.5, 0.5, 0);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.REVERSE_PORTAL, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 20, 0, 0, 0, 1);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiongreen"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0,
                                    0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "木" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1.2"));
                        }
                        if (entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.POISON) : false) {
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(
                                        new MobEffectInstance(MobEffects.POISON, (int) ((entityiterator instanceof LivingEntity _livEnt && _livEnt.hasEffect(MobEffects.POISON) ? _livEnt.getEffect(MobEffects.POISON).getDuration() : 0) + 200),
                                                (int) ((entityiterator instanceof LivingEntity _livEnt && _livEnt.hasEffect(MobEffects.POISON) ? _livEnt.getEffect(MobEffects.POISON).getAmplifier() : 0) + 1)));
                        } else {
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0115(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:bowshoot5"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:bowshoot5"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.FLAME, (entity.getX()), (entity.getY() + 1), (entity.getZ()), 2, 0, 0, 0, 0.05);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.LARGE_SMOKE, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 20, 0.5, 0.5, 0.5, 0.1);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.FLAME, shijiaoX, (entity.getY() + 1.5), shijiaoZ, 20, 0, 0, 0, 0.5);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosionred"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0, 0,
                                    0);
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"),
                                        SoundSource.NEUTRAL, 1, 1, false);
                            }
                        }
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_023"), 200, 0));
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "火" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1.5"));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0116(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double zweizhi = 0;
        double xweizhi = 0;
        double yweizhi = 0;
        double blockyingdu = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010107"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + 1.5), shijiaoZ, Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010107"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "打击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.WHITE_ASH, (entity.getX()), (entity.getY() + 1), (entity.getZ()), 5, 1, 1, 1, 0);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("打击") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            world.levelEvent(2001, BlockPos.containing(shijiaoX, entity.getY() + 1.5, shijiaoZ), Block.getId(Blocks.COBBLESTONE.defaultBlockState()));
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"),
                                        SoundSource.NEUTRAL, 1, 1, false);
                            }
                        }
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 100, 0));
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiondeeporange"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0,
                                    0, 0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "土" + " " + entity.getStringUUID() + " " + "false" + " " + "1" + " " + "1.5"));
                        }
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 1), 0.3, ((entityiterator.getZ() - entity.getZ()) * 1)));
                        }
                    }
                }
            }
            xweizhi = -1;
            yweizhi = -1;
            zweizhi = -1;
            for (int index0 = 0; index0 < (int) (3); index0++) {
                for (int index1 = 0; index1 < (int) (3); index1++) {
                    for (int index2 = 0; index2 < (int) (3); index2++) {
                        blockyingdu = world.getBlockState(BlockPos.containing(shijiaoX + xweizhi, entity.getLookAngle().y + 1.5 + yweizhi, shijiaoZ + zweizhi)).getDestroySpeed(world,
                                BlockPos.containing(shijiaoX + xweizhi, entity.getLookAngle().y + 1.5 + yweizhi, shijiaoZ + zweizhi));
                        if (blockyingdu >= 0 && blockyingdu < 5) {
                            world.destroyBlock(BlockPos.containing(shijiaoX + xweizhi, entity.getLookAngle().y + 1.5 + yweizhi, shijiaoZ + zweizhi), false);
                        }
                        zweizhi = zweizhi + 1;
                    }
                    zweizhi = -1;
                    yweizhi = yweizhi + 1;
                }
                yweizhi = -1;
                xweizhi = xweizhi + 1;
            }
            xweizhi = -1;
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }
}
