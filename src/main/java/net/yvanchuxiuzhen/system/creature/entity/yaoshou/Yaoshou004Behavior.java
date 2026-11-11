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

/** 004号妖兽的来源逐刻技能与阶段流程。 */
final class Yaoshou004Behavior {
    private Yaoshou004Behavior() { }

    /** 来源 Yaoshou004DangShiTiShouShangShiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void hurt(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (entity.isAlive()) {
            if (!Relations.isFriendly(entity, sourceentity)) {
                entity.getPersistentData().putString("linshigongjizhe", (sourceentity.getStringUUID()));
                if (entity.getPersistentData().getBoolean("beidaduan")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                        ((YaoshouEntity) entity).setAnimation("empty");
                    }
                    entity.getPersistentData().putBoolean("beidaduan", (false));
                }
                if ((entity.getPersistentData().getString("jineng")).equals("")) {
                    if (!entity.level().isClientSide) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou004.attacked");
                        }
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou004DangShiTiGengXinKeShiProcedure：保留阶段条件、候选顺序及随机调用。 */
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
                                    if (jvli2 > 6) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "野蛮冲撞");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) < (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / 2) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "吃草");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        }
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "金刚顶");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "践踏");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "乱摆头");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                    }
                                }
                                if (jvli2 < 3 && !(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false)) {
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
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou004.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 5) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((3 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
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
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 11) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("野蛮冲撞")) {
                            Yaoshou004yemanchongzhuang(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("吃草")) {
                            Yaoshou004chicao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("金刚顶")) {
                            Yaoshou004jingangding(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("践踏")) {
                            Yaoshou004jianta(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("乱摆头")) {
                            Yaoshou004luanbaitou(world, entity);
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

    /** 来源 Yaoshou004luanbaitouProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou004luanbaitou(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        boolean kaishi = false;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "释放");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou004.skill03");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou032.skill03");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 7) {
            entity.getPersistentData().putString("jinengjinxing", "顶一");
            kaishi = true;
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("顶一") && entity.getPersistentData().getDouble("entitytick") >= 13) {
            entity.getPersistentData().putString("jinengjinxing", "顶二");
            kaishi = true;
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("顶二") && entity.getPersistentData().getDouble("entitytick") >= 32) {
            entity.getPersistentData().putString("jinengjinxing", "顶三");
            kaishi = true;
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("顶三") && entity.getPersistentData().getDouble("entitytick") >= 49) {
            Yaoshou037054Support.finish(entity);
        }
        if (kaishi == true) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY()), (entityiterator.getZ()), 10, 0, 0, 0, 0.5);
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiondeeporange"), (entityiterator.getX()), (entityiterator.getY()), (entityiterator.getZ()), 1, 0, 0, 0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "土" + " " + entity.getStringUUID() + " " + "false" + " " + "8" + " " + "1.5"));
                        }
                    }
                }
            }
            kaishi = false;
        }
    }

    /** 来源 Yaoshou004jiantaProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou004jianta(LevelAccessor world, Entity entity) {
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
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.getPersistentData().putString("jinengjinxing", "践踏开始");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou004.skill04");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou032.skill04");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("践踏开始") && entity.getPersistentData().getDouble("entitytick") >= 24) {
            R = 3;
            speed = 0.5;
            particleID = "minecraft:smoke_large";
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
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.EXPLOSION, (entity.getX()), (entity.getY()), (entity.getZ()), 8, (entity.getBbWidth() / 2), 0, (entity.getBbWidth() / 2), 0);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getBbWidth() + 4), 2, (entity.getBbWidth() + 4)), e -> true).stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("smallexplosiondeeporange"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1, 0,
                                    0, 0, 0);
                        {
                            Entity _ent = entityiterator;
                            if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                        ("shanghai" + " " + "土" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "1.5"));
                        }
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 1, (entity.getLookAngle().z)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 30) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou004jingangdingProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou004jingangding(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "顶");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou004.skill02");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou032.skill02");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("顶") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList();
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (entity.getX()), (entity.getY()), (entity.getZ()), 20, 0, 0, 0, 1);
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
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
                            entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage * 1.2));
                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.5, (entity.getLookAngle().z)));
                            }
                        } else if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
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
                            entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage * 1.7));
                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.3), 1.2, (entity.getLookAngle().z * 0.3)));
                            }
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 30) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou004chicaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou004chicao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou004.eatgrass");
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "吃草");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("吃草") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 3));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.HAPPY_VILLAGER, (entity.getX()), (entity.getY() + entity.getBbHeight()), (entity.getZ()), 20, 1, 1, 1, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("stripgreen"), (entity.getX()), (entity.getY()), (entity.getZ()), 20, 1, 0, 1, 1);
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 36) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou004yemanchongzhuangProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou004yemanchongzhuang(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "奔跑");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_004")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou004.skill01");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_032")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou032.skill01");
                    }
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("奔跑") && entity.getPersistentData().getDouble("entitytick") >= 52) {
            entity.getPersistentData().putString("jinengjinxing", "刹车");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("奔跑")) {
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 21) {
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.8), 0, (entity.getLookAngle().z * 0.8)));
            } else {
                entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.8), 0, (entity.getLookAngle().z * 0.8)));
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokelightgray"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 10, 0, 0, 0, 0.1);
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 10) {
                buchong = entity.getBbWidth() / 2;
                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
                {
                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getBbWidth() + 2) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 10, 0, 0, 0, 1);
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
                            entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage * 0.8));
                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                entityiterator.setDeltaMovement(new Vec3((entity.getLookAngle().x), 1, (entity.getLookAngle().z)));
                            }
                        }
                    }
                }
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("刹车") && entity.getPersistentData().getDouble("entitytick") >= 80) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("刹车")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.LARGE_SMOKE, (entity.getX()), (entity.getY()), (entity.getZ()), 10, 0, 0, 0, 0.1);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokedarkgrey"), (entity.getX()), (entity.getY()), (entity.getZ()), 10, 0, 0, 0, 0.1);
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.2), 0, (entity.getLookAngle().z * 0.2)));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 80) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
