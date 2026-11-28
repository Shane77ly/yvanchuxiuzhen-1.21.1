package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;


/** 016号妖兽的完整来源逐刻技能闭包。 */
final class Yaoshou016Behavior {
    private Yaoshou016Behavior() { }

    /** 来源 Yaoshou016DangShiTiGengXinKeShiProcedure：保留阶段顺序、条件和数值。 */
    static void tick(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli2 = 0;
        double jvli1 = 0;
        double qvanzhongdiejia = 0;
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
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "潜地");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "喷射毒液");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 50);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "喷射毒液");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "撤退");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    }
                                }
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false) && jvli2 < 3) {
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 10);
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
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("挣脱")) {
                            Yaoshou037054Support.escape(entity);
                        } else if ((CreatureData.state(entity).skill()).equals("普攻")) {
                            if ((CreatureData.state(entity).phase()).equals("")) {
                                CreatureData.state(entity).setPhase("准备");
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_016")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou016.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 1) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getZ();
                                CreatureData.state(entity).setPhase("攻击");
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 4) {
                                {
                                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
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
                                            if (entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("duyao_11")) : false) {
                                                if (entityiterator instanceof LivingEntity _entity)
                                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_11"),
                                                            (int) ((entityiterator instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("duyao_11"))
                                                                    ? _livEnt.getEffect(StatusEffects.holder("duyao_11")).getDuration()
                                                                    : 0) + 400),
                                                            (int) (1 + (entityiterator instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("duyao_11"))
                                                                    ? _livEnt.getEffect(StatusEffects.holder("duyao_11")).getAmplifier()
                                                                    : 0))));
                                            } else {
                                                if (entityiterator instanceof LivingEntity _entity)
                                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_11"), 400, 0));
                                            }
                                        }
                                    }
                                }
                                CreatureData.state(entity).setPhase("");
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("潜地")) {
                            Yaoshou016qiandi(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("喷射毒液")) {
                            Yaoshou016pensheduye(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("撤退")) {
                            Yaoshou013chetui(world, entity);
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

    /** 来源 Yaoshou013chetuiProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou013chetui(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            if (entity instanceof Mob _entity)
                _entity.getNavigation().moveTo((entity.getX() + Yaoshou037054Support.randomInt(-12, 12)), (entity.getY()), (entity.getZ() + Yaoshou037054Support.randomInt(-12, 12)), 2.2);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 44) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou016pensheduyeProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou016pensheduye(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_016")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou016.skill01");
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback, byte piercing) {
                        AbstractArrow entityToSpawn = new Yaoshou016duyeEntity(CreatureSetup.<Yaoshou016duyeEntity>entityType("projectile_yaoshou_016duye").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou016duyeEntity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        ((Yaoshou016duyeEntity) entityToSpawn).setSourcePierceLevel(piercing);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 0, (byte) 4);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 0);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 25) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou016qiandiProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou016qiandi(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("潜入");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_016")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou016.skill02");
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("潜入") && CreatureData.state(entity).tick() >= 32) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, (false), (false)));
            CreatureData.state(entity).setPhase("地下移动");
        } else if ((CreatureData.state(entity).phase()).equals("地下移动") && CreatureData.state(entity).tick() >= 132) {
            if (entity instanceof LivingEntity _entity)
                _entity.removeEffect(MobEffects.INVISIBILITY);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("地下移动")) {
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 <= 2) {
                {
                    Entity _ent = entity;
                    _ent.teleportTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()));
                    if (_ent instanceof ServerPlayer _serverPlayer)
                        _serverPlayer.connection.teleport((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), _ent.getYRot(), _ent.getXRot());
                }
                entity.setDeltaMovement(new Vec3(0, 0.5, 0));
                if (!entity.level().isClientSide) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_016")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou016.skill03");
                    }
                }
                CreatureData.state(entity).setPhase("出洞");
                SkillDelays.after(2, () -> {
                        if (entity instanceof LivingEntity _entity)
                            _entity.removeEffect(MobEffects.INVISIBILITY);

                });
            } else {
                if (entity instanceof Mob _entity)
                    _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 1);
            }
        } else if ((CreatureData.state(entity).phase()).equals("出洞") && CreatureData.state(entity).tick() >= 32) {
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiondarkgreen"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0,
                                    0, 0, 0);
                        SpellDamage.deal(entityiterator, DamageCategory.TREE, entity, false, 5, 1.2);
                        if (entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("duyao_11")) : false) {
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_11"),
                                        (int) ((entityiterator instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("duyao_11")) ? _livEnt.getEffect(StatusEffects.holder("duyao_11")).getDuration() : 0) + 400),
                                        (int) (1 + (entityiterator instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("duyao_11"))
                                                ? _livEnt.getEffect(StatusEffects.holder("duyao_11")).getAmplifier()
                                                : 0))));
                        } else {
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_11"), 400, 0));
                        }
                    }
                }
            }
            CreatureData.state(entity).setPhase("尾声");
        } else if ((CreatureData.state(entity).phase()).equals("尾声")) {
            CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
            if (CreatureData.state(entity).tick2() >= 13) {
                CreatureData.state(entity).setPhase("结束");
                CreatureData.state(entity).setTick2(0);
            }
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 32) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou016duyeDangTouZhiWuJiZhongShiTiShiProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou016duyeDangTouZhiWuJiZhongShiTiShi(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (!Relations.isFriendly(sourceentity, entity)) {
            SpellDamage.deal(entity, DamageCategory.TREE, sourceentity, false, 2, 0.5);
            if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("duyao_11")) : false) {
                if (entity instanceof LivingEntity _entity)
                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_11"),
                            (int) ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("duyao_11")) ? _livEnt.getEffect(StatusEffects.holder("duyao_11")).getDuration() : 0) + 400),
                            (int) (1 + (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("duyao_11")) ? _livEnt.getEffect(StatusEffects.holder("duyao_11")).getAmplifier() : 0))));
            } else {
                if (entity instanceof LivingEntity _entity)
                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_11"), 400, 0));
            }
        }
    }

    /** 来源 Yaoshou016duyeDangTouSheWuSheZhongCiFangKuaiProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou016duyeDangTouSheWuSheZhongCiFangKuai(LevelAccessor world, Entity immediatesourceentity) {
        if (immediatesourceentity == null)
            return;
        Entity linshientity = null;
        double xvnhuan = 0;
        xvnhuan = 1;
        for (int index0 = 0; index0 < (int) (4); index0++) {
            SkillDelays.after((int) xvnhuan, () -> {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("smokedarkgreen"), (immediatesourceentity.getX()), (immediatesourceentity.getY() + immediatesourceentity.getBbHeight() / 2),
                                (immediatesourceentity.getZ()), 8, 0, 0, 0, 0.2);

                });
            xvnhuan = xvnhuan + 10;
        }
    }

    /** 来源 Yaoshou016duyeTouZhiWuFeiXingTickProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou016duyeTouZhiWuFeiXingTick(LevelAccessor world, Entity immediatesourceentity) {
        if (immediatesourceentity == null)
            return;
        if (world instanceof ServerLevel _level)
            _level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, (immediatesourceentity.getX()), (immediatesourceentity.getY() + immediatesourceentity.getBbHeight() / 2), (immediatesourceentity.getZ()), 3, 0.2, 0, 0.2, 0);
    }
}
