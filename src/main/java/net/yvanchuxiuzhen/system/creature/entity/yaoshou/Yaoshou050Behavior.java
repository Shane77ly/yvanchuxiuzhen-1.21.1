package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 050号妖兽来源的完整逐刻技能闭包。 */
final class Yaoshou050Behavior {
    private Yaoshou050Behavior() { }

    /** 来源 Yaoshou050DangShiTiGengXinKeShiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void tick(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli2 = 0;
        double jvli1 = 0;
        double qvanzhongdiejia = 0;
        double jiaodu = 0;
        double xvnhuan = 0;
        double suijishu = 0;
        double qvanzhongmax = 0;
        double shijiaoX = 0;
        double xvnhuan2 = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double shunxvjishu = 0;
        double buchong = 0;
        double damage = 0;
        if (entity.isAlive()) {
            CreatureData.state(entity).setTick((CreatureData.state(entity).tick() + 1));
            if ((CreatureData.state(entity).skill()).equals("")) {
                if (!(CreatureData.state(entity).attackerUuid()).equals(CreatureData.state(entity).tempAttackerUuid())) {
                    CreatureData.state(entity).setAttackerUuid((CreatureData.state(entity).tempAttackerUuid()));
                }
            }
            if (!(CreatureData.state(entity).attackerUuid()).equals("")) {
                linshientity = Yaoshou037054Support.target(world, entity);
            }
            if (!(linshientity == null)) {
                if (linshientity.isAlive()) {
                    if ((CreatureData.state(entity).skill()).equals("")) {
                        if ((CreatureData.state(entity).queuedSkill()).equals("")) {
                            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                            jvli2 = jvli1 - (entity.getBbWidth() / 2 + linshientity.getBbWidth() / 2);
                            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            shunxvjishu = 0;
                            if (entity.isPassenger()) {
                                shunxvjishu = shunxvjishu + 1;
                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "挣脱");
                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 100);
                            }
                            if (!entity.isPassenger()) {
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_002")) : false)
                                        && !(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false)) {
                                    if (jvli2 < 5) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "鼠窜");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "苍痕一击");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    }
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "飞扑");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "向后跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "向左跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "向右跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "飒风");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    }
                                }
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false) && jvli2 < 3) {
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                }
                            }
                            xvnhuan = 1;
                            for (int index0 = 0; index0 < (int) (shunxvjishu); index0++) {
                                qvanzhongmax = qvanzhongmax + CreatureData.state(entity).candidateWeight((int) (xvnhuan));
                                xvnhuan = xvnhuan + 1;
                            }
                            suijishu = Yaoshou037054Support.randomInt(1, (int) qvanzhongmax);
                            xvnhuan2 = 1;
                            for (int index1 = 0; index1 < (int) (shunxvjishu); index1++) {
                                qvanzhongdiejia = qvanzhongdiejia + CreatureData.state(entity).candidateWeight((int) (xvnhuan2));
                                if (qvanzhongdiejia >= suijishu) {
                                    CreatureData.state(entity).setQueuedSkill((CreatureData.state(entity).candidateSkill((int) (xvnhuan2))));
                                    CreatureData.state(entity).setQueuedTimer((CreatureData.state(entity).candidateTimer((int) (xvnhuan2))));
                                    break;
                                }
                                xvnhuan2 = xvnhuan2 + 1;
                            }
                            CreatureData.state(entity).setCandidateCount(shunxvjishu);
                        } else if (CreatureData.state(entity).tick() >= CreatureData.state(entity).queuedTimer()) {
                            CreatureData.state(entity).setSkill((CreatureData.state(entity).queuedSkill()));
                            CreatureData.state(entity).setQueuedSkill("");
                            CreatureData.state(entity).setQueuedTimer(0);
                            CreatureData.state(entity).setTick(0);
                            xvnhuan = 1;
                            for (int index2 = 0; index2 < (int) (CreatureData.state(entity).candidateCount()); index2++) {
                                CreatureData.state(entity).setCandidateSkill((int) (xvnhuan), "");
                                CreatureData.state(entity).setCandidateTimer((int) (xvnhuan), 0);
                                CreatureData.state(entity).setCandidateWeight((int) (xvnhuan), 0);
                                xvnhuan = xvnhuan + 1;
                            }
                            CreatureData.state(entity).setCandidateCount(0);
                        }
                    } else {
                        if ((CreatureData.state(entity).skill()).equals("靠近")) {
                            if ((CreatureData.state(entity).phase()).equals("")) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                                if (entity instanceof Mob _entity)
                                    _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 1.5);
                                CreatureData.state(entity).setPhase("移动中");
                            } else if ((CreatureData.state(entity).phase()).equals("移动中") && CreatureData.state(entity).tick() >= 40) {
                                Yaoshou037054Support.finish(entity);
                            } else if ((CreatureData.state(entity).phase()).equals("移动中")) {
                                jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                                jvli2 = jvli1 - (entity.getBbWidth() / 2 + linshientity.getBbWidth() / 2);
                                if (jvli2 <= 2) {
                                    CreatureData.state(entity).setSkill("吹白");
                                    Yaoshou037054Support.finish(entity);
                                }
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("挣脱")) {
                            Yaoshou037054Support.escape(entity);
                        } else if ((CreatureData.state(entity).skill()).equals("普攻")) {
                            if ((CreatureData.state(entity).phase()).equals("")) {
                                CreatureData.state(entity).setPhase("准备");
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_052")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou052.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 2) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getZ();
                                {
                                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                            .toList();
                                    for (Entity entityiterator : _entfound) {
                                        if (!Relations.isFriendly(entity, entityiterator)) {
                                            if (world instanceof ServerLevel _level)
                                                _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 5, 0, 0, 0, 0.5);
                                            damage = EntityAttributes.stat(entity, Stat.ATTACK);
                                            if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.WEAKNESS) : false) {
                                                damage = damage - ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(MobEffects.WEAKNESS) ? _livEnt.getEffect(MobEffects.WEAKNESS).getAmplifier() : 0) + 1) * 4;
                                            }
                                            if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.DAMAGE_BOOST) : false) {
                                                damage = damage + ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(MobEffects.DAMAGE_BOOST) ? _livEnt.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() : 0) + 1) * 3;
                                            }
                                            if (damage <= 0) {
                                                damage = 0.1;
                                            }
                                            entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) damage);
                                        }
                                    }
                                }
                                CreatureData.state(entity).setPhase("攻击");
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 7) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("飞扑") || (CreatureData.state(entity).skill()).equals("向右跳")
                                || (CreatureData.state(entity).skill()).equals("向左跳") || (CreatureData.state(entity).skill()).equals("向后跳")) {
                            if ((CreatureData.state(entity).phase()).equals("")) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_052")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou052.sprint");
                                    }
                                }
                                CreatureData.state(entity).setPhase("跳跃");
                            } else if ((CreatureData.state(entity).phase()).equals("跳跃") && CreatureData.state(entity).tick() >= 1) {
                                jiaodu = entity.getYRot();
                                if ((CreatureData.state(entity).skill()).equals("向右跳")) {
                                    jiaodu = jiaodu + 70;
                                } else if ((CreatureData.state(entity).skill()).equals("向左跳")) {
                                    jiaodu = jiaodu - 70;
                                } else if ((CreatureData.state(entity).skill()).equals("向后跳")) {
                                    jiaodu = jiaodu + 180;
                                }
                                entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.5, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
                                CreatureData.state(entity).setPhase("结束");
                            } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 10) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("鼠窜")) {
                            Yaoshou050shucuan(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("苍痕一击")) {
                            Yaoshou050canghenyiji(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("飒风")) {
                            Yaoshou050safeng(world, entity);
                        }
                    }
                } else {
                    CreatureData.state(entity).setAttackerUuid("");
                    CreatureData.state(entity).setTempAttackerUuid("");
                    Yaoshou037054Support.finish(entity);
                }
            } else {
                if (CreatureData.state(entity).tick() >= 40) {
                    if (entity instanceof Mob _entity)
                        _entity.getNavigation().moveTo((entity.getX() + Yaoshou037054Support.randomInt(-3, 3)), (entity.getY()), (entity.getZ() + Yaoshou037054Support.randomInt(-3, 3)), 1);
                    CreatureData.state(entity).setTick(0);
                }
            }
        }
    }

    /** 来源 Yaoshou050safengProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou050safeng(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        String team = "";
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_050")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou050.skill02");
                }
            }
            entity.setDeltaMovement(new Vec3(0, 0.5, 0));
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 6) {
            team = Relations.team(entity);
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel) Yaoshou050Support.spawn(entity);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 16) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou050canghenyijiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou050canghenyiji(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("起跳");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_050")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou050.skill02");
                }
            }
            entity.setDeltaMovement(new Vec3(0, 0.7, 0));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0));
        } else if ((CreatureData.state(entity).phase()).equals("起跳") && CreatureData.state(entity).tick() >= 17) {
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_050")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou050.skill01");
                }
            }
            CreatureData.state(entity).setPhase("杀机");
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou050skill02"), SoundSource.HOSTILE, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou050skill02"), SoundSource.HOSTILE, 1, 1, false);
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("杀机") && CreatureData.state(entity).tick() >= 27) {
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("杀机")) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), (entity.getLookAngle().y), (entity.getLookAngle().z)));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("disappearlightgray"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 5, (entity.getBbWidth()), (entity.getBbHeight()),
                        (entity.getBbWidth()), 0);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 10, 0, 0, 0, 1);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiongolden"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0,
                                    0, 0);
                        SpellDamage.deal(entityiterator, DamageCategory.GOLD, entity, false, 5, 1.5);
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 30) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou050shucuanProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou050shucuan(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setSkillCount((Yaoshou037054Support.randomInt(1, 6)));
            CreatureData.state(entity).setPhase("设置");
        } else if ((CreatureData.state(entity).phase()).equals("设置")) {
            jiaodu = entity.getYRot();
            jiaodu = jiaodu + Yaoshou037054Support.randomInt(-30, 30);
            CreatureData.state(entity).setAngle(jiaodu);
            CreatureData.state(entity).setPhase("飞窜");
        } else if ((CreatureData.state(entity).phase()).equals("飞窜")) {
            jiaodu = CreatureData.state(entity).angle();
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_050")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou050.skill01");
                }
            }
            {
                Entity _ent = entity;
                _ent.setYRot((float) jiaodu);
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
            entity.setDeltaMovement(new Vec3((-(3 * Math.sin(jiaodu * (Math.PI / 180)))), 0, (3 * Math.cos(jiaodu * (Math.PI / 180)))));
            CreatureData.state(entity).setPhase("等待");
        } else if ((CreatureData.state(entity).phase()).equals("等待")) {
            CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
            if (CreatureData.state(entity).tick2() >= 10) {
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                if (CreatureData.state(entity).skillCount() >= 1) {
                    CreatureData.state(entity).setPhase("设置");
                    CreatureData.state(entity).setSkillCount((CreatureData.state(entity).skillCount() - 1));
                } else {
                    CreatureData.state(entity).setPhase("结束");
                }
                CreatureData.state(entity).setTick2(0);
            }
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 44) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
