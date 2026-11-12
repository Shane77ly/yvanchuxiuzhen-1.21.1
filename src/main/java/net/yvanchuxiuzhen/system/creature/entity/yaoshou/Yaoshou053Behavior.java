package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 053号妖兽来源的完整逐刻技能闭包。 */
final class Yaoshou053Behavior {
    private Yaoshou053Behavior() { }

    /** 来源 Yaoshou053DangShiTiGengXinKeShiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void tick(LevelAccessor world, Entity entity) {
        ItemStack friendlistitem = ItemStack.EMPTY;
        String team = "";
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
                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 100);
                            }
                            if (!entity.isPassenger()) {
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_002")) : false)
                                        && !(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false)) {
                                    if (jvli2 < 8) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冰脊");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "吹白");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("yaoshou_053baofengxve")) : false)) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冰天雪地");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冰雨");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冰爪");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        if (entity instanceof LivingEntity _entity)
                                            _entity.removeEffect(StatusEffects.holder("effect_034"));
                                    }
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冰瞬身");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冰闪身");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "向后跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "向左跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "向右跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), (10 + 10
                                                    * (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0)));
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "走位");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 3);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_034")) : false) {
                                            if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0) >= 3) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "爆气");
                                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 1);
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
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冰爪")) {
                            Yaoshou053bingzhua(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冰脊")) {
                            Yaoshou053bingji(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("吹白")) {
                            Yaoshou053chuibai(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冰天雪地")) {
                            Yaoshou053bingtianxvedi(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冰雨")) {
                            Yaoshou053bingyv(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冰瞬身")) {
                            Yaoshou053bingshunshen(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冰闪身")) {
                            Yaoshou053bingshanshen(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向后跳")) {
                            Yaoshou053xianghoutiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向左跳")) {
                            Yaoshou053xiangzuotiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("向右跳")) {
                            Yaoshou053xiangyoutiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("爆气")) {
                            Yaoshou011Behavior.Yaoshou011baoqi(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("走位")) {
                            Yaoshou017Behavior.Yaoshou017zouwei(world, entity);
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

    /** 来源 Yaoshou053xiangyoutiaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053xiangyoutiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 7) {
            jiaodu = entity.getYRot() + 90;
            entity.setDeltaMovement(new Vec3((-(1.2 * Math.sin(jiaodu * (Math.PI / 180)))), 0.6, (1.2 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou053xiangzuotiaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053xiangzuotiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 7) {
            jiaodu = entity.getYRot() - 90;
            entity.setDeltaMovement(new Vec3((-(1.2 * Math.sin(jiaodu * (Math.PI / 180)))), 0.6, (1.2 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou053xianghoutiaoProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053xianghoutiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill02");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 7) {
            jiaodu = entity.getYRot() + 180;
            entity.setDeltaMovement(new Vec3((-(1.2 * Math.sin(jiaodu * (Math.PI / 180)))), 0.6, (1.2 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou053bingshanshenProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053bingshanshen(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "闪身");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill05");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("闪身") && entity.getPersistentData().getDouble("entitytick") >= 3) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou053skill05"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou053skill05"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SNOWFLAKE, (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 20, (entity.getBbWidth()), (entity.getBbHeight()), (entity.getBbWidth()), 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("flash_ice"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 20, (entity.getBbWidth()), (entity.getBbHeight()),
                        (entity.getBbWidth()), 1);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("disappearlightblue"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 10, 0, 0, 0, 0.5);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosionlightblue"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 20, 0,
                                    0, 0, 1);
                        entityiterator.setTicksFrozen((int) (entityiterator.getTicksFrozen() + 10));
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "冰" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "0.5"));
                        }
                    }
                }
            }
            jiaodu = entity.getYRot() + 180;
            entity.setDeltaMovement(new Vec3((-(2 * Math.sin(jiaodu * (Math.PI / 180)))), 0.4, (2 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 44) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou053bingshunshenProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053bingshunshen(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou053skill01"), SoundSource.HOSTILE, 1,
                            1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou053skill01"), SoundSource.HOSTILE, 1, 1,
                            false);
                }
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "冲锋");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill01");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("冲锋") && entity.getPersistentData().getDouble("entitytick") >= 14) {
            Yaoshou037054Support.finish(entity);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("冲锋")) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), (entity.getLookAngle().y), (entity.getLookAngle().z)));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("flash_ice"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 10, (entity.getBbWidth()), (entity.getBbHeight()),
                        (entity.getBbWidth()), 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SNOWFLAKE, (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 5, (entity.getBbWidth()), (entity.getBbHeight()), (entity.getBbWidth()), 0);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        entityiterator.setTicksFrozen((int) (entityiterator.getTicksFrozen() + 20));
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou053bingyvProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053bingyv(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double xvnhuan = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill06");
                }
            }
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0, (false), (false)));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            xvnhuan = 1;
            for (int index0 = 0; index0 < (int) (10); index0++) {
                SkillDelays.after((int) xvnhuan, () -> {
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 10, 4, 3, 4, 0);
                    for (int index1 = 0; index1 < 20; index1++) {
                        if (world instanceof ServerLevel)
                            SkillSummons.shootIce01(entity,
                                    new Vec3(entity.getX() + Yaoshou037054Support.randomInt(-8, 8), entity.getY() + 12,
                                            entity.getZ() + Yaoshou037054Support.randomInt(-8, 8)),
                                    new Vec3(0, -1, 0));
                    }
                });
                xvnhuan = xvnhuan + 5;
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 42) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou053bingtianxvediProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053bingtianxvedi(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill06");
                }
            }
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("yaoshou_053baofengxve"), 1200, (int) Math.floor(entity.getPersistentData().getDouble("LEVELnumber") / 5), (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 60) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou053chuibaiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053chuibai(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double chongfu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "吹风");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill04");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("吹风") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou033skill01"), SoundSource.HOSTILE, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou033skill01"), SoundSource.HOSTILE, 1, 1, false);
                }
            }
            for (int index0 = 0; index0 < (int) (30); index0++) {
                if (world instanceof ServerLevel _level)
                    _level.getServer().getCommands().performPrefixedCommand(
                            new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null)
                                    .withSuppressedOutput(),
                            ("particle cloud ~ ~ ~ " + entity.getLookAngle().x * Yaoshou037054Support.randomDouble(0.5, 1.5) + " " + entity.getLookAngle().y * Yaoshou037054Support.randomDouble(0.5, 1.5) + " "
                                    + entity.getLookAngle().z * Yaoshou037054Support.randomDouble(0.5, 1.5) + " 1 0 force"));
            }
            chongfu = 2;
            for (int index1 = 0; index1 < (int) (3); index1++) {
                buchong = entity.getBbWidth() / 2;
                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((chongfu + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                        .getX();
                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((chongfu + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                        .getY();
                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((chongfu + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                        .getZ();
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((chongfu + 1) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionlightblue"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1,
                                        0, 0, 0, 0);
                            entityiterator.setTicksFrozen((int) (entityiterator.getTicksFrozen() + 50));
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "冰" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "0.5"));
                            }
                        }
                    }
                }
                chongfu = chongfu + 2;
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 32) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou053bingjiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053bingji(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        String team = "";
        String AttackUUID = "";
        double AttackDamage = 0;
        double Damage_multiplier = 0;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou053skill03"), SoundSource.NEUTRAL, (float) 1.5, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou053skill03"), SoundSource.NEUTRAL, (float) 1.5, 1, false);
                }
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (entity instanceof LivingEntity _entity)
                _entity.setHealth((float) ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + Math.round(entity.getPersistentData().getDouble("LEVELnumber") * 0.3)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 102, 1, (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "冰封");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill03");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("冰封") && entity.getPersistentData().getDouble("entitytick") >= 102) {
            Yaoshou037054Support.finish(entity);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("冰封")) {
            entity.getPersistentData().putDouble("jiluzuobiaoX", (linshientity.getX()));
            entity.getPersistentData().putDouble("jiluzuobiaoY", (linshientity.getY()));
            entity.getPersistentData().putDouble("jiluzuobiaoZ", (linshientity.getZ()));
            entity.getPersistentData().putString("jinengjinxing", "蓄能");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄能")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("brokenlightblue"), (entity.getPersistentData().getDouble("jiluzuobiaoX")),
                        (entity.getPersistentData().getDouble("jiluzuobiaoY") + linshientity.getBbHeight() / 2), (entity.getPersistentData().getDouble("jiluzuobiaoZ")), 2, 0.1, 2, 0.1, 0);
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 10) {
                entity.getPersistentData().putString("jinengjinxing", "释放");
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放")) {
            CompoundTag friendlist = new CompoundTag();
            friendlist = (entity.getPersistentData()).getCompound("friendlist");
            team = Relations.team(entity);
            if (world instanceof ServerLevel _serverLevelForEntitySpawn) {
                AttackDamage = 0;
                friendlist.putInt("AttackDamage", (int) AttackDamage);
                AttackUUID = entity.getStringUUID();
                friendlist.putString("AttackUUID", AttackUUID);
                Damage_multiplier = 1;
                friendlist.putInt("Damage_multiplier", (int) Damage_multiplier);
                jiaodu = entity.getYRot();
                friendlist.putInt("jiaodu", (int) jiaodu);
                SkillSummons.summonEntityIceSpike(entity, new Vec3(entity.getPersistentData().getDouble("jiluzuobiaoX"),
                        entity.getPersistentData().getDouble("jiluzuobiaoY"), entity.getPersistentData().getDouble("jiluzuobiaoZ")), friendlist);
            }
            entity.getPersistentData().putString("jinengjinxing", "冰封");
        }
    }

    /** 来源 Yaoshou053bingzhuaProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou053bingzhua(LevelAccessor world, Entity entity) {
        String team = "";
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "跳跃");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_053")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou053.skill02");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("跳跃") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            SkillIce05shifang(world, entity);
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 SkillIce05shifangProcedure：保留阶段、条件、数值和调用顺序。 */
    static void SkillIce05shifang(LevelAccessor world, Entity entity) {
        if (!(world instanceof ServerLevel)) return;
        BlockPos target = entity.level().clip(new ClipContext(entity.getEyePosition(1f),
                entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(1)), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, entity)).getBlockPos();
        CompoundTag friendlist = entity.getPersistentData().getCompound("friendlist");
        int attackDamage = entity.getPersistentData().getString("jineng").equals("冰脊")
                ? 1 : (int) Math.round(entity.getPersistentData().getDouble("LEVELnumber") / 4);
        friendlist.putInt("AttackDamage", attackDamage);
        friendlist.putString("AttackUUID", entity.getStringUUID());
        friendlist.putInt("Damage_multiplier", 1);
        friendlist.putInt("jiaodu", (int) entity.getYRot());
        entity.getPersistentData().put("friendlist", friendlist);
        SkillSummons.summonEntityIceSpike(entity, new Vec3(target.getX(), entity.getY(), target.getZ()));
    }
}
