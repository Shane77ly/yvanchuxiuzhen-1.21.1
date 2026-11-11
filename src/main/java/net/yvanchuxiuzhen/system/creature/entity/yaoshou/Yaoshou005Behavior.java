package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

/** 005号妖兽的来源逐刻技能与阶段流程。 */
final class Yaoshou005Behavior {
    private Yaoshou005Behavior() { }

    /** 来源 Yaoshou005DangShiTiShouShangShiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void hurt(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (entity.isAlive()) {
            if (!Relations.isFriendly(entity, sourceentity)) {
                entity.getPersistentData().putString("linshigongjizhe", (sourceentity.getStringUUID()));
                if (entity.getPersistentData().getBoolean("beidaduan")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                        ((YaoshouEntity) entity).setAnimation("empty");
                    }
                    entity.getPersistentData().putBoolean("beidaduan", (false));
                    entity.setSprinting((false));
                }
                if ((entity.getPersistentData().getString("jineng")).equals("")) {
                    if (!entity.level().isClientSide) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou005.attacked");
                        }
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou005DangShiTiGengXinKeShiProcedure：保留阶段条件、候选顺序及随机调用。 */
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
                                    if (jvli2 > 5) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冲锋");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.DAMAGE_BOOST) : false)) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "战吼");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 40);
                                        }
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "高高拱起");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "獠牙乱突");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "声如擂鼓");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 40);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    }
                                }
                                if (jvli2 < 3 && !(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false)) {
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
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
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou005.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 3) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getZ();
                                {
                                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
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
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冲锋")) {
                            Yaoshou005chongfeng(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("战吼")) {
                            Yaoshou005zhanhou(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("高高拱起")) {
                            Yaoshou005gaogaogongqi(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("獠牙乱突")) {
                            Yaoshou005liaoyaluantu(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("声如擂鼓")) {
                            Yaoshou005shengruleigu(world, entity);
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

    /** 来源 Yaoshou005shengruleiguProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou005shengruleigu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double xvnhuan = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou005.skill03");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 32) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
            xvnhuan = 1;
            for (int index0 = 0; index0 < (int) (4); index0++) {
                SkillDelays.after((int) xvnhuan, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("yinbo"), (entity.getX()), (entity.getY() + entity.getBbHeight()), (entity.getZ()), 1, 0, 0, 0, 0);

                });
                xvnhuan = xvnhuan + 5;
            }
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosionwhite"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0,
                                    0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "音波" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "1.2"));
                        }
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(0, 2, 0));
                        }
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 60) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou005liaoyaluantuProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou005liaoyaluantu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        boolean gongji = false;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double beishu = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "第一次攻击");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou005.skill02");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("第一次攻击") && entity.getPersistentData().getDouble("entitytick") >= 13) {
            entity.getPersistentData().putString("jinengjinxing", "第二次攻击");
            gongji = true;
            beishu = 0.5;
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("第二次攻击") && entity.getPersistentData().getDouble("entitytick") >= 25) {
            entity.getPersistentData().putString("jinengjinxing", "第三次攻击");
            gongji = true;
            beishu = 1;
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("第三次攻击") && entity.getPersistentData().getDouble("entitytick") >= 38) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
            gongji = true;
            beishu = 1.5;
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 49) {
            Yaoshou037054Support.finish(entity);
        }
        if (gongji) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4, 2, 4), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 12, 0, 0, 0, 1);
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
                        entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage * beishu));
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou005gaogaogongqiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou005gaogaogongqi(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        boolean keyi = false;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "憋坏");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou005.skill01");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("憋坏") && entity.getPersistentData().getDouble("entitytick") >= 10) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 20, 0, 0, 0, 1);
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
                        entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage * 1.5));
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(0, 2, 0));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou005zhanhouProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou005zhanhou(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        boolean keyi = false;
        double xvnhuan = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou005.skill03");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 32) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
            xvnhuan = 1;
            for (int index0 = 0; index0 < (int) (4); index0++) {
                SkillDelays.after((int) xvnhuan, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("yinbo"), (entity.getX()), (entity.getY() + entity.getBbHeight()), (entity.getZ()), 1, 0, 0, 0, 0);

                });
                xvnhuan = xvnhuan + 5;
            }
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, (int) Math.round(entity.getPersistentData().getDouble("LEVELnumber") / 4)));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 60) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou005chongfengProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou005chongfeng(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        boolean keyi = false;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "冲刺");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_005")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.attack");
                }
            }
            entity.setSprinting((true));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("冲刺") && entity.getPersistentData().getDouble("entitytick") >= 60) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("冲刺")) {
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 10) {
                if (entity instanceof Mob _entity)
                    _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 1.5);
                jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                if (jvli1 <= 3) {
                    entity.getPersistentData().putString("jinengjinxing", "结束");
                    keyi = true;
                }
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 60) {
            entity.setSprinting((false));
            Yaoshou037054Support.finish(entity);
            if (keyi) {
                entity.getPersistentData().putString("jineng", "獠牙乱突");
            }
        }
    }
}
