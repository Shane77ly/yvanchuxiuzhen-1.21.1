package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 003号妖兽的来源逐刻技能与阶段流程。 */
final class Yaoshou003Behavior {
    private Yaoshou003Behavior() { }

    /** 来源 Yaoshou003DangShiTiShouShangShiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void hurt(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (entity.isAlive()) {
            if (!Relations.isFriendly(entity, sourceentity)) {
                entity.getPersistentData().putString("linshigongjizhe", (sourceentity.getStringUUID()));
                if (entity.getPersistentData().getBoolean("beidaduan")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("empty");
                    }
                    entity.getPersistentData().putBoolean("beidaduan", (false));
                }
                if ((entity.getPersistentData().getString("jineng")).equals("")) {
                    if (!entity.level().isClientSide) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.attacked");
                        }
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou003DangShiTiGengXinKeShiProcedure：保留阶段条件、候选顺序及随机调用。 */
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
                                    if (jvli2 > 8) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "大跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "冲刺");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    } else if (jvli2 > 2) {
                                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) < (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / 2) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "吃草");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "左前跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "右前跳");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "大顶");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 40);
                                    }
                                    if (jvli2 < 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "震地");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        if (entity.getY() >= linshientity.getY() + 2) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 100);
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        }
                                    }
                                }
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false)) {
                                    if (jvli2 < 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    }
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
                            if (!(entity.getPersistentData().getString("jinengjinxing")).equals("移动中")) {
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
                            if (!(entity.getPersistentData().getString("jinengjinxing")).equals("准备") && !(entity.getPersistentData().getString("jinengjinxing")).equals("攻击")) {
                                entity.getPersistentData().putString("jinengjinxing", "准备");
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 5) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
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
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 11) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("震地")) {
                            Yaoshou003skillzhendi(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("大顶")) {
                            Yaoshou003skilldading(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("右前跳")) {
                            Yaoshou003skillyouqiantiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("左前跳")) {
                            Yaoshou003skillzuoqiantiao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("后蹬")) {
                            Yaoshou003skillhoudeng(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("吃草")) {
                            Yaoshou003skillchicao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("冲刺")) {
                            Yaoshou003skillchongci(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("大跳")) {
                            Yaoshou003skilldatiao(world, entity);
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

    /** 来源 Yaoshou003skilldatiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skilldatiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill02");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 12) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 1, (entity.getLookAngle().z)));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 30) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillchongciProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillchongci(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (entity instanceof Mob _entity)
                _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 2);
            entity.getPersistentData().putString("jinengjinxing", "释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.sprint");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.sprint");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.sprint");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") > 60) {
            Yaoshou037054Support.finish(entity);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放")) {
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 <= 2) {
                entity.getPersistentData().putDouble("entitytick", 0);
                entity.getPersistentData().putDouble("entitytick2", 0);
                entity.getPersistentData().putString("jinengjinxing", "");
                entity.getPersistentData().putString("jineng", "大顶");
            } else {
                entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
                if (entity.getPersistentData().getDouble("entitytick2") >= 20) {
                    if (entity instanceof Mob _entity)
                        _entity.getNavigation().moveTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), 2);
                    entity.getPersistentData().putDouble("entitytick2", 0);
                }
            }
        }
    }

    /** 来源 Yaoshou003skillchicaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillchicao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.getPersistentData().putString("jinengjinxing", "吃草");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.graze");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.graze");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.graze");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("吃草") && entity.getPersistentData().getDouble("entitytick") >= 23) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 3));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.HAPPY_VILLAGER, (entity.getX()), (entity.getY() + entity.getBbHeight()), (entity.getZ()), 20, 1, 1, 1, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("stripgreen"), (entity.getX()), (entity.getY()), (entity.getZ()), 20, 1, 0, 1, 1);
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillhoudengProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillhoudeng(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((entity.getX() * 2 - linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (entity.getZ() * 2 - linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.superbackkick");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.superbackkick");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.superbackkick");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 5) {
            shijiaoX = -(entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(3)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX());
            shijiaoZ = -(entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(3)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ());
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + entity.getBbHeight() / 2), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smokedarkgrey"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 12, 0, 0, 0, 1);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 20, 0, 0, 0, 1);
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
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3((-(entity.getLookAngle().x)), 1, (-(entity.getLookAngle().z))));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 10) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillzuoqiantiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillzuoqiantiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill03");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill03");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill03");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 4) {
            jiaodu = entity.getYRot() - 45;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.4, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 14) {
            Yaoshou037054Support.finish(entity);
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 >= 5) {
                entity.getPersistentData().putString("jineng", "右前跳");
            } else {
                entity.getPersistentData().putString("jineng", "大顶");
            }
        }
    }

    /** 来源 Yaoshou003skillyouqiantiaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillyouqiantiao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double jvli1 = 0;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill04");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill04");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill04");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 4) {
            jiaodu = entity.getYRot() + 45;
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(jiaodu * (Math.PI / 180)))), 0.4, (1 * Math.cos(jiaodu * (Math.PI / 180)))));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 14) {
            Yaoshou037054Support.finish(entity);
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 >= 5) {
                entity.getPersistentData().putString("jineng", "左前跳");
            } else {
                entity.getPersistentData().putString("jineng", "大顶");
            }
        }
    }

    /** 来源 Yaoshou003skilldadingProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skilldading(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.attack");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.attack");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.attack");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 5) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((2 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY()), (entityiterator.getZ()), 20, 0, 0, 0, 1);
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
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 1, (entity.getLookAngle().z)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou003skillzhendiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou003skillzhendi(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
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
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "起跳");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou003.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou021.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou042.skill02");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("起跳") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            entity.getPersistentData().putString("jinengjinxing", "释放");
            entity.setDeltaMovement(new Vec3(0, 0.4, 0));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            R = 2;
            speed = 0.5;
            if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                particleID = "yvanchuxiuzhen:smokedeeporange";
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                particleID = "yvanchuxiuzhen:smokelightgreen";
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                particleID = "yvanchuxiuzhen:smokegolden";
            }
            particlenumber = 30;
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
            if (Yaoshou037054Support.isKind(entity, "yaoshou_003")) {
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8, 2, 8), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (entityiterator.onGround()) {
                                if (world instanceof ServerLevel _level)
                                    _level.sendParticles(Particles.type("smallexplosiondeeporange"), (entityiterator.getX()), (entityiterator.getY() + entity.getBbHeight() / 2), (entityiterator.getZ()), 1, 0,
                                            0, 0, 0);
                                {
                                    Entity _ent = entityiterator;
                                    if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                        _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                                ("shanghai" + " " + "土" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "1.3"));
                                }
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 80, 1));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                                if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                    entityiterator.setDeltaMovement(new Vec3(0, 0.5, 0));
                                }
                            }
                        }
                    }
                }
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_021")) {
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8, 2, 8), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionlightgreen"), (entityiterator.getX()), (entityiterator.getY() + entity.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0,
                                        0, 0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "风" + " " + entity.getStringUUID() + " " + "false" + " " + "4" + " " + "0.8"));
                            }
                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 1), 0.5, ((entityiterator.getZ() - entity.getZ()) * 1)));
                            }
                            if (entityiterator.onGround()) {
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_001"), 40, 0));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 40, 0));
                            }
                        }
                    }
                }
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_042")) {
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                SkillDelays.after(5, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                SkillDelays.after(10, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                SkillDelays.after(15, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                SkillDelays.after(20, () -> {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thundergolden"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("thunderlightyellow"), (entity.getX()), (entity.getY()), (entity.getZ()), 4, 5, 5, 5, 0);

                });
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getBbWidth() + 7) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionyellow"), (entityiterator.getX()), (entityiterator.getY() + entity.getBbHeight() / 2), (entityiterator.getZ()), 1, 0, 0, 0,
                                        0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "雷" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "1.2"));
                            }
                            if (entityiterator.onGround()) {
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
                                if (entityiterator instanceof LivingEntity _entity)
                                    _entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 40, 0));
                            }
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
