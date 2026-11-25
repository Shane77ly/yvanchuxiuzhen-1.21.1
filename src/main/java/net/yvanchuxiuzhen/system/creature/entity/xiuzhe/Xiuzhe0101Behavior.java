package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

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
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 修者来源的目标记录、技能权重、准备阶段、普攻和33技能调度。 */
final class Xiuzhe0101Behavior {
    private Xiuzhe0101Behavior() { }

    /** 来源Xiuzhe0101DangShiTiGengXinKeShiProcedure，保持条件、数值与阶段顺序。 */
    static void tick(LevelAccessor world, Entity entity) {

        Entity linshientity = null;
        double jvli1 = 0;
        double suiji = 0;
        double gaoducha = 0;
        double qvanzhongmax = 0;
        double jvli2 = 0;
        double qvanzhongdiejia = 0;
        double jinengxvnhuan = 0;
        double xvnhuan2 = 0;
        double shunxvjishu = 0;
        double xvnhuan = 0;
        double suijishu = 0;
        if (entity.isAlive()) {
            CreatureData.state(entity).setTick((CreatureData.state(entity).tick() + 1));
            if ((CreatureData.state(entity).skill()).equals("")) {
                if (!(CreatureData.state(entity).attackerUuid()).equals(CreatureData.state(entity).tempAttackerUuid())) {
                    CreatureData.state(entity).setAttackerUuid((CreatureData.state(entity).tempAttackerUuid()));
                }
            }
            if (!(CreatureData.state(entity).attackerUuid()).equals("")) {
                linshientity = Xiuzhe0101Support.target(world, entity);
            }
            if (!(linshientity == null)) {
                if (linshientity.isAlive()) {
                    if ((CreatureData.state(entity).skill()).equals("")) {
                        if ((CreatureData.state(entity).queuedSkill()).equals("")) {
                            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                            jvli2 = jvli1 - (entity.getBbWidth() / 2 + linshientity.getBbWidth() / 2);
                            gaoducha = linshientity.getY() - entity.getY();
                            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            shunxvjishu = 0;
                            if (entity.isPassenger()) {
                                shunxvjishu = shunxvjishu + 1;
                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "挣脱载具");
                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                            } else {
                                if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0) >= 4) {
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "后空翻");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "左翻滚");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "右翻滚");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "前翻滚");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "回血丹药");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 50);
                                }
                                if (gaoducha >= 2) {
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "蹬墙踢");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "震山符");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "回血丹药");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                } else if (gaoducha <= -2) {
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "踏地");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "回血丹药");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "滑铲");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "撼地");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                } else {
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), Math.round(10 * jvli2));
                                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.1) {
                                        if (jvli2 >= 5) {
                                            jinengxvnhuan = 30;
                                            for (int index0 = 0; index0 < (int) (5); index0++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), (CreatureData.state(entity).skillChoice((int) (jinengxvnhuan))));
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                                jinengxvnhuan = jinengxvnhuan - 1;
                                            }
                                        } else {
                                            jinengxvnhuan = 25;
                                            for (int index1 = 0; index1 < (int) (5); index1++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), (CreatureData.state(entity).skillChoice((int) (jinengxvnhuan))));
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                                jinengxvnhuan = jinengxvnhuan + 1;
                                            }
                                        }
                                    } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5) {
                                        if (jvli2 >= 5) {
                                            jinengxvnhuan = 20;
                                            for (int index2 = 0; index2 < (int) (5); index2++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), (CreatureData.state(entity).skillChoice((int) (jinengxvnhuan))));
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                                jinengxvnhuan = jinengxvnhuan - 1;
                                            }
                                        } else {
                                            jinengxvnhuan = 15;
                                            for (int index3 = 0; index3 < (int) (5); index3++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), (CreatureData.state(entity).skillChoice((int) (jinengxvnhuan))));
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                                jinengxvnhuan = jinengxvnhuan + 1;
                                            }
                                        }
                                    } else {
                                        if (jvli2 >= 5) {
                                            jinengxvnhuan = 10;
                                            for (int index4 = 0; index4 < (int) (5); index4++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), (CreatureData.state(entity).skillChoice((int) (jinengxvnhuan))));
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                                jinengxvnhuan = jinengxvnhuan - 1;
                                            }
                                        } else {
                                            jinengxvnhuan = 5;
                                            for (int index5 = 0; index5 < (int) (5); index5++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), (CreatureData.state(entity).skillChoice((int) (jinengxvnhuan))));
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                                jinengxvnhuan = jinengxvnhuan + 1;
                                            }
                                        }
                                    }
                                }
                            }
                            xvnhuan = 1;
                            for (int index6 = 0; index6 < (int) (shunxvjishu); index6++) {
                                qvanzhongmax = qvanzhongmax + CreatureData.state(entity).candidateWeight((int) (xvnhuan));
                                xvnhuan = xvnhuan + 1;
                            }
                            suijishu = Xiuzhe0101Support.randomInt(1, (int) qvanzhongmax);
                            xvnhuan2 = 1;
                            for (int index7 = 0; index7 < (int) (shunxvjishu); index7++) {
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
                            for (int index8 = 0; index8 < (int) (CreatureData.state(entity).candidateCount()); index8++) {
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
                            } else if ((CreatureData.state(entity).phase()).equals("移动中") && CreatureData.state(entity).tick() >= 20) {
                                Xiuzhe0101Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("普攻")) {
                            basicAttack(world, entity);
                        }
                        castSkill(world, entity);
                    }
                } else {
                    CreatureData.state(entity).setAttackerUuid("");
                    CreatureData.state(entity).setTempAttackerUuid("");
                    Xiuzhe0101Support.finish(entity);
                }
            } else {
                if (CreatureData.state(entity).tick() >= 40) {
                    if (entity instanceof Mob _entity)
                        _entity.getNavigation().moveTo((entity.getX() + Xiuzhe0101Support.randomInt(-3, 3)), (entity.getY()), (entity.getZ() + Xiuzhe0101Support.randomInt(-3, 3)), 1);
                    CreatureData.state(entity).setTick(0);
                }
            }
        }
    }

    /** 来源Xiuzhe0101DangShiTiShouShangShiProcedure，保持条件、数值与阶段顺序。 */
    static void hurt(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (entity.isAlive()) {
            if (!Relations.isFriendly(entity, sourceentity)) {
                CreatureData.state(entity).setTempAttackerUuid((sourceentity.getStringUUID()));
                if (CreatureData.state(entity).interrupted()) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("empty");
                    }
                    CreatureData.state(entity).setInterrupted((false));
                }
                if ((CreatureData.state(entity).skill()).equals("")) {
                    if (Math.random() < 0.333) {
                        if (!entity.level().isClientSide) {
                            if (entity instanceof Xiuzhe0101Entity) {
                                ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.attacked01");
                            }
                        }
                    } else if (Math.random() < 0.5) {
                        if (!entity.level().isClientSide) {
                            if (entity instanceof Xiuzhe0101Entity) {
                                ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.attacked02");
                            }
                        }
                    } else {
                        if (!entity.level().isClientSide) {
                            if (entity instanceof Xiuzhe0101Entity) {
                                ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.attacked03");
                            }
                        }
                    }
                }
            }
        }
    }

    /** 来源Xiuzhe0101pugongProcedure，保持条件、数值与阶段顺序。 */
    static void basicAttack(LevelAccessor world, Entity entity) {

        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        linshientity = Xiuzhe0101Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            CreatureData.state(entity).setPhase("准备");
            if (!entity.level().isClientSide) {
                if (Math.random() < 0.5) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.attack01");
                    }
                } else {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.attack02");
                    }
                }
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
        } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 2) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
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
        } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 6) {
            Xiuzhe0101Support.finish(entity);
        }
    }

    /** 来源Xiuzhe0101jinengshifangProcedure，保持条件、数值与阶段顺序。 */
    static void castSkill(LevelAccessor world, Entity entity) {

        if ((CreatureData.state(entity).skill()).equals("直拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0101");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0101(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("炮拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0102(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("冲拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0103");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0103(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("连续出拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0104");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0104(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("旋风拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0104");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0105(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("闷拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0105");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0106(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("寸拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0106");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0107(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("碎骨拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0108(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("蓄力重拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0107");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0109(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("上勾拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0108");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0110(world, entity);
            }
        }
        if ((CreatureData.state(entity).skill()).equals("下勾拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0105");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0111(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("碎金拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0112(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("幽海拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0113(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("荆棘拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0114(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("烈焰拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0115(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("碎石拳")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMeleeSkills.Npcskill0116(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("挣脱载具")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0101");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0201(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("蹬墙踢")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0202(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("震山符")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0101");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0203(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("回血丹药")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilleat0101");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0204(world, entity);
            }
        }
        if ((CreatureData.state(entity).skill()).equals("踏地")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0103");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0205(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("撼地")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0104");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0206(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("滑铲")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0105");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0207(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("后空翻")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.backroll01");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0208(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("左翻滚")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.leftroll01");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0209(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("右翻滚")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.rightroll01");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0210(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("前翻滚")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.roll01");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcMovementSkills.Npcskill0211(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("风雷符")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcTalismanSkills.Npcskill0301(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("流火符")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcTalismanSkills.Npcskill0302(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("强压符")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0101");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcTalismanSkills.Npcskill0303(world, entity);
            }
        }
        if ((CreatureData.state(entity).skill()).equals("剑符")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcTalismanSkills.Npcskill0304(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("落雷符")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcTalismanSkills.Npcskill0305(world, entity);
            }
        } else if ((CreatureData.state(entity).skill()).equals("爆燃符")) {
            if ((CreatureData.state(entity).phase()).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                CreatureData.state(entity).setPhase("开始");
            } else {
                NpcTalismanSkills.Npcskill0306(world, entity);
            }
        }
    }
}
