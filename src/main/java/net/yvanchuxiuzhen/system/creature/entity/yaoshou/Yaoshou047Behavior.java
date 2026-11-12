package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 047号妖兽来源的完整逐刻技能闭包。 */
final class Yaoshou047Behavior {
    private Yaoshou047Behavior() { }

    /** 来源 Yaoshou047DangShiTiGengXinKeShiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void tick(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        boolean qingchu = false;
        double jvli2 = 0;
        double jvli1 = 0;
        double qvanzhongdiejia = 0;
        double xvnhuan = 0;
        double suijishu = 0;
        double qvanzhongmax = 0;
        double shijiaoX = 0;
        double xvnhuan2 = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double shunxvjishu = 0;
        double buchong = 0;
        double jvli3 = 0;
        double damage = 0;
        if (entity.isAlive()) {
            if (entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_035")) : false) {
                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                    if (entity.isNoGravity()) {
                        entity.setNoGravity((false));
                        qingchu = true;
                    }
                    if (qingchu) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(Particles.type("brokenwhite"), (entity.getX()), (entity.getY()), (entity.getZ()), 20, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.2);
                        qingchu = false;
                    }
                }
            } else {
                if (!entity.isNoGravity()) {
                    entity.setNoGravity((true));
                }
            }
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
                            jvli3 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
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
                                    if (jvli2 < 24) {
                                        if (!entity.isVehicle()) {
                                            if (jvli3 > 5) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "乱羽");
                                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 40);
                                                if (entity.getLookAngle().y - linshientity.getLookAngle().y < 8) {
                                                    shunxvjishu = shunxvjishu + 1;
                                                    entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "恫吓");
                                                    entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                                }
                                            }
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "夜舞");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                            if (entity.getLookAngle().y - linshientity.getLookAngle().y < 10) {
                                                shunxvjishu = shunxvjishu + 1;
                                                entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "夜啼");
                                                entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                                entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 40);
                                            }
                                        }
                                    }
                                    if (jvli2 > 6) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), Math.round(10 + jvli2));
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        if (!entity.isVehicle()) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "飞离");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
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
                                entity.getPersistentData().putDouble("mudidiX", (linshientity.getX() + Yaoshou037054Support.randomInt(-7, 7)));
                                if (linshientity.onGround()) {
                                    entity.getPersistentData().putDouble("mudidiY", (linshientity.getY() + linshientity.getBbHeight() / 2 + 8));
                                } else {
                                    entity.getPersistentData().putDouble("mudidiY", (linshientity.getY() + linshientity.getBbHeight() / 2));
                                }
                                entity.getPersistentData().putDouble("mudidiZ", (linshientity.getZ() + Yaoshou037054Support.randomInt(-7, 7)));
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((entity.getPersistentData().getDouble("mudidiX")), (entity.getPersistentData().getDouble("mudidiY")), (entity.getPersistentData().getDouble("mudidiZ"))));
                                entity.getPersistentData().putString("jinengjinxing", "移动中");
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("移动中") && entity.getPersistentData().getDouble("entitytick") >= 40) {
                                Yaoshou037054Support.finish(entity);
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("移动中")) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((entity.getPersistentData().getDouble("mudidiX")), (entity.getPersistentData().getDouble("mudidiY")), (entity.getPersistentData().getDouble("mudidiZ"))));
                                entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), (entity.getLookAngle().y), (entity.getLookAngle().z)));
                                jvli1 = Math.sqrt(Math.pow(entity.getX() - entity.getPersistentData().getDouble("mudidiX"), 2) + Math.pow(entity.getY() - entity.getPersistentData().getDouble("mudidiY"), 2)
                                        + Math.pow(entity.getZ() - entity.getPersistentData().getDouble("mudidiZ"), 2));
                                if (jvli1 <= 2) {
                                    Yaoshou037054Support.finish(entity);
                                }
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("挣脱")) {
                            Yaoshou037054Support.escape(entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("普攻")) {
                            if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
                                entity.getPersistentData().putString("jinengjinxing", "准备");
                                if (!entity.level().isClientSide) {
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou047.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 3) {
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
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
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 12) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("恫吓")) {
                            Yaoshou047donghe(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("夜舞")) {
                            Yaoshou047yewu(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("乱羽")) {
                            Yaoshou047luanyv(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("夜啼")) {
                            Yaoshou047yeti(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("飞离")) {
                            Yaoshou047feili(world, entity);
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
        } else {
            if (entity.isNoGravity()) {
                entity.setNoGravity((false));
            }
            entity.setDeltaMovement(new Vec3(0, (-0.5), 0));
        }
    }

    /** 来源 Yaoshou047feiliProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047feili(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "准备");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 2) {
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
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 30) {
            Yaoshou037054Support.finish(entity);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束")) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z)));
        }
    }

    /** 来源 Yaoshou047yetiProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047yeti(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        String team = "";
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            CompoundTag friendlist = new CompoundTag();
            friendlist = (entity.getPersistentData()).getCompound("friendlist");
            team = Relations.team(entity);
            if (world instanceof ServerLevel) Skills.showDamageArea(entity, entity.position(), "animation.shanghaifanwei.shiliuge1", "", 20, 0);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou047.skill03");
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "鸣叫");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("鸣叫") && entity.getPersistentData().getDouble("entitytick") >= 40) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 44) {
            Yaoshou037054Support.finish(entity);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.LARGE_SMOKE, (entity.getX()), (entity.getY()), (entity.getZ()), 5, 8, 0, 8, 0.5);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("鸣叫")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SOUL, (entity.getX()), (entity.getY()), (entity.getZ()), 10, (entity.getBbWidth()), (entity.getBbHeight()), (entity.getBbWidth()), 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SMOKE, (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 10, 0, 0, 0, 1);
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 5) {
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosiondeeppurple"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()),
                                        1, 0, 0, 0, 0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "灵魂" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "0.5"));
                            }
                        }
                    }
                }
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        }
    }

    /** 来源 Yaoshou047luanyvProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047luanyv(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double damage = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou047.skill04");
                    }
                } else if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.skill01");
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "俯冲");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("俯冲") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getBbWidth() + 2) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
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
                        entityiterator.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) damage);
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "拔升");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("拔升") && entity.getPersistentData().getDouble("entitytick") >= 16) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("俯冲")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.setDeltaMovement(new Vec3((-(1.5 * Math.sin(entity.getYRot() * (Math.PI / 180)))), (-(1.5 * Math.sin(entity.getXRot() * (Math.PI / 180)))), (1.5 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("拔升")) {
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(entity.getYRot() * (Math.PI / 180)))), 0.5, (1 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
        }
    }

    /** 来源 Yaoshou047yewuProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047yewu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double r = 0;
        double mubiaoY = 0;
        double jiaodu = 0;
        double mubiaoZ = 0;
        double mubiaoX = 0;
        double jvli1 = 0;
        double jvli2 = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putDouble("suijishangxian", (Yaoshou037054Support.randomInt(60, 120)));
            entity.getPersistentData().putString("jinengjinxing", "盘旋");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束")) {
            Yaoshou037054Support.finish(entity);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("盘旋")) {
            r = 10;
            jiaodu = entity.getPersistentData().getDouble("entitytick") * 3;
            mubiaoX = linshientity.getX() + r * Math.cos(jiaodu * (Math.PI / 180));
            mubiaoZ = linshientity.getZ() + r * Math.sin(jiaodu * (Math.PI / 180));
            if (linshientity.onGround()) {
                mubiaoY = linshientity.getY() + 10;
            } else {
                mubiaoY = linshientity.getY() + 3;
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(mubiaoX, mubiaoY, mubiaoZ));
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 0.5), (entity.getLookAngle().y * 0.5), (entity.getLookAngle().z * 0.5)));
            if (entity.getPersistentData().getDouble("entitytick") >= entity.getPersistentData().getDouble("suijishangxian")) {
                if (!entity.level().isClientSide) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou014.skill02");
                        }
                    } else if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou047.skill04");
                        }
                    }
                }
                entity.getPersistentData().putString("jinengjinxing", "俯冲");
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("俯冲")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x * 1.5), (entity.getLookAngle().y * 1.5), (entity.getLookAngle().z * 1.5)));
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 8) {
                entity.getPersistentData().putString("jinengjinxing", "拔升");
                entity.getPersistentData().putDouble("linshishijiaoX", (entity.getLookAngle().x));
                entity.getPersistentData().putDouble("linshishijiaoZ", (entity.getLookAngle().z));
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
            if (!entity.isVehicle()) {
                jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                jvli2 = jvli1 - (entity.getBbWidth() / 2 + linshientity.getBbWidth() / 2);
                if (jvli2 <= 2) {
                    linshientity.startRiding(entity);
                    entity.getPersistentData().putString("jinengjinxing", "拔升");
                    entity.getPersistentData().putDouble("linshishijiaoX", (entity.getLookAngle().x));
                    entity.getPersistentData().putDouble("linshishijiaoZ", (entity.getLookAngle().z));
                    entity.getPersistentData().putDouble("entitytick2", 0);
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("拔升")) {
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (Yaoshou037054Support.isKind(entity, "yaoshou_014")) {
                if (entity.getPersistentData().getDouble("entitytick2") >= 34) {
                    entity.getPersistentData().putString("jinengjinxing", "结束");
                    entity.getPersistentData().putDouble("entitytick2", 0);
                }
            } else if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                if (entity.getPersistentData().getDouble("entitytick2") >= 8) {
                    entity.getPersistentData().putString("jinengjinxing", "结束");
                    entity.getPersistentData().putDouble("entitytick2", 0);
                }
            }
            entity.setDeltaMovement(new Vec3((entity.getPersistentData().getDouble("linshishijiaoX") * 1.5), 0.5, (entity.getPersistentData().getDouble("linshishijiaoZ") * 1.5)));
        }
    }

    /** 来源 Yaoshou047dongheProcedure：保留阶段、条件、数值和调用顺序。 */
    static void Yaoshou047donghe(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double dijinjvli = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "吓唬");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_047")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou047.skill01");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("吓唬") && entity.getPersistentData().getDouble("entitytick") >= 6) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            dijinjvli = 1;
            for (int index0 = 0; index0 < (int) (4); index0++) {
                buchong = entity.getBbWidth() / 2;
                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((dijinjvli + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                        .getX();
                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((dijinjvli + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                        .getY();
                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((dijinjvli + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                        .getZ();
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(ParticleTypes.SMOKE, shijiaoX, shijiaoY, shijiaoZ, (int) (dijinjvli * 4), dijinjvli, dijinjvli, dijinjvli, 0);
                {
                    final Vec3 _center = new Vec3(shijiaoX, shijiaoY, shijiaoZ);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((dijinjvli + 2) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosiondarkgrey"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1,
                                        0, 0, 0, 0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "灵魂" + " " + entity.getStringUUID() + " " + "false" + " " + "5" + " " + "1"));
                            }
                            if (entityiterator instanceof LivingEntity _entity)
                                _entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                        }
                    }
                }
                dijinjvli = dijinjvli + 2;
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 44) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
