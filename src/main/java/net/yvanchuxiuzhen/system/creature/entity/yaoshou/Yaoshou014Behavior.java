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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 014号妖兽来源的逐刻选择、近战与完整技能阶段。 */
final class Yaoshou014Behavior {
    private Yaoshou014Behavior() { }

    /** 来源 Yaoshou014DangShiTiShouShangShiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void hurt(LevelAccessor world, Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (entity.isAlive()) {
            if (!Relations.isFriendly(entity, sourceentity)) {
                CreatureData.state(entity).setTempAttackerUuid((sourceentity.getStringUUID()));
                if (CreatureData.state(entity).interrupted()) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                        ((YaoshouEntity) entity).setAnimation("empty");
                    }
                    CreatureData.state(entity).setInterrupted((false));
                }
                if ((CreatureData.state(entity).skill()).equals("")) {
                    if (!entity.level().isClientSide) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.attacked");
                        }
                    }
                }
                if (entity.isVehicle()) {
                    {
                        final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                        List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                .toList();
                        for (Entity entityiterator : _entfound) {
                            if (entityiterator.isPassenger()) {
                                entityiterator.stopRiding();
                            }
                        }
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou014DangShiTiGengXinKeShiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void tick(LevelAccessor world, Entity entity) {
        ItemStack friendlistitem = ItemStack.EMPTY;
        String team = "";
        boolean qingchu = false;
        Entity linshientity = null;
        double jvli2 = 0;
        double jvli1 = 0;
        double jvli3 = 0;
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
            if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_035")) : false) {
                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                    if (entity.isNoGravity()) {
                        entity.setNoGravity((false));
                        qingchu = true;
                    }
                    if (qingchu) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("brokenwhite"), (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.2);
                        qingchu = false;
                    }
                }
            } else {
                if (!entity.isNoGravity()) {
                    entity.setNoGravity((true));
                }
            }
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
                            jvli3 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
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
                                    if (jvli2 < 24) {
                                        if (!entity.isVehicle()) {
                                            if (jvli3 > 5) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "乱羽");
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 40);
                                            }
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "夜舞");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 40);
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "黑风");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 40);
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "飞羽");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 40);
                                        }
                                    }
                                    if (jvli2 > 6) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), Math.round(10 + jvli2));
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
                                        if (!entity.isVehicle()) {
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "飞离");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        }
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
                                CreatureData.state(entity).setDestX((linshientity.getX() + Yaoshou037054Support.randomInt(-7, 7)));
                                if (linshientity.onGround()) {
                                    CreatureData.state(entity).setDestY((linshientity.getY() + linshientity.getBbHeight() / 2 + 8));
                                } else {
                                    CreatureData.state(entity).setDestY((linshientity.getY() + linshientity.getBbHeight() / 2));
                                }
                                CreatureData.state(entity).setDestZ((linshientity.getZ() + Yaoshou037054Support.randomInt(-7, 7)));
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((CreatureData.state(entity).destX()), (CreatureData.state(entity).destY()), (CreatureData.state(entity).destZ())));
                                CreatureData.state(entity).setPhase("移动中");
                            } else if ((CreatureData.state(entity).phase()).equals("移动中") && CreatureData.state(entity).tick() >= 40) {
                                Yaoshou037054Support.finish(entity);
                            } else if ((CreatureData.state(entity).phase()).equals("移动中")) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((CreatureData.state(entity).destX()), (CreatureData.state(entity).destY()), (CreatureData.state(entity).destZ())));
                                entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), (entity.getLookAngle().y), (entity.getLookAngle().z)));
                                jvli1 = Math.sqrt(Math.pow(entity.getX() - CreatureData.state(entity).destX(), 2) + Math.pow(entity.getY() - CreatureData.state(entity).destY(), 2)
                                        + Math.pow(entity.getZ() - CreatureData.state(entity).destZ(), 2));
                                if (jvli1 <= 2) {
                                    Yaoshou037054Support.finish(entity);
                                }
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("挣脱")) {
                            Yaoshou037054Support.escape(entity);
                        } else if ((CreatureData.state(entity).skill()).equals("普攻")) {
                            if ((CreatureData.state(entity).phase()).equals("")) {
                                CreatureData.state(entity).setPhase("准备");
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 3) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
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
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 12) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("黑风")) {
                            Yaoshou014heifeng(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("夜舞")) {
                            Yaoshou047yewu(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("乱羽")) {
                            Yaoshou047luanyv(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("飞羽")) {
                            Yaoshou014feiyv(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("飞离")) {
                            Yaoshou047feili(world, entity);
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
        } else {
            if (entity.isNoGravity()) {
                entity.setNoGravity((false));
            }
            entity.setDeltaMovement(new Vec3(0, (-0.5), 0));
        }
    }

    /** 来源 Yaoshou047feiliProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047feili(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("准备");
        } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 2) {
            {
                Entity _ent = entity;
                _ent.setYRot((float) (entity.getYRot() + 180));
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
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 30) {
            Yaoshou037054Support.finish(entity);
        } else if ((CreatureData.state(entity).phase()).equals("结束")) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z)));
        }
    }

    /** 来源 Yaoshou014feiyvProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou014feiyv(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double linshidx = 0;
        double constZ = 0;
        double jiaodu = 0;
        double constX = 0;
        double linshidz = 0;
        double linshidy = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("飞箭");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.skill03");
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("飞箭") && CreatureData.state(entity).tick() >= 9) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            jiaodu = entity.getYRot();
            jiaodu = jiaodu + 60;
            for (int index0 = 0; index0 < (int) (9); index0++) {
                constX = entity.getX() + Math.cos(jiaodu * (Math.PI / 180));
                constZ = entity.getZ() + Math.sin(jiaodu * (Math.PI / 180));
                linshidx = 3 * Math.cos(jiaodu * (Math.PI / 180));
                linshidy = 3 * Math.sin((-(entity.getXRot())) * (Math.PI / 180));
                linshidz = 3 * Math.sin(jiaodu * (Math.PI / 180));
                if (world instanceof ServerLevel projectileLevel) {
                    Yaoshou014skill03Entity _entityToSpawn = CreatureSetup.<Yaoshou014skill03Entity>entityType("projectile_yaoshou_014skill_03").get().create(projectileLevel);
                    _entityToSpawn.setOwner(entity);
                    _entityToSpawn.configure(((LivingEntity) entity).getAttributeValue(Attributes.ATTACK_DAMAGE), 1);
                    _entityToSpawn.setPos(constX, (entity.getY() + 1), constZ);
                    _entityToSpawn.shoot(linshidx, linshidy, linshidz, 1, 0);
                    projectileLevel.addFreshEntity(_entityToSpawn);
                }
                jiaodu = jiaodu + 15;
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 18) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou047luanyvProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047luanyv(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou047.skill04");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.skill01");
                    }
                }
            }
            CreatureData.state(entity).setPhase("俯冲");
        } else if ((CreatureData.state(entity).phase()).equals("俯冲") && CreatureData.state(entity).tick() >= 8) {
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getBbWidth() + 2) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 20, 0, 0, 0, 1);
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
            CreatureData.state(entity).setPhase("拔升");
        } else if ((CreatureData.state(entity).phase()).equals("拔升") && CreatureData.state(entity).tick() >= 16) {
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        } else if ((CreatureData.state(entity).phase()).equals("俯冲")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.setDeltaMovement(new Vec3((-(1.5 * Math.sin(entity.getYRot() * (Math.PI / 180)))), (-(1.5 * Math.sin(entity.getXRot() * (Math.PI / 180)))), (1.5 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
        } else if ((CreatureData.state(entity).phase()).equals("拔升")) {
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(entity.getYRot() * (Math.PI / 180)))), 0.5, (1 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
        }
    }

    /** 来源 Yaoshou047yewuProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047yewu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double r = 0;
        double mubiaoY = 0;
        double jiaodu = 0;
        double mubiaoZ = 0;
        double mubiaoX = 0;
        double jvli1 = 0;
        double jvli2 = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setRandomCap((Yaoshou037054Support.randomInt(60, 120)));
            CreatureData.state(entity).setPhase("盘旋");
        } else if ((CreatureData.state(entity).phase()).equals("结束")) {
            Yaoshou037054Support.finish(entity);
        } else if ((CreatureData.state(entity).phase()).equals("盘旋")) {
            r = 10;
            jiaodu = CreatureData.state(entity).tick() * 3;
            mubiaoX = linshientity.getX() + r * Math.cos(jiaodu * (Math.PI / 180));
            mubiaoZ = linshientity.getZ() + r * Math.sin(jiaodu * (Math.PI / 180));
            if (linshientity.onGround()) {
                mubiaoY = linshientity.getY() + 10;
            } else {
                mubiaoY = linshientity.getY() + 3;
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(mubiaoX, mubiaoY, mubiaoZ));
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.5), (entity.getLookAngle().y * 0.5), (entity.getLookAngle().z * 0.5)));
            if (CreatureData.state(entity).tick() >= CreatureData.state(entity).randomCap()) {
                if (!entity.level().isClientSide) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.skill02");
                        }
                    } else if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou047.skill04");
                        }
                    }
                }
                CreatureData.state(entity).setPhase("俯冲");
            }
        } else if ((CreatureData.state(entity).phase()).equals("俯冲")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 1.5), (entity.getLookAngle().y * 1.5), (entity.getLookAngle().z * 1.5)));
            CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
            if (CreatureData.state(entity).tick2() >= 8) {
                CreatureData.state(entity).setPhase("拔升");
                CreatureData.state(entity).setViewX((entity.getLookAngle().x));
                CreatureData.state(entity).setViewZ((entity.getLookAngle().z));
                CreatureData.state(entity).setTick2(0);
            }
            if (!entity.isVehicle()) {
                jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                jvli2 = jvli1 - (entity.getBbWidth() / 2 + linshientity.getBbWidth() / 2);
                if (jvli2 <= 2) {
                    linshientity.startRiding(entity);
                    CreatureData.state(entity).setPhase("拔升");
                    CreatureData.state(entity).setViewX((entity.getLookAngle().x));
                    CreatureData.state(entity).setViewZ((entity.getLookAngle().z));
                    CreatureData.state(entity).setTick2(0);
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("拔升")) {
            CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
            if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                if (CreatureData.state(entity).tick2() >= 34) {
                    CreatureData.state(entity).setPhase("结束");
                    CreatureData.state(entity).setTick2(0);
                }
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                if (CreatureData.state(entity).tick2() >= 8) {
                    CreatureData.state(entity).setPhase("结束");
                    CreatureData.state(entity).setTick2(0);
                }
            }
            entity.setDeltaMovement(new Vec3((CreatureData.state(entity).viewX() * 1.5), 0.5, (CreatureData.state(entity).viewZ() * 1.5)));
        }
    }

    /** 来源 Yaoshou014heifengProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou014heifeng(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("开始吹风");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.skill04");
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("开始吹风") && CreatureData.state(entity).tick() >= 8) {
            if (world instanceof ServerLevel) Skills.launchWindBlade(entity, linshientity, EntityAttributes.stat(entity, Stat.MANA_ATT) * 1.5);
            CreatureData.state(entity).setPhase("第二次吹风");
        } else if ((CreatureData.state(entity).phase()).equals("第二次吹风") && CreatureData.state(entity).tick() >= 32) {
            if (world instanceof ServerLevel) Skills.launchWindBlade(entity, linshientity, EntityAttributes.stat(entity, Stat.MANA_ATT) * 1.5);
            CreatureData.state(entity).setPhase("第三次吹风");
        } else if ((CreatureData.state(entity).phase()).equals("第三次吹风") && CreatureData.state(entity).tick() >= 44) {
            if (world instanceof ServerLevel) Skills.launchWindBlade(entity, linshientity, EntityAttributes.stat(entity, Stat.MANA_ATT) * 1.5);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 50) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
