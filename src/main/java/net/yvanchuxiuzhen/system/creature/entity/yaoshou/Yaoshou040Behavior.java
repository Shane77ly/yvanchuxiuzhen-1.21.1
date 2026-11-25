package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSummons;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 040号妖兽的来源逐刻选择与技能阶段。 */
final class Yaoshou040Behavior {
    private Yaoshou040Behavior() { }

    /** 来源 Yaoshou040DangShiTiGengXinKeShiProcedure：保留阶段条件、执行次序与数值。 */
    static void tick(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double xvnhuan = 0;
        double jvli2 = 0;
        double jvli1 = 0;
        double qvanzhongdiejia = 0;
        double suijishu = 0;
        double qvanzhongmax = 0;
        double shijiaoX = 0;
        double xvnhuan2 = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double shunxvjishu = 0;
        double buchong = 0;
        if (entity.isAlive()) {
            CreatureData.state(entity).setTick((CreatureData.state(entity).tick() + 1));
            if ((CreatureData.state(entity).skill()).equals("")) {
                if (!(CreatureData.state(entity).attackerUuid()).equals(CreatureData.state(entity).tempAttackerUuid())) {
                    CreatureData.state(entity).setAttackerUuid((CreatureData.state(entity).tempAttackerUuid()));
                }
            }
            if (!(CreatureData.state(entity).attackerUuid()).equals("")) {
                linshientity = Yaoshou039040Support.target(world, CreatureData.state(entity).attackerUuid());
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
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "万丈光芒");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "落阳");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "游离光");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "雷柱");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        if (jvli2 > 8) {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 60);
                                        } else {
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        }
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
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
                            suijishu = Yaoshou037054Support.randomInt( 1, (int) qvanzhongmax);
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
                                    _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY() + 12), (linshientity.getZ()), 1.5);
                                CreatureData.state(entity).setPhase("移动中");
                            } else if ((CreatureData.state(entity).phase()).equals("移动中") && CreatureData.state(entity).tick() >= 40) {
                                YaoshouShared.finishSkill((YaoshouEntity) entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("挣脱")) {
                            YaoshouShared.escape((YaoshouEntity) entity);
                        } else if ((CreatureData.state(entity).skill()).equals("普攻")) {
                            if ((CreatureData.state(entity).phase()).equals("")) {
                                CreatureData.state(entity).setPhase("准备");
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
                                            entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) EntityAttributes.stat(entity, Stat.ATTACK));
                                        }
                                    }
                                }
                                CreatureData.state(entity).setPhase("攻击");
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 12) {
                                YaoshouShared.finishSkill((YaoshouEntity) entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("万丈光芒")) {
                            Yaoshou040wanzhangguangmang(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("落阳")) {
                            Yaoshou040luoyang(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("游离光")) {
                            Yaoshou040youliguang(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("雷柱")) {
                            Yaoshou040guangzhu(world, entity);
                        }
                    }
                } else {
                    CreatureData.state(entity).setAttackerUuid("");
                    CreatureData.state(entity).setTempAttackerUuid("");
                    YaoshouShared.finishSkill((YaoshouEntity) entity);
                }
            } else {
                if (CreatureData.state(entity).tick() >= 40) {
                    if (entity instanceof Mob _entity)
                        _entity.getNavigation().moveTo((entity.getX() + Yaoshou037054Support.randomInt( -3, 3)), (entity.getY()), (entity.getZ() + Yaoshou037054Support.randomInt( -3, 3)), 1);
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

    /** 来源 Yaoshou040wanzhangguangmangProcedure：保留阶段条件、执行次序与数值。 */
    static void Yaoshou040wanzhangguangmang(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double xvnhuan = 0;
        linshientity = Yaoshou039040Support.target(world, CreatureData.state(entity).attackerUuid());
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel) {
                Skills.showEntityDamageArea(entity, entity.position(), "animation.shanghaifanwei.shiliuge3", 60);
            }
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 60) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("cailiao_229particle"), (entity.getX()), (entity.getY()), (entity.getZ()), 128, 0, 0, 0, 1.5);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(28 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("bigbanglightyellow"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0, 0,
                                    0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "光" + " " + entity.getStringUUID() + " " + "false" + " " + "20" + " " + "3"));
                        }
                    }
                }
            }
            xvnhuan = 1;
            for (int index0 = 0; index0 < (int) (8); index0++) {
                SkillDelays.after((int) xvnhuan, () -> {

                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("bigbangyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 12, 12, 12, 12, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("bigbanglightyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 12, 12, 12, 12, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosionyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 24, 20, 20, 20, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosionlightyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 24, 20, 20, 20, 0);
                });
                xvnhuan = xvnhuan + 5;
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 62) {
            YaoshouShared.finishSkill((YaoshouEntity) entity);
        }

    }

    /** 来源 Yaoshou040luoyangProcedure：保留阶段条件、执行次序与数值。 */
    static void Yaoshou040luoyang(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou039040Support.target(world, CreatureData.state(entity).attackerUuid());
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.setShiftKeyDown((true));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 20) {
            if (world instanceof ServerLevel level) {
                Yaoshou040Skills.fallingSun(level, entity, linshientity.position().add(0, 16, 0));
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 40) {
            entity.setShiftKeyDown((false));
            YaoshouShared.finishSkill((YaoshouEntity) entity);
        }

    }

    /** 来源 Yaoshou040youliguangProcedure：保留阶段条件、执行次序与数值。 */
    static void Yaoshou040youliguang(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou039040Support.target(world, CreatureData.state(entity).attackerUuid());
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("释放");
            entity.setShiftKeyDown((true));
        } else if ((CreatureData.state(entity).phase()).equals("释放") && CreatureData.state(entity).tick() >= 100) {
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("释放")) {
            CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
            if (CreatureData.state(entity).tick2() >= 2) {
                if (world instanceof ServerLevel level) {
                    Yaoshou040skill0301Entity projectile = Yaoshou040Skills.projectile(level, entity);
                    projectile.setPos(entity.getX(), entity.getY(), entity.getZ());
                    projectile.shoot(Yaoshou037054Support.randomDouble(-1, 1), Yaoshou037054Support.randomDouble(-0.3, 0),
                            Yaoshou037054Support.randomDouble(-1, 1), 0.3F, 10);
                    level.addFreshEntity(projectile);
                }
                CreatureData.state(entity).setTick2(0);
            }
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 44) {
            entity.setShiftKeyDown((false));
            YaoshouShared.finishSkill((YaoshouEntity) entity);
        }

    }

    /** 来源 Yaoshou040guangzhuProcedure：保留阶段条件、执行次序与数值。 */
    static void Yaoshou040guangzhu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou039040Support.target(world, CreatureData.state(entity).attackerUuid());
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
            entity.setShiftKeyDown((true));
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 20) {
            if (world instanceof ServerLevel level) {
                CultivationSummons.summonLightPillars(level, entity, linshientity,
                        EntityAttributes.stat(entity, Stat.MANA_ATT));
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 40) {
            entity.setShiftKeyDown((false));
            YaoshouShared.finishSkill((YaoshouEntity) entity);
        }

    }
}
