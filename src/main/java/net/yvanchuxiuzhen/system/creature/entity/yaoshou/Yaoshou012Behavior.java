package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 012号妖兽来源的逐刻选择、近战与完整技能阶段。 */
final class Yaoshou012Behavior {
    private Yaoshou012Behavior() { }

    /** 来源 Yaoshou012DangShiTiGengXinKeShiProcedure：保留阶段、条件、数值和调用顺序。 */
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
            entity.getPersistentData().putDouble("entitytick", (entity.getPersistentData().getDouble("entitytick") + 1));
            if ((entity.getPersistentData().getString("jineng")).equals("")) {
                if (!(entity.getPersistentData().getString("gongjizhe")).equals(entity.getPersistentData().getString("linshigongjizhe"))) {
                    entity.getPersistentData().putString("gongjizhe", (entity.getPersistentData().getString("linshigongjizhe")));
                }
            }
            if (!(entity.getPersistentData().getString("gongjizhe")).equals("")) {
                linshientity = Yaoshou037054Support.target(world, entity);
            }
            if (!(linshientity == null)) {
                if (linshientity.isAlive()) {
                    if ((entity.getPersistentData().getString("jineng")).equals("")) {
                        if ((entity.getPersistentData().getString("yvdingjineng")).equals("")) {
                            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                            jvli2 = jvli1 - (entity.getBbWidth() / 2 + linshientity.getBbWidth() / 2);
                            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            shunxvjishu = 0;
                            if (entity.isPassenger()) {
                                shunxvjishu = shunxvjishu + 1;
                                entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "挣脱");
                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 100);
                            }
                            if (!entity.isPassenger()) {
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_002")) : false)
                                        && !(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false)) {
                                    if (jvli2 > 4) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "飞扑");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "扶摇直上");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "天虎扑咬");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 40);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "向后跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "向左跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "向右跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "扫尾");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "虎啸");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "天虎扑咬");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0) >= 3) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "爆气");
                                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)),
                                                        (10 + 10 * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034"))
                                                                ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier()
                                                                : 0)));
                                            }
                                        }
                                    }
                                }
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false) && jvli2 < 3) {
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                }
                            }
                            xvnhuan = 1;
                            for (int index0 = 0; index0 < (int) (shunxvjishu); index0++) {
                                qvanzhongmax = qvanzhongmax + entity.getPersistentData().getDouble(("jinengqvanzhong" + Integer.toString((int) xvnhuan)));
                                xvnhuan = xvnhuan + 1;
                            }
                            suijishu = Yaoshou037054Support.randomInt(1, (int) qvanzhongmax);
                            xvnhuan2 = 1;
                            for (int index1 = 0; index1 < (int) (shunxvjishu); index1++) {
                                qvanzhongdiejia = qvanzhongdiejia + entity.getPersistentData().getDouble(("jinengqvanzhong" + Integer.toString((int) xvnhuan2)));
                                if (qvanzhongdiejia >= suijishu) {
                                    entity.getPersistentData().putString("yvdingjineng", (entity.getPersistentData().getString(("linshijineng" + Integer.toString((int) xvnhuan2)))));
                                    entity.getPersistentData().putDouble("yvdingjinengjishi", (entity.getPersistentData().getDouble(("jinengjishi" + Integer.toString((int) xvnhuan2)))));
                                    break;
                                }
                                xvnhuan2 = xvnhuan2 + 1;
                            }
                            entity.getPersistentData().putDouble("jinengshuliang", shunxvjishu);
                        } else if (entity.getPersistentData().getDouble("entitytick") >= entity.getPersistentData().getDouble("yvdingjinengjishi")) {
                            entity.getPersistentData().putString("jineng", (entity.getPersistentData().getString("yvdingjineng")));
                            entity.getPersistentData().putString("yvdingjineng", "");
                            entity.getPersistentData().putDouble("yvdingjinengjishi", 0);
                            entity.getPersistentData().putDouble("entitytick", 0);
                            xvnhuan = 1;
                            for (int index2 = 0; index2 < (int) (entity.getPersistentData().getDouble("jinengshuliang")); index2++) {
                                entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) xvnhuan)), "");
                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) xvnhuan)), 0);
                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) xvnhuan)), 0);
                                xvnhuan = xvnhuan + 1;
                            }
                            entity.getPersistentData().putDouble("jinengshuliang", 0);
                        }
                    } else {
                        if ((entity.getPersistentData().getString("jineng")).equals("靠近")) {
                            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                                if (entity instanceof Mob _entity)
                                    _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 1.5);
                                entity.getPersistentData().putString("jinengjinxing", "移动中");
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("移动中") && entity.getPersistentData().getDouble("entitytick") >= 40) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("挣脱")) {
                            Yaoshou037054Support.escape(entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("普攻")) {
                            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                                entity.getPersistentData().putString("jinengjinxing", "准备");
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 3) {
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
                                            damage = entity.getPersistentData().getDouble("attack_Num");
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
                                entity.getPersistentData().putString("jinengjinxing", "攻击");
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 12) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("飞扑")) {
                            Yaoshou012feipu(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("扶摇直上")) {
                            Yaoshou012fuyaozhishang(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("虎啸")) {
                            Yaoshou012huxiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("扫尾")) {
                            Yaoshou012saowei(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("天虎扑咬")) {
                            Yaoshou012tianhupuyao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向后跳")) {
                            Yaoshou012houtiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向左跳")) {
                            Yaoshou012zuotiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向右跳")) {
                            Yaoshou012youtiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("爆气")) {
                            Yaoshou011Behavior.Yaoshou011baoqi(world, entity);
                        }
                    }
                } else {
                    entity.getPersistentData().putString("gongjizhe", "");
                    entity.getPersistentData().putString("linshigongjizhe", "");
                    Yaoshou037054Support.finish(entity);
                }
            } else {
                if (entity.getPersistentData().getDouble("entitytick") >= 40) {
                    if (entity instanceof Mob _entity)
                        _entity.getNavigation().moveTo((entity.getX() + Yaoshou037054Support.randomInt(-3, 3)), (entity.getY()), (entity.getZ() + Yaoshou037054Support.randomInt(-3, 3)), 1);
                    entity.getPersistentData().putDouble("entitytick", 0);
                }
            }
        }
    }

    /** 来源 Yaoshou012youtiaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012youtiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill05");
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_026")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_026")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou026.skill06");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 9) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "振翅");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("振翅") && entity.getPersistentData().getDouble("entitytick") >= 13) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CLOUD, (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.5);
            jiaodu = entity.getYRot() - 70;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.6, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 31) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou012zuotiaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012zuotiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill05");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 9) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "振翅");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("振翅") && entity.getPersistentData().getDouble("entitytick") >= 13) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CLOUD, (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.5);
            jiaodu = entity.getYRot() + 70;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.6, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 31) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou012houtiaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012houtiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill05");
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_026")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_026")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou026.skill06");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 9) {
            entity.getPersistentData().putString("jinengjinxing", "振翅");
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2, false);
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("振翅") && entity.getPersistentData().getDouble("entitytick") >= 13) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CLOUD, (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.5);
            jiaodu = entity.getYRot() + 180;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.6, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 31) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou012tianhupuyaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012tianhupuyao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        Yaoshoujinkong(world, entity);
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill03");
                }
            }
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 1.3), 0.6, (entity.getLookAngle().z * 1.3)));
            entity.getPersistentData().putString("jinengjinxing", "起飞");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("起飞") && entity.getPersistentData().getDouble("entitytick") >= 17) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, 2, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, 2, 1, false);
                }
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CRIT, shijiaoX, shijiaoY, shijiaoZ, 20, 1, 1, 1, 1);
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiongolden"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0,
                                    0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "金" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "1"));
                        }
                        if (entityiterator instanceof LivingEntity _entity)
                            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20));
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 25) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 YaoshoujinkongProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshoujinkong(LevelAccessor world, Entity entity) {
        boolean qingchu = false;
        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_035")) : false) {
            if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.LEVITATION) : false) {
                    if (entity instanceof LivingEntity _entity)
                        _entity.removeEffect(MobEffects.LEVITATION);
                    qingchu = true;
                }
                if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.SLOW_FALLING) : false) {
                    if (entity instanceof LivingEntity _entity)
                        _entity.removeEffect(MobEffects.SLOW_FALLING);
                    qingchu = true;
                }
                if (entity.isNoGravity()) {
                    entity.setNoGravity((false));
                    qingchu = true;
                }
                if (qingchu) {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("brokenwhite"), (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.2);
                    Yaoshou037054Support.finish(entity);
                }
            }
        }
    }

    /** 来源 Yaoshou012saoweiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012saowei(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill02");
                }
            }
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.45, (entity.getLookAngle().z)));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 7) {
            entity.getPersistentData().putString("jinengjinxing", "扫");
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("扫") && entity.getPersistentData().getDouble("entitytick") >= 13) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012skill02"), SoundSource.NEUTRAL, (float) 1.5, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012skill02"), SoundSource.NEUTRAL, (float) 1.5, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SWEEP_ATTACK, shijiaoX, shijiaoY, shijiaoZ, 16, 5, 2, 5, 0);
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(9, 3, 9), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiongolden"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0,
                                    0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "金" + " " + entity.getStringUUID() + " " + "false" + " " + "3" + " " + "1.5"));
                        }
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x * 1), 0.4, (entity.getLookAngle().z * 1)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou012huxiaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012huxiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double jvli = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("whirlwindlightgray"), shijiaoX, shijiaoY, shijiaoZ, 1, 0, 0, 0, 0);
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill01");
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_026")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_026")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou026.skill01");
                    }
                }
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012skill01"), SoundSource.HOSTILE, 2, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012skill01"), SoundSource.HOSTILE, 2, 1, false);
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            buchong = entity.getBbWidth() / 2;
            jvli = 1;
            for (int index0 = 0; index0 < (int) (3); index0++) {
                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((jvli + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((jvli + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((jvli + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
                {
                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((jvli + 2) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                                if (world instanceof ServerLevel _level)
                                    _level.sendParticles(Particles.type("smallexplosionwhite"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1,
                                            0, 0, 0, 0);
                                {
                                    Entity _ent = entityiterator;
                                    if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                        _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                                ("shanghai" + " " + "风" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "0.7"));
                                }
                                if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                    entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.2, (entity.getLookAngle().z)));
                                }
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                            } else {
                                if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                    entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.5, (entity.getLookAngle().z)));
                                }
                                if (entity instanceof Player) {
                                    if (entity.getPersistentData()
                                            .getDouble("LEVELnumber") > (entityiterator instanceof Player player ? Cultivation.get(player, CultivationValue.LEVEL_NUMBER) : 0)) {
                                        if (entityiterator instanceof LivingEntity _entity)
                                            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 20));
                                        if (entityiterator instanceof LivingEntity _entity)
                                            _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, (int) ((entity.getPersistentData().getDouble("LEVELnumber")
                                                    - (entityiterator instanceof Player player ? Cultivation.get(player, CultivationValue.LEVEL_NUMBER) : 0)) - 1)));
                                    } else {
                                        if (entityiterator instanceof LivingEntity _entity)
                                            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
                                    }
                                } else {
                                    if (entity.getPersistentData().getDouble("LEVELnumber") > entityiterator.getPersistentData().getDouble("LEVELnumber")) {
                                        if (entityiterator instanceof LivingEntity _entity)
                                            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 20));
                                        if (entityiterator instanceof LivingEntity _entity)
                                            _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, (int) ((entity.getPersistentData().getDouble("LEVELnumber") - entityiterator.getPersistentData().getDouble("LEVELnumber")) - 1)));
                                    } else {
                                        if (entityiterator instanceof LivingEntity _entity)
                                            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
                                    }
                                }
                            }
                        }
                    }
                }
                jvli = jvli + 2;
            }
            for (int index1 = 0; index1 < (int) (32); index1++) {
                if (world instanceof ServerLevel _level)
                    _level.getServer().getCommands().performPrefixedCommand(
                            new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null)
                                    .withSuppressedOutput(),
                            ("particle minecraft:cloud ~ ~ ~ " + entity.getLookAngle().x * Yaoshou037054Support.randomDouble(0.5, 1.5) + " " + entity.getLookAngle().y * Yaoshou037054Support.randomDouble(0.5, 1.5) + " "
                                    + entity.getLookAngle().z * Yaoshou037054Support.randomDouble(0.5, 1.5) + " 1 0 force"));
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 49) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou012fuyaozhishangProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012fuyaozhishang(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        Yaoshoujinkong(world, entity);
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill04");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 12) {
            entity.setDeltaMovement(new Vec3(0, 0.5, 0));
            entity.getPersistentData().putString("jinengjinxing", "振翅二");
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CLOUD, (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.2);
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, (float) 1.5, (float) 0.5);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, (float) 1.5, (float) 0.5, false);
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("振翅二") && entity.getPersistentData().getDouble("entitytick") >= 26) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.2), 0.8, (entity.getLookAngle().z * 0.2)));
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, 2, (float) 0.8);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, 2, (float) 0.8, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "振翅三");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("振翅三") && entity.getPersistentData().getDouble("entitytick") >= 40) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.2), 1, (entity.getLookAngle().z * 0.2)));
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, (float) 2.5, (float) 1.2);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, (float) 2.5, (float) 1.2, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "横移");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("横移") && entity.getPersistentData().getDouble("entitytick") >= 54) {
            entity.getPersistentData().putString("jinengjinxing", "飘");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill06");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("飘") && entity.getPersistentData().getDouble("entitytick") >= 66) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("飘")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 1.8), (entity.getLookAngle().y * 1.8), (entity.getLookAngle().z * 1.8)));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 68) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou012feipuProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou012feipu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        Yaoshoujinkong(world, entity);
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_012")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou012.skill05");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 9) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, (float) 2.5, (float) 1.2, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "振翅");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("振翅") && entity.getPersistentData().getDouble("entitytick") >= 13) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 2), 0.7, (entity.getLookAngle().z * 2)));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CLOUD, (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.5);
            entity.getPersistentData().putString("jinengjinxing", "振翅2");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("振翅2") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 30) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
