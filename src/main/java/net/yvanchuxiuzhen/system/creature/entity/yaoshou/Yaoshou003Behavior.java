package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.particles.SimpleParticleType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 003号妖兽的来源逐刻技能与阶段流程。 */
final class Yaoshou003Behavior {
    private Yaoshou003Behavior() { }

    /** 来源 Yaoshou003DangShiTiShouShangShiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void hurt(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (entity.isAlive()) {
            if (!Relations.isFriendly(entity, sourceentity)) {
                CreatureData.state(entity).setTempAttackerUuid((sourceentity.getStringUUID()));
                if (CreatureData.state(entity).interrupted()) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("empty");
                    }
                    CreatureData.state(entity).setInterrupted((false));
                }
                if ((CreatureData.state(entity).skill()).equals("")) {
                    if (!entity.level().isClientSide) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.attacked");
                        }
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou003DangShiTiGengXinKeShiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void tick(LevelAccessor world, Entity entity) {
        boolean kaishi = false;
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double jvli2 = 0;
        double jvli1 = 0;
        double qvanzhongdiejia = 0;
        double xvnhuan = 0;
        double suijishu = 0;
        double qvanzhongmax = 0;
        double xvnhuan2 = 0;
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
                                    if (jvli2 > 8) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "大跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "冲刺");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    } else if (jvli2 > 2) {
                                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) < (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / 2) {
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "吃草");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "左前跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "右前跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "大顶");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 40);
                                    }
                                    if (jvli2 < 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "震地");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        if (entity.getY() >= linshientity.getY() + 2) {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 100);
                                        } else {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        }
                                    }
                                }
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false)) {
                                    if (jvli2 < 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 20);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    }
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
                            if (!(CreatureData.state(entity).phase()).equals("移动中")) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                                if (entity instanceof Mob _entity)
                                    _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 1.5);
                                CreatureData.state(entity).setPhase("移动中");
                            } else if ((CreatureData.state(entity).phase()).equals("移动中") && CreatureData.state(entity).tick() >= 40) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("挣脱")) {
                            Yaoshou037054Support.escape(entity);
                        } else if ((CreatureData.state(entity).skill()).equals("普攻")) {
                            if (!(CreatureData.state(entity).phase()).equals("准备") && !(CreatureData.state(entity).phase()).equals("攻击")) {
                                CreatureData.state(entity).setPhase("准备");
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 5) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getZ();
                                {
                                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
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
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 11) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("震地")) {
                            Yaoshou003skillzhendi(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("大顶")) {
                            Yaoshou003skilldading(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("右前跳")) {
                            Yaoshou003skillyouqiantiao(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("左前跳")) {
                            Yaoshou003skillzuoqiantiao(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("后蹬")) {
                            Yaoshou003skillhoudeng(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("吃草")) {
                            Yaoshou003skillchicao(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("冲刺")) {
                            Yaoshou003skillchongci(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("大跳")) {
                            Yaoshou003skilldatiao(world, entity);
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

    /** 来源 Yaoshou003skilldatiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skilldatiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill02");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 12) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 1, (entity.getLookAngle().z)));
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 30) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillchongciProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillchongci(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (entity instanceof Mob _entity)
                _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 2);
            CreatureData.state(entity).setPhase("释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.sprint");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.sprint");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.sprint");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() > 60) {
            Yaoshou037054Support.finish(entity);
        } else if ((CreatureData.state(entity).phase()).equals("释放")) {
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 <= 2) {
                CreatureData.state(entity).setTick(0);
                CreatureData.state(entity).setTick2(0);
                CreatureData.state(entity).setPhase("");
                CreatureData.state(entity).setSkill("大顶");
            } else {
                CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
                if (CreatureData.state(entity).tick2() >= 20) {
                    if (entity instanceof Mob _entity)
                        _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 2);
                    CreatureData.state(entity).setTick2(0);
                }
            }
        }
    }

    /** 来源 Yaoshou003skillchicaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillchicao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        if ((CreatureData.state(entity).phase()).equals("")) {
            CreatureData.state(entity).setPhase("吃草");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.graze");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.graze");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.graze");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("吃草") && CreatureData.state(entity).tick() >= 23) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 3));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.HAPPY_VILLAGER, (entity.getX()), (entity.getY() + entity.getBbHeight()), (entity.getZ()), 20, 1, 1, 1, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("stripgreen"), (entity.getX()), (entity.getY()), (entity.getZ()), 20, 1, 0, 1, 1);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillhoudengProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillhoudeng(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((entity.getX() * 2 - linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (entity.getZ() * 2 - linshientity.getZ())));
            CreatureData.state(entity).setPhase("释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.superbackkick");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.superbackkick");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.superbackkick");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 5) {
            shijiaoX = -(entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(3)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX());
            shijiaoZ = -(entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(3)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ());
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smokedarkgrey"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 12, 0, 0, 0, 1);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 20, 0, 0, 0, 1);
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
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3((-(entity.getLookAngle().x)), 1, (-(entity.getLookAngle().z))));
                        }
                    }
                }
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 10) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillzuoqiantiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillzuoqiantiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill03");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill03");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill03");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 4) {
            jiaodu = entity.getYRot() - 45;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.4, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 14) {
            Yaoshou037054Support.finish(entity);
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 >= 5) {
                CreatureData.state(entity).setSkill("右前跳");
            } else {
                CreatureData.state(entity).setSkill("大顶");
            }
        }
    }

    /** 来源 Yaoshou003skillyouqiantiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillyouqiantiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill04");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill04");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill04");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 4) {
            jiaodu = entity.getYRot() + 45;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.4, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 14) {
            Yaoshou037054Support.finish(entity);
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 >= 5) {
                CreatureData.state(entity).setSkill("左前跳");
            } else {
                CreatureData.state(entity).setSkill("大顶");
            }
        }
    }

    /** 来源 Yaoshou003skilldadingProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skilldading(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.attack");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.attack");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.attack");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 5) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY()), (entityiterator.getZ()), 20, 0, 0, 0, 1);
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
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 1, (entity.getLookAngle().z)));
                        }
                    }
                }
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillzhendiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillzhendi(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double xiangliangY = 0;
        double xiangliangX = 0;
        double R = 0;
        double particlenumber = 0;
        double xiangliangZ = 0;
        double shengchengZ = 0;
        double shengchengX = 0;
        double shengchengY = 0;
        double xvnhuan = 0;
        double speed = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("起跳");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill02");
                    }
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("起跳") && CreatureData.state(entity).tick() >= 8) {
            CreatureData.state(entity).setPhase("释放");
            entity.setDeltaMovement(new Vec3(0, 0.4, 0));
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 20) {
            R = 2;
            speed = 0.5;
            SimpleParticleType particle = null;
            if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                particle = Particles.type("smokedeeporange");
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                particle = Particles.type("smokelightgreen");
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                particle = Particles.type("smokegolden");
            }
            particlenumber = 30;
            xvnhuan = 0;
            shengchengY = entity.getY() + entity.getBbHeight() * 0.5;
            xiangliangY = 0;
            for (int index0 = 0; index0 < (int) (particlenumber); index0++) {
                shengchengX = entity.getX() + R * Math.cos(xvnhuan);
                shengchengZ = entity.getZ() + R * Math.sin(xvnhuan);
                xiangliangX = speed * Math.cos(xvnhuan);
                xiangliangZ = speed * Math.sin(xvnhuan);
                if (world instanceof ServerLevel _level && particle != null)
                    Particles.directed(_level, particle, shengchengX, shengchengY, shengchengZ, xiangliangX, xiangliangY, xiangliangZ, speed);
                xvnhuan = xvnhuan + 360 / particlenumber;
            }
            if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8, 2, 8), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (entityiterator.onGround()) {
                                if (world instanceof ServerLevel _level)
                                    _level.sendParticles(Particles.type("smallexplosiondeeporange"), (entityiterator.getX()), (entityiterator.getY() + entity.getBbHeight() / 2), (entityiterator.getZ()), 1, 0,
                                            0, 0, 0);
                                SpellDamage.deal(entityiterator, DamageCategory.SOIL, entity, false, 5, 1.3);
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 80, 1));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                                if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                    entityiterator.setDeltaMovement(new Vec3(0, 0.5, 0));
                                }
                            }
                        }
                    }
                }
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8, 2, 8), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionlightgreen"), (entityiterator.getX()), (entityiterator.getY() + entity.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0,
                                        0, 0);
                            SpellDamage.deal(entityiterator, DamageCategory.WIND, entity, false, 4, 0.8);
                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 1), 0.5, ((entityiterator.getZ() - entity.getZ()) * 1)));
                            }
                            if (entityiterator.onGround()) {
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_001"), 40, 0));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 40, 0));
                            }
                        }
                    }
                }
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                SkillDelays.after(5, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                SkillDelays.after(10, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                SkillDelays.after(15, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                SkillDelays.after(20, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getBbWidth() + 7) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionyellow"), (entityiterator.getX()), (entityiterator.getY() + entity.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0, 0,
                                        0);
                            SpellDamage.deal(entityiterator, DamageCategory.THUNDER, entity, false, 5, 1.2);
                            if (entityiterator.onGround()) {
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 40, 0));
                            }
                        }
                    }
                }
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
