package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 024号妖兽的来源逐刻技能与阶段流程。 */
final class Yaoshou024Behavior {
    private Yaoshou024Behavior() { }

    /** 来源 Yaoshou024DangShiTiGengXinKeShiProcedure：保留阶段条件、候选顺序及随机调用。 */
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
                                    if (jvli2 > 5) {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "飞扑");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "靠近");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "点灵");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 50);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "普攻");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "向左跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "向右跳");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 10);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "弹射螳刀");
                                        CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                        CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 60);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            shunxvjishu = shunxvjishu + 1;
                                            CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "爆气");
                                            CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 5);
                                            CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), (10 + (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)
                                                            * 10));
                                        }
                                    }
                                    shunxvjishu = shunxvjishu + 1;
                                    CreatureData.state(entity).setCandidateSkill((int) (shunxvjishu), "连续耕爪");
                                    CreatureData.state(entity).setCandidateTimer((int) (shunxvjishu), 20);
                                    CreatureData.state(entity).setCandidateWeight((int) (shunxvjishu), 50);
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
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_015")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou015.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((CreatureData.state(entity).phase()).equals("准备") && CreatureData.state(entity).tick() >= 1) {
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
                            } else if ((CreatureData.state(entity).phase()).equals("攻击") && CreatureData.state(entity).tick() >= 6) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((CreatureData.state(entity).skill()).equals("飞扑")) {
                            Yaoshou024feipu(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("向左跳")) {
                            Yaoshou015xiangzuotiao(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("向右跳")) {
                            Yaoshou015xiangyoutiao(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("弹射螳刀")) {
                            Yaoshou024tanshetangdao(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("连续耕爪")) {
                            Yaoshou024lianxvgengzhua(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("爆气")) {
                            Yaoshou011baoqi(world, entity);
                        } else if ((CreatureData.state(entity).skill()).equals("点灵")) {
                            Yaoshou024dianling(world, entity);
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

    /** 来源 Yaoshou024dianlingProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou024dianling(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("蓄力");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 20) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("brokenskyblue"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 36, 0, 0, 0, 1);
            for (int index0 = 0; index0 < (int) (10); index0++) {
                shijiaoX = linshientity.getX() + Yaoshou037054Support.randomInt(-5, 5);
                shijiaoY = linshientity.getY() + Yaoshou037054Support.randomInt(0, 3);
                shijiaoZ = linshientity.getZ() + Yaoshou037054Support.randomInt(-5, 5);
                if (world.isEmptyBlock(BlockPos.containing(shijiaoX, shijiaoY, shijiaoZ))) {
                    break;
                }
            }
            {
                Entity _ent = entity;
                _ent.teleportTo(shijiaoX, shijiaoY, shijiaoZ);
                if (_ent instanceof ServerPlayer _serverPlayer)
                    _serverPlayer.connection.teleport(shijiaoX, shijiaoY, shijiaoZ, _ent.getYRot(), _ent.getXRot());
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("starskyblue"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 24, 0, 0, 0, 0.5);
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("蓄力")) {
            CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("brokenskyblue"), (entity.getX()), (entity.getY()), (entity.getZ()), (int) Math.ceil(CreatureData.state(entity).tick2() / 2),
                        (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 42) {
            if (entity instanceof LivingEntity _entity)
                _entity.removeEffect(MobEffects.INVISIBILITY);
            Yaoshou037054Support.finish(entity);
            CreatureData.state(entity).setSkill("弹射螳刀");
        }
    }

    /** 来源 Yaoshou011baoqiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou011baoqi(LevelAccessor world, Entity entity) {
        String team = "";
        String zhilingjihe = "";
        String particleID = "";
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
        team = Relations.team(entity);
        if (world instanceof ServerLevel) Skills.showWindBurst(entity);
        if (world instanceof ServerLevel _level)
            _level.sendParticles(ParticleTypes.CLOUD, (entity.getX()), (entity.getY()), (entity.getZ()), 40, 0, 0, 0, 1);
        if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
                _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:tongyong_baoqi"), SoundSource.NEUTRAL, 1, 1);
            } else {
                _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:tongyong_baoqi"), SoundSource.NEUTRAL, 1, 1, false);
            }
        }
        {
            final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
            List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(9 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
            for (Entity entityiterator : _entfound) {
                if (!Relations.isFriendly(entity, entityiterator)) {
                    if (entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("yvanchuxiuzhen:noentity")))) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("brokenlightgray"), (entityiterator.getX()), (entityiterator.getY()), (entityiterator.getZ()), 16, 0, 0, 0, 1);
                        if (!entityiterator.level().isClientSide())
                            entityiterator.discard();
                    } else if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                        entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 1), 0.5, ((entityiterator.getZ() - entity.getZ()) * 1)));
                    }
                }
            }
        }
        R = entity.getBbWidth();
        speed = 0.5;
        particleID = "minecraft:cloud";
        particlenumber = 20;
        xvnhuan = 0;
        shengchengY = entity.getY() + entity.getBbHeight() * 0.5;
        xiangliangY = 0;
        for (int index0 = 0; index0 < (int) (particlenumber); index0++) {
            shengchengX = entity.getX() + R * Math.cos(xvnhuan);
            shengchengZ = entity.getZ() + R * Math.sin(xvnhuan);
            xiangliangX = speed * Math.cos(xvnhuan);
            xiangliangZ = speed * Math.sin(xvnhuan);
            zhilingjihe = "particle " + particleID + " " + shengchengX + " " + shengchengY + " " + shengchengZ + " " + xiangliangX + " " + xiangliangY + " " + xiangliangZ + " " + speed + " 0";
            if (world instanceof ServerLevel _level)
                _level.getServer().getCommands().performPrefixedCommand(
                        new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), zhilingjihe);
            xvnhuan = xvnhuan + 360 / particlenumber;
        }
        Yaoshou037054Support.finish(entity);
    }

    /** 来源 Yaoshou024lianxvgengzhuaProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou024lianxvgengzhua(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            CreatureData.state(entity).setPhase("开始");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_024")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou024.skill02");
                }
            }
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.5), 0, (entity.getLookAngle().z * 0.5)));
        } else if ((CreatureData.state(entity).phase()).equals("开始") && CreatureData.state(entity).tick() >= 48) {
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("开始")) {
            CreatureData.state(entity).setTick2((CreatureData.state(entity).tick2() + 1));
            if (CreatureData.state(entity).tick2() >= 4) {
                if (EntityAttributes.level(entity) >= 21) {
                    entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                }
                entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.5), 0, (entity.getLookAngle().z * 0.5)));
                buchong = entity.getBbWidth() / 2;
                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(null, BlockPos.containing(shijiaoX, shijiaoY, shijiaoZ), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou015skill02"), SoundSource.HOSTILE,
                                (float) Yaoshou037054Support.randomDouble(0.8, 1.5), (float) Yaoshou037054Support.randomDouble(0.5, 1.5));
                    } else {
                        _level.playLocalSound(shijiaoX, shijiaoY, shijiaoZ, Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou015skill02"), SoundSource.HOSTILE, (float) Yaoshou037054Support.randomDouble(0.8, 1.5),
                                (float) Yaoshou037054Support.randomDouble(0.5, 1.5), false);
                    }
                }
                {
                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionlightyellow"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()),
                                        1, 0, 0, 0, 0);
                            SpellDamage.deal(entityiterator, DamageCategory.LIGHT, entity, false, 5, 1);
                        }
                    }
                }
                CreatureData.state(entity).setTick2(0);
            }
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 50) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou024tanshetangdaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou024tanshetangdao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou015skill01"), SoundSource.HOSTILE, 1,
                            1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou015skill01"), SoundSource.HOSTILE, 1, 1,
                            false);
                }
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 40, 1, (false), (false)));
            CreatureData.state(entity).setPhase("蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_024")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou024.skill01");
                }
            }
        } else if ((CreatureData.state(entity).phase()).equals("蓄力") && CreatureData.state(entity).tick() >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ, 10, 0, 0, 0, 1);
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("xvedi"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 10, 0.5, 0.5, 0.5, 0.4);
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
                        entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage * 1.5));
                    }
                }
            }
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 1.3), 0, (entity.getLookAngle().y * 1.3)));
            SkillDelays.after(10, () -> {
                    {
                        final Vec3 _center = new Vec3(
                                (entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + entity.getBbWidth() / 2))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX()),
                                (entity.getY() + entity.getBbHeight() / 2),
                                (entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + entity.getBbWidth() / 2))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getZ()));
                        List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                .toList();
                        for (Entity entityiterator : _entfound) {
                            if (!Relations.isFriendly(entity, entityiterator)) {
                                if (world instanceof ServerLevel _level)
                                    _level.sendParticles(Particles.type("xvedi"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 10, 0.5, 0.5,
                                            0.5, 0.4);
                                if (world instanceof ServerLevel _level)
                                    _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 20, 0, 0, 0, 1);
                                entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (EntityAttributes.stat(entity, Stat.ATTACK) * 1.5));
                            }
                        }
                    }

                });
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 41) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou015xiangyoutiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou015xiangyoutiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            jiaodu = entity.getYRot() + 60;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.4, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 10) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou015xiangzuotiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou015xiangzuotiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            jiaodu = entity.getYRot() - 60;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.4, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 10) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou024feipuProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou024feipu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((CreatureData.state(entity).phase()).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_024")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou024.skill03");
                }
            }
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 1.5), 0.6, (entity.getLookAngle().z * 1.5)));
            CreatureData.state(entity).setPhase("结束");
        } else if ((CreatureData.state(entity).phase()).equals("结束") && CreatureData.state(entity).tick() >= 21) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
