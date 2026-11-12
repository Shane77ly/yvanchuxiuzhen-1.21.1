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
            entity.getPersistentData().putDouble("entitytick", (entity.getPersistentData().getDouble("entitytick") + 1));
            if ((entity.getPersistentData().getString("jineng")).equals("")) {
                if (!(entity.getPersistentData().getString("gongjizhe")).equals(entity.getPersistentData().getString("linshigongjizhe"))) {
                    entity.getPersistentData().putString("gongjizhe", (entity.getPersistentData().getString("linshigongjizhe")));
                }
            }
            if (!(entity.getPersistentData().getString("gongjizhe")).equals("")) {
                linshientity = Xiuzhe0101Support.target(world, entity);
            }
            if (!(linshientity == null)) {
                if (linshientity.isAlive()) {
                    if ((entity.getPersistentData().getString("jineng")).equals("")) {
                        if ((entity.getPersistentData().getString("yvdingjineng")).equals("")) {
                            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                            jvli2 = jvli1 - (entity.getBbWidth() / 2 + linshientity.getBbWidth() / 2);
                            gaoducha = linshientity.getY() - entity.getY();
                            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            shunxvjishu = 0;
                            if (entity.isPassenger()) {
                                shunxvjishu = shunxvjishu + 1;
                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "挣脱载具");
                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 10);
                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                            } else {
                                if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(StatusEffects.holder("effect_034")) ? _livEnt.getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0) >= 4) {
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "后空翻");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 10);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "左翻滚");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 10);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "右翻滚");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 10);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "前翻滚");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 10);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "回血丹药");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 10);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 50);
                                }
                                if (gaoducha >= 2) {
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "蹬墙踢");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "震山符");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "回血丹药");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                } else if (gaoducha <= -2) {
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "踏地");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "回血丹药");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "滑铲");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "撼地");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                } else {
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)), "靠近");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), Math.round(10 * jvli2));
                                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.1) {
                                        if (jvli2 >= 5) {
                                            jinengxvnhuan = 30;
                                            for (int index0 = 0; index0 < (int) (5); index0++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)),
                                                        (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(jinengxvnhuan)))));
                                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                                jinengxvnhuan = jinengxvnhuan - 1;
                                            }
                                        } else {
                                            jinengxvnhuan = 25;
                                            for (int index1 = 0; index1 < (int) (5); index1++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)),
                                                        (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(jinengxvnhuan)))));
                                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                                jinengxvnhuan = jinengxvnhuan + 1;
                                            }
                                        }
                                    } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5) {
                                        if (jvli2 >= 5) {
                                            jinengxvnhuan = 20;
                                            for (int index2 = 0; index2 < (int) (5); index2++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)),
                                                        (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(jinengxvnhuan)))));
                                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                                jinengxvnhuan = jinengxvnhuan - 1;
                                            }
                                        } else {
                                            jinengxvnhuan = 15;
                                            for (int index3 = 0; index3 < (int) (5); index3++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)),
                                                        (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(jinengxvnhuan)))));
                                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                                jinengxvnhuan = jinengxvnhuan + 1;
                                            }
                                        }
                                    } else {
                                        if (jvli2 >= 5) {
                                            jinengxvnhuan = 10;
                                            for (int index4 = 0; index4 < (int) (5); index4++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)),
                                                        (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(jinengxvnhuan)))));
                                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                                jinengxvnhuan = jinengxvnhuan - 1;
                                            }
                                        } else {
                                            jinengxvnhuan = 5;
                                            for (int index5 = 0; index5 < (int) (5); index5++) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(shunxvjishu)),
                                                        (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(jinengxvnhuan)))));
                                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(shunxvjishu)), 5);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(shunxvjishu)), 30);
                                                jinengxvnhuan = jinengxvnhuan + 1;
                                            }
                                        }
                                    }
                                }
                            }
                            xvnhuan = 1;
                            for (int index6 = 0; index6 < (int) (shunxvjishu); index6++) {
                                qvanzhongmax = qvanzhongmax + entity.getPersistentData().getDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(xvnhuan)));
                                xvnhuan = xvnhuan + 1;
                            }
                            suijishu = Xiuzhe0101Support.randomInt(1, (int) qvanzhongmax);
                            xvnhuan2 = 1;
                            for (int index7 = 0; index7 < (int) (shunxvjishu); index7++) {
                                qvanzhongdiejia = qvanzhongdiejia + entity.getPersistentData().getDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(xvnhuan2)));
                                if (qvanzhongdiejia >= suijishu) {
                                    entity.getPersistentData().putString("yvdingjineng", (entity.getPersistentData().getString(("linshijineng" + Xiuzhe0101Support.whole(xvnhuan2)))));
                                    entity.getPersistentData().putDouble("yvdingjinengjishi", (entity.getPersistentData().getDouble(("jinengjishi" + Xiuzhe0101Support.whole(xvnhuan2)))));
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
                            for (int index8 = 0; index8 < (int) (entity.getPersistentData().getDouble("jinengshuliang")); index8++) {
                                entity.getPersistentData().putString(("linshijineng" + Xiuzhe0101Support.whole(xvnhuan)), "");
                                entity.getPersistentData().putDouble(("jinengjishi" + Xiuzhe0101Support.whole(xvnhuan)), 0);
                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Xiuzhe0101Support.whole(xvnhuan)), 0);
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
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("移动中") && entity.getPersistentData().getDouble("entitytick") >= 20) {
                                Xiuzhe0101Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("普攻")) {
                            basicAttack(world, entity);
                        }
                        castSkill(world, entity);
                    }
                } else {
                    entity.getPersistentData().putString("gongjizhe", "");
                    entity.getPersistentData().putString("linshigongjizhe", "");
                    Xiuzhe0101Support.finish(entity);
                }
            } else {
                if (entity.getPersistentData().getDouble("entitytick") >= 40) {
                    if (entity instanceof Mob _entity)
                        _entity.getNavigation().moveTo((entity.getX() + Xiuzhe0101Support.randomInt(-3, 3)), (entity.getY()), (entity.getZ() + Xiuzhe0101Support.randomInt(-3, 3)), 1);
                    entity.getPersistentData().putDouble("entitytick", 0);
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
                entity.getPersistentData().putString("linshigongjizhe", (sourceentity.getStringUUID()));
                if (entity.getPersistentData().getBoolean("beidaduan")) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("empty");
                    }
                    entity.getPersistentData().putBoolean("beidaduan", (false));
                }
                if ((entity.getPersistentData().getString("jineng")).equals("")) {
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
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.getPersistentData().putString("jinengjinxing", "准备");
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
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 2) {
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
                        entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) entity.getPersistentData().getDouble("attack_Num"));
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "攻击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 6) {
            Xiuzhe0101Support.finish(entity);
        }
    }

    /** 来源Xiuzhe0101jinengshifangProcedure，保持条件、数值与阶段顺序。 */
    static void castSkill(LevelAccessor world, Entity entity) {

        if ((entity.getPersistentData().getString("jineng")).equals("直拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0101");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0101(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("炮拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0102(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("冲拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0103");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0103(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("连续出拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0104");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0104(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("旋风拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0104");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0105(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("闷拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0105");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0106(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("寸拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0106");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0107(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("碎骨拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0108(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("蓄力重拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0107");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0109(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("上勾拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0108");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0110(world, entity);
            }
        }
        if ((entity.getPersistentData().getString("jineng")).equals("下勾拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0105");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0111(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("碎金拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0112(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("幽海拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0113(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("荆棘拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0114(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("烈焰拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0115(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("碎石拳")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillqvan0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMeleeSkills.Npcskill0116(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("挣脱载具")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0101");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0201(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("蹬墙踢")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0202(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("震山符")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0101");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0203(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("回血丹药")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilleat0101");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0204(world, entity);
            }
        }
        if ((entity.getPersistentData().getString("jineng")).equals("踏地")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0103");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0205(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("撼地")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0104");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0206(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("滑铲")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skilltuoshen0105");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0207(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("后空翻")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.backroll01");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0208(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("左翻滚")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.leftroll01");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0209(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("右翻滚")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.rightroll01");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0210(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("前翻滚")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.roll01");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcMovementSkills.Npcskill0211(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("风雷符")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcTalismanSkills.Npcskill0301(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("流火符")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcTalismanSkills.Npcskill0302(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("强压符")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0101");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcTalismanSkills.Npcskill0303(world, entity);
            }
        }
        if ((entity.getPersistentData().getString("jineng")).equals("剑符")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcTalismanSkills.Npcskill0304(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("落雷符")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcTalismanSkills.Npcskill0305(world, entity);
            }
        } else if ((entity.getPersistentData().getString("jineng")).equals("爆燃符")) {
            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                if (!entity.level().isClientSide) {
                    if (entity instanceof Xiuzhe0101Entity) {
                        ((Xiuzhe0101Entity) entity).setAnimation("animation.xiuzhe0101.skillfulu0102");
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "开始");
            } else {
                NpcTalismanSkills.Npcskill0306(world, entity);
            }
        }
    }
}
