package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;



/** 017蜕壳动物形态的独立来源逐刻与长壳流程。 */
final class Yaoshou01701Behavior {
    private Yaoshou01701Behavior() { }

    /** 来源 Yaoshou01701DangShiTiGengXinKeShiProcedure：保留阶段顺序与数值。 */
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
                                    if (jvli2 > 12) {
                                        if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.WEAKNESS) : false)) {
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "长壳");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 20);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 80);
                                        }
                                    }
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "走位");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 50);
                                        if (jvli2 <= 7) {
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "魂灵泡沫");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "逃跑");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        if (entity instanceof TamableAnimal _tamEnt ? _tamEnt.isTame() : false) {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 100);
                                        } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5) {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 60);
                                        } else {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        }
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "走位");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "魂灵泡沫");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "逃跑");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 60);
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
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("挣脱")) {
                            Yaoshou037054Support.escape(entity);
                        } else if ((CreatureData.state(entity).skill()).equals("普攻")) {
                            if ((CreatureData.state(entity).phase()).equals("")) {
                                CreatureData.state(entity).setPhase("准备");
                                if (!entity.level().isClientSide) {
                                    if (entity instanceof Yaoshou01701Entity) {
                                        ((Yaoshou01701Entity) entity).setAnimation("animation.yaoshou01701.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 3) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
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
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 8) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("长壳")) {
                            Yaoshou017zhangke(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("走位")) {
                            Yaoshou017zouwei(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("魂灵泡沫")) {
                            Yaoshou017hunlingpaomo(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("逃跑")) {
                            Yaoshou017taopao(world, entity);
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

    /** 来源 Yaoshou017taopaoProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017taopao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double jvli1 = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("准备");
        } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 5) {
            jiaodu = entity.getYRot() + Yaoshou037054Support.randomInt(160, 200);
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
            CreatureData.state(entity).setPhase("逃跑");
        } else if ((CreatureData.state(entity).phase()).equals("逃跑") && CreatureData.state(entity).tick() >= 20) {
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("逃跑")) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0, (entity.getLookAngle().z)));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokedeeppurple"), (entity.getX()), (entity.getY()), (entity.getZ()), 5, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.1);
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 25) {
            Yaoshou037054Support.finish(entity);
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 >= 16) {
                CreatureData.state(entity).setAttackerUuid("");
                CreatureData.state(entity).setTempAttackerUuid("");
            }
        }
    }

    /** 来源 Yaoshou017hunlingpaomoProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017hunlingpaomo(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 3) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            CreatureData.state(entity).setPhase("第一击");
        } else if ((CreatureData.state(entity).phase()).equals("第一击") && CreatureData.state(entity).tick() >= 6) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            CreatureData.state(entity).setPhase("第二击");
        } else if ((CreatureData.state(entity).phase()).equals("第二击") && CreatureData.state(entity).tick() >= 9) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            CreatureData.state(entity).setPhase("第三击");
        } else if ((CreatureData.state(entity).phase()).equals("第三击") && CreatureData.state(entity).tick() >= 12) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            CreatureData.state(entity).setPhase("第四击");
        } else if ((CreatureData.state(entity).phase()).equals("第四击") && CreatureData.state(entity).tick() >= 15) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 20) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou017zouweiProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017zouwei(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            CreatureData.state(entity).setPhase("准备");
        } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 5) {
            if (entity instanceof Mob _entity)
                _entity.getNavigation().moveTo((entity.getX() + Yaoshou037054Support.randomInt(-10, 10)), (entity.getY()), (entity.getZ() + Yaoshou037054Support.randomInt(-10, 10)), 2);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 25) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou017zhangkeProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017zhangke(LevelAccessor world, Entity entity) {
        if ((CreatureData.state(entity).phase()).equals("")) {
            CreatureData.state(entity).setPhase("长壳");
        } else if ((CreatureData.state(entity).phase()).equals("长壳") && CreatureData.state(entity).tick() >= 60) {

            if (world instanceof ServerLevel _serverLevelForEntitySpawn) {
                Entity _entityForSpawning = new Yaoshou01701Entity(CreatureSetup.<Yaoshou01701Entity>entityType("yaoshou_01701").get(), _serverLevelForEntitySpawn);
                _entityForSpawning.moveTo((entity.getX()), (entity.getY()), (entity.getZ()), world.getRandom().nextFloat() * 360F, 0);
                Relations.shareEntityAllegiance(entity, _entityForSpawning);
                EntityAttributes.setRealmName(_entityForSpawning, (EntityAttributes.realmName(entity)));
                EntityAttributes.setLevel(_entityForSpawning, (EntityAttributes.level(entity)));
                EntityAttributes.setExperienceMax(_entityForSpawning, (EntityAttributes.experienceMax(entity)));
                EntityAttributes.setExperience(_entityForSpawning, (EntityAttributes.experience(entity)));
                CreatureData.state(_entityForSpawning).setExperienceStand((CreatureData.state(entity).experienceStand()));
                CreatureData.state(_entityForSpawning).setExperienceRate((CreatureData.state(entity).experienceRate()));
                CreatureData.state(_entityForSpawning).setExperienceHinder((CreatureData.state(entity).experienceHinder()));
                ((LivingEntity) _entityForSpawning).getAttribute(YaoshouAttributes.SIZE).setBaseValue(((LivingEntity) entity).getAttribute(YaoshouAttributes.SIZE).getValue());
                CreatureData.state(_entityForSpawning).setTameNum((CreatureData.state(entity).tameNum()));
                CreatureData.state(_entityForSpawning).setCharacter((CreatureData.state(entity).character()));
                CreatureData.state(_entityForSpawning).setCharacterDef((CreatureData.state(entity).characterDef()));
                CreatureData.state(_entityForSpawning).setCharacterAtt((CreatureData.state(entity).characterAtt()));
                CreatureData.state(_entityForSpawning).setCharacterInterval((CreatureData.state(entity).characterInterval()));
                CreatureData.state(_entityForSpawning).setCharacterResist((CreatureData.state(entity).characterResist()));
                EntityAttributes.setRating(_entityForSpawning, Stat.HEALTH_MAX, (EntityAttributes.rating(entity, Stat.HEALTH_MAX)));
                EntityAttributes.setStat(_entityForSpawning, Stat.HEALTH_MAX, (EntityAttributes.stat(entity, Stat.HEALTH_MAX)));
                ((LivingEntity) _entityForSpawning).getAttribute(Attributes.MAX_HEALTH)
                        .setBaseValue(((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).getValue());
                if (_entityForSpawning instanceof LivingEntity _entity)
                    _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1);
                EntityAttributes.setRating(_entityForSpawning, Stat.HEALTH_REC, (EntityAttributes.rating(entity, Stat.HEALTH_REC)));
                EntityAttributes.setStat(_entityForSpawning, Stat.HEALTH_REC, (EntityAttributes.stat(entity, Stat.HEALTH_REC)));
                EntityAttributes.setRating(_entityForSpawning, Stat.POWER_MAX, (EntityAttributes.rating(entity, Stat.POWER_MAX)));
                EntityAttributes.setStat(_entityForSpawning, Stat.POWER_MAX, (EntityAttributes.stat(entity, Stat.POWER_MAX)));
                EntityAttributes.setRating(_entityForSpawning, Stat.POWER_REC, (EntityAttributes.rating(entity, Stat.POWER_REC)));
                EntityAttributes.setStat(_entityForSpawning, Stat.POWER_REC, (EntityAttributes.stat(entity, Stat.POWER_REC)));
                EntityAttributes.setRating(_entityForSpawning, Stat.SOUL_MAX, (EntityAttributes.rating(entity, Stat.SOUL_MAX)));
                EntityAttributes.setStat(_entityForSpawning, Stat.SOUL_MAX, (EntityAttributes.stat(entity, Stat.SOUL_MAX)));
                EntityAttributes.setRating(_entityForSpawning, Stat.SOUL_REC, (EntityAttributes.rating(entity, Stat.SOUL_REC)));
                EntityAttributes.setStat(_entityForSpawning, Stat.SOUL_REC, (EntityAttributes.stat(entity, Stat.SOUL_REC)));
                EntityAttributes.setRating(_entityForSpawning, Stat.ATTACK, (EntityAttributes.rating(entity, Stat.ATTACK)));
                EntityAttributes.setStat(_entityForSpawning, Stat.ATTACK, (EntityAttributes.stat(entity, Stat.ATTACK)));
                ((LivingEntity) _entityForSpawning).getAttribute(Attributes.ATTACK_DAMAGE)
                        .setBaseValue(((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                EntityAttributes.setRating(_entityForSpawning, Stat.ATTACK_DEF, "强劲");
                EntityAttributes.setStat(_entityForSpawning, Stat.ATTACK_DEF, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / 2.7, 2)));
                EntityAttributes.setRating(_entityForSpawning, Stat.MANA_ATT, (EntityAttributes.rating(entity, Stat.MANA_ATT)));
                EntityAttributes.setStat(_entityForSpawning, Stat.MANA_ATT, (EntityAttributes.stat(entity, Stat.MANA_ATT)));
                EntityAttributes.setRating(_entityForSpawning, Stat.MANA_DEF, "勉强");
                EntityAttributes.setStat(_entityForSpawning, Stat.MANA_DEF, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / 2.9, 2)));
                EntityAttributes.setRating(_entityForSpawning, Stat.SOUL_ATT, (EntityAttributes.rating(entity, Stat.SOUL_ATT)));
                EntityAttributes.setStat(_entityForSpawning, Stat.SOUL_ATT, (EntityAttributes.stat(entity, Stat.SOUL_ATT)));
                EntityAttributes.setRating(_entityForSpawning, Stat.SOUL_DEF, "强劲");
                EntityAttributes.setStat(_entityForSpawning, Stat.SOUL_DEF, Math.ceil(2 + Math.pow(EntityAttributes.level(entity) / 2.7, 2)));
                EntityAttributes.setRating(_entityForSpawning, Stat.MOVE_SPEED, (EntityAttributes.rating(entity, Stat.MOVE_SPEED)));
                EntityAttributes.setStat(_entityForSpawning, Stat.MOVE_SPEED, (EntityAttributes.stat(entity, Stat.MOVE_SPEED)));
                ((LivingEntity) _entityForSpawning).getAttribute(Attributes.MOVEMENT_SPEED)
                        .setBaseValue(((LivingEntity) entity).getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                if (_entityForSpawning instanceof Mob _mobForSpawning)
                    _mobForSpawning.finalizeSpawn(_serverLevelForEntitySpawn, world.getCurrentDifficultyAt(_entityForSpawning.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                world.addFreshEntity(_entityForSpawning);
            }
            CreatureData.state(entity).setSkill("");
            CreatureData.state(entity).setPhase("");
            CreatureData.state(entity).setTick(0);
            if (!entity.level().isClientSide())
                entity.discard();
        } else if ((CreatureData.state(entity).phase()).equals("长壳")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SOUL, (entity.getX()), (entity.getY()), (entity.getZ()), 5, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokeblack"), (entity.getX()), (entity.getY()), (entity.getZ()), 3, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
        }
    }
}
