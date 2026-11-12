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
import net.minecraft.server.level.ServerPlayer;
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
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;


/** 037号妖兽的完整来源逐刻技能闭包。 */
final class Yaoshou037Behavior {
    private Yaoshou037Behavior() { }

    /** 来源 Yaoshou037DangShiTiGengXinKeShiProcedure：保留阶段顺序、条件和数值。 */
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
                                    if (jvli2 < 8) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "吼叫");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "飞扑");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "落雷");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("yaoshou_037leibaoxiaoguo")) : false)) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "雷暴");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        }
                                    }
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "抖擞精神");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "光闪");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
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
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "走位");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0) >= 3) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "爆气");
                                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)),
                                                        (10 + 10 * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034"))
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
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 2) {
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
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 8) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("吼叫")) {
                            Yaoshou037houjiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("飞扑")) {
                            Yaoshou037feipu(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("落雷")) {
                            Yaoshou037luolei(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("雷暴")) {
                            Yaoshou037leibao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("抖擞精神")) {
                            Yaoshou037dousoujingshen(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("光闪")) {
                            Yaoshou037guangshan(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向后跳")) {
                            Yaoshou037tiaoyve(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向左跳")) {
                            Yaoshou037tiaoyve(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向右跳")) {
                            Yaoshou037tiaoyve(world, entity);
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

    /** 来源 Yaoshou037tiaoyveProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou037tiaoyve(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.skill02");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            if ((entity.getPersistentData().getString("jineng")).equals("向左跳")) {
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
            } else if ((entity.getPersistentData().getString("jineng")).equals("向右跳")) {
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
            } else {
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
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(entity.getYRot() * (Math.PI / 180)))), 0.5, (1 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 30) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou037guangshanProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou037guangshan(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.skill05");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou039skill01"), SoundSource.HOSTILE, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou039skill01"), SoundSource.HOSTILE, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("stargolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 32, 0, 0, 0, 1);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.FLASH, (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 1, 0, 0, 0, 0);
            {
                Entity _ent = entity;
                _ent.teleportTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()));
                if (_ent instanceof ServerPlayer _serverPlayer)
                    _serverPlayer.connection.teleport((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), _ent.getYRot(), _ent.getXRot());
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou037dousoujingshenProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou037dousoujingshen(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.getPersistentData().putString("jinengjinxing", "抖擞");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.skill03");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("抖擞") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("starlightyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 10, (entity.getBbWidth()), (entity.getBbHeight()),
                        (entity.getBbWidth()), 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("stargolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 10, (entity.getBbWidth()), (entity.getBbHeight()),
                        (entity.getBbWidth()), 0);
            if (entity.isOnFire()) {
                entity.clearFire();
            }
            if (entity instanceof LivingEntity _entity)
                _entity.removeAllEffects();
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou037leibaoProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou037leibao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.skill01");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("yaoshou_037leibaoxiaoguo"), 600, 0));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                        (entity.getBbWidth()), 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                        (entity.getBbWidth()), 0);
            SkillDelays.after(5, () -> {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);

                });
            SkillDelays.after(10, () -> {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);

                });
            SkillDelays.after(15, () -> {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);

                });
            SkillDelays.after(20, () -> {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);

                });
            SkillDelays.after(25, () -> {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 3, (entity.getBbWidth()), (entity.getBbHeight()),
                                (entity.getBbWidth()), 0);

                });
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 50) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou037luoleiProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou037luolei(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 113, 1, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "准备");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.skill04");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.CLOUD, (entity.getX()), (entity.getY()), (entity.getZ()), 10, 0, 0, 0, 1);
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, (float) 1.5, (float) 0.5);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.HOSTILE, (float) 1.5, (float) 0.5, false);
                }
            }
            entity.setDeltaMovement(new Vec3(0, 0.5, 0));
            entity.getPersistentData().putString("jinengjinxing", "落雷");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("落雷") && entity.getPersistentData().getDouble("entitytick") >= 105) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("落雷")) {
            entity.getPersistentData().putString("jinengjinxing", "设置落点");
            entity.getPersistentData().putDouble("luodianX", (linshientity.getX()));
            entity.getPersistentData().putDouble("luodianY", (linshientity.getY()));
            entity.getPersistentData().putDouble("luodianZ", (linshientity.getZ()));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("thundergolden"), (linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 2, 0, 0, 0, 0);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("设置落点")) {
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 10) {
                if (world instanceof ServerLevel _level)
                    _level.getServer().getCommands()
                            .performPrefixedCommand(
                                    new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getPersistentData().getDouble("luodianX")), (entity.getPersistentData().getDouble("luodianY")), (entity.getPersistentData().getDouble("luodianZ"))),
                                            Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                                    ("lightninglanding 3 " + entity.getStringUUID() + " 1 " + Relations.team(entity) + " 2 金色"));
                entity.getPersistentData().putDouble("entitytick2", 0);
                entity.getPersistentData().putString("jinengjinxing", "落雷");
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 44) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou037feipuProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou037feipu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "跳跃");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.skill02");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("跳跃") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 2), 1, (entity.getLookAngle().z * 2)));
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou037skill02"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou037skill02"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, 2, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, 2, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "落地");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("落地") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 29) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou037houjiaoProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou037houjiao(LevelAccessor world, Entity entity) {
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
                if (Yaoshou037054Support.isKind(entity, "yaoshou_037")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou037.skill01");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("yinbo"), shijiaoX, shijiaoY, shijiaoZ, 1, 0, 0, 0, 0);
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, shijiaoY, shijiaoZ), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou037skill01"), SoundSource.NEUTRAL, 2, 1);
                } else {
                    _level.playLocalSound(shijiaoX, shijiaoY, shijiaoZ, Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou037skill01"), SoundSource.NEUTRAL, 2, 1, false);
                }
            }
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(shijiaoX, shijiaoY, shijiaoZ), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound(shijiaoX, shijiaoY, shijiaoZ, Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(6 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.5, (entity.getLookAngle().z)));
                        }
                        if (entity instanceof Player) {
                            if (entity.getPersistentData().getDouble("LEVELnumber") > Cultivation.get((Player) entityiterator, CultivationValue.LEVEL_NUMBER)) {
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, (int) ((entity.getPersistentData().getDouble("LEVELnumber")
                                            - Cultivation.get((Player) entityiterator, CultivationValue.LEVEL_NUMBER)) - 1)));
                            }
                        } else {
                            if (entity.getPersistentData().getDouble("LEVELnumber") > entityiterator.getPersistentData().getDouble("LEVELnumber")) {
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, (int) ((entity.getPersistentData().getDouble("LEVELnumber") - entityiterator.getPersistentData().getDouble("LEVELnumber")) - 1)));
                            }
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 48) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
