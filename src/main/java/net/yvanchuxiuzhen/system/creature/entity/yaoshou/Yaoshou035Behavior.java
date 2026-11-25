package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 035号妖兽的候选调度、技能阶段与命中行为。 */
final class Yaoshou035Behavior {
    private Yaoshou035Behavior() { }

    static void tick(YaoshouEntity entity) { tick(entity.level(), entity); }

    private static void summon(LevelAccessor world, Entity entity) {
        String phase = CreatureData.state(entity).phase();
        if (phase.isEmpty()) {
            Entity target = Yaoshou037054Support.target(world, entity);
            entity.lookAt(EntityAnchorArgument.Anchor.EYES,
                    new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
            CreatureData.state(entity).setPhase("蓄力");
            if (!entity.level().isClientSide()) ((YaoshouEntity) entity).setAnimation("animation.yaoshou035.skill01");
        } else if (phase.equals("蓄力") && CreatureData.state(entity).tick() >= 2) {
            double y = entity.getY() + entity.getBbHeight() / 2;
            if (world instanceof ServerLevel level) {
                level.playSound(null, BlockPos.containing(entity.getX(), y, entity.getZ()),
                        Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou035skill01"), SoundSource.HOSTILE, 1, 1);
                for (int index = 0; index < 2; index++) {
                    double x = entity.getX() + Yaoshou037054Support.randomInt(-2, 2);
                    double z = entity.getZ() + Yaoshou037054Support.randomInt(-2, 2);
                    level.sendParticles(Particles.type("smokeblack"), x, entity.getY(), z, 24, 0.7, 1, 0.7, 0);
                    Mob monkey = (Mob) CreatureSetup.entityType("yaoshou_013").get().create(level);
                    monkey.moveTo(x, entity.getY(), z, world.getRandom().nextFloat() * 360F, 0);
                    Relations.shareEntityAllegiance(entity, monkey);
                    EntityAttributes.setLevel(monkey, Math.ceil(EntityAttributes.level(entity) / 2));
                    CreatureData.state(monkey).setCountdown(600);
                    Scoreboard scoreboard = level.getScoreboard();
                    Objective objective = scoreboard.getObjective("zhaohuanwu");
                    if (objective == null) objective = scoreboard.addObjective("zhaohuanwu", ObjectiveCriteria.DUMMY,
                            Component.literal("zhaohuanwu"), ObjectiveCriteria.RenderType.INTEGER, false, null);
                    scoreboard.getOrCreatePlayerScore(monkey, objective).set(1);
                    CreatureData.state(monkey).setAttackerUuid(CreatureData.state(entity).attackerUuid());
                    monkey.finalizeSpawn(level, level.getCurrentDifficultyAt(monkey.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                    level.addFreshEntity(monkey);
                }
            } else if (world instanceof Level level && level.isClientSide()) {
                level.playLocalSound(entity.getX(), y, entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou035skill01"),
                        SoundSource.HOSTILE, 1, 1, false);
            }
            CreatureData.state(entity).setPhase("结束");
        } else if (phase.equals("结束") && CreatureData.state(entity).tick() >= 13) {
            Yaoshou037054Support.finish(entity);
        }
    }

    static void tick(LevelAccessor world, Entity entity) {
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
            if (CreatureData.state(entity).summonCooldown() >= 1) {
                CreatureData.state(entity).setSummonCooldown((CreatureData.state(entity).summonCooldown() - 1));
            }
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
                                    if (jvli2 < 8) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "恫吓");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    }
                                    if (jvli2 > 3) {
                                        if (CreatureData.state(entity).summonCooldown() < 1) {
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "召唤小猴");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "飞扑");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
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
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0) >= 3) {
                                                shunxvjishu = shunxvjishu + 1;
                                                CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "爆气");
                                                CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                                CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), (10 + 10 * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034"))
                                                                ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier()
                                                                : 0)));
                                                if (entity instanceof LivingEntity _entity)
                                                    _entity.removeEffect(StatusEffects.holder("effect_034"));
                                            }
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
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_035")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou035.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 1) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getZ();
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
                                        }
                                    }
                                }
                                CreatureData.state(entity).setPhase("攻击");
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 5) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("恫吓")) {
                            Yaoshou035donghe(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("飞扑") || (CreatureData.state(entity).skill()).equals("向后跳")
                                || (CreatureData.state(entity).skill()).equals("向左跳") || (CreatureData.state(entity).skill()).equals("向右跳")) {
                            Yaoshou035feipu(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("召唤小猴")) {
                            summon(world, entity);
                            CreatureData.state(entity).setSummonCooldown(600);
                        } else if ((CreatureData.state(entity).skill()).equals("爆气")) {
                            Yaoshou011Behavior.Yaoshou011baoqi(world, entity);
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

    static void Yaoshou035donghe(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double buchong = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_035")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou035.skill01");
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 4) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("yinbo"), shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ, 1, 0, 0, 0, 0);
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + entity.getBbHeight() / 2, shijiaoZ), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou035skill01"), SoundSource.NEUTRAL, 2, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ, Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou035skill01"), SoundSource.NEUTRAL, 2, 1, false);
                }
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, entity.getY() + entity.getBbHeight() / 2, shijiaoZ), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ, Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(9 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiondarkgrey"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0,
                                    0, 0, 0);
                        if (world instanceof ServerLevel level && entityiterator instanceof LivingEntity living) {
                            SpellDamage.command(level, living, "shanghai 音波 " + entity.getStringUUID() + " false 5 1");
                        }
                    }
                }
            }
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 12) {
            Yaoshou037054Support.finish(entity);
        }
    }

    static void Yaoshou035feipu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_035")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou035.sprint");
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 1) {
            if ((CreatureData.state(entity).skill()).equals("向左跳")) {
                {
                    Entity _ent = entity;
                    _ent.setYRot((float) (entity.getYRot() - 70));
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
            } else if ((CreatureData.state(entity).skill()).equals("向右跳")) {
                {
                    Entity _ent = entity;
                    _ent.setYRot((float) (entity.getYRot() + 70));
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
            } else if ((CreatureData.state(entity).skill()).equals("向后跳")) {
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
            }
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(entity.getYRot() * (Math.PI / 180)))), 0.6, (1 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 10) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
