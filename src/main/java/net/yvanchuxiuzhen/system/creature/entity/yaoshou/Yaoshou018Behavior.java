package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
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

/** 018号妖兽的来源逐刻技能与阶段流程。 */
final class Yaoshou018Behavior {
    private Yaoshou018Behavior() { }

    /** 来源 Yaoshou018DangShiTiShouShangShiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void hurt(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (entity.isAlive()) {
            if (!Relations.isFriendly(entity, sourceentity)) {
                entity.getPersistentData().putString("linshigongjizhe", (sourceentity.getStringUUID()));
                if (entity.getPersistentData().getBoolean("beidaduan")) {
                    if (Yaoshou037054Support.isKind(entity, "yaoshou_018")) {
                        ((YaoshouEntity) entity).setAnimation("empty");
                    }
                    entity.getPersistentData().putBoolean("beidaduan", (false));
                }
                if ((entity.getPersistentData().getString("jineng")).equals("")) {
                    if (!entity.level().isClientSide) {
                        if (Yaoshou037054Support.isKind(entity, "yaoshou_018")) {
                            ((YaoshouEntity) entity).setAnimation("animation.yaoshou018.attacked");
                        }
                    }
                }
            }
        }
    }

    /** 来源 Yaoshou018DangShiTiGengXinKeShiProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void tick(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
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
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "逃跑");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "卷缩滚动");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "靠近");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "跳跃滚动");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "卷缩滚动");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                    }
                                }
                                if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("effect_033")) : false) && jvli2 < 3) {
                                    shunxvjishu = shunxvjishu + 1;
                                    entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                    entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                    entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
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
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_018")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou018.attack");
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
                                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
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
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 9) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("卷缩滚动")) {
                            Yaoshou018jvansuogundong(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("逃跑")) {
                            Yaoshou018taopao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("跳跃滚动")) {
                            Yaoshou018tiaoyvegundong(world, entity);
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

    /** 来源 Yaoshou018tiaoyvegundongProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou018tiaoyvegundong(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "翻滚");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_018")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou018.skill01");
                }
            }
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.5, (entity.getLookAngle().z)));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚") && entity.getPersistentData().getDouble("entitytick") >= 38) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚")) {
            if (entity.onGround()) {
                if (world instanceof ServerLevel _level)
                    _level.sendParticles(Particles.type("smokeskyblue"), (entity.getX()), (entity.getY()), (entity.getZ()), 3, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
                if (entity.getPersistentData().getDouble("LEVELnumber") >= 21) {
                    entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                }
                entity.setDeltaMovement(new Vec3((-(1 * Math.sin(entity.getYRot() * (Math.PI / 180)))), 0, (1 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
                entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
                if (entity.getPersistentData().getDouble("entitytick2") >= 5) {
                    {
                        final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                        List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                .toList();
                        for (Entity entityiterator : _entfound) {
                            if (!Relations.isFriendly(entity, entityiterator)) {
                                if (world instanceof ServerLevel _level)
                                    _level.sendParticles(Particles.type("smallexplosionskyblue"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()),
                                            1, 0, 0, 0, 0);
                                {
                                    Entity _ent = entityiterator;
                                    if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                        _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                                ("shanghai" + " " + "光" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "0.8"));
                                }
                                if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                    entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 0.4), 0.3, ((entityiterator.getZ() - entity.getZ()) * 0.4)));
                                }
                            }
                        }
                    }
                    entity.getPersistentData().putDouble("entitytick2", 0);
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 40) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou018taopaoProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou018taopao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        double shijiaoX = 0;
        double jvli1 = 0;
        double shijiaoZ = 0;
        double shijiaoY = 0;
        double buchong = 0;
        double jiaodu = 0;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_018")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou018.skill01");
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "调整视角");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("调整视角") && entity.getPersistentData().getDouble("entitytick") >= 2) {
            jiaodu = entity.getYRot() + 180;
            {
                Entity _ent = entity;
                _ent.setYRot((float) jiaodu);
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
            entity.getPersistentData().putString("jinengjinxing", "翻滚");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚") && entity.getPersistentData().getDouble("entitytick") >= 38) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokeskyblue"), (entity.getX()), (entity.getY()), (entity.getZ()), 3, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(entity.getYRot() * (Math.PI / 180)))), 0, (1 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 5) {
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionskyblue"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1,
                                        0, 0, 0, 0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "光" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "0.8"));
                            }
                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 0.4), 0.3, ((entityiterator.getZ() - entity.getZ()) * 0.4)));
                            }
                        }
                    }
                }
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 40) {
            Yaoshou037054Support.finish(entity);
            buchong = entity.getBbWidth() / 2;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((1.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
            if (jvli1 >= 16) {
                entity.getPersistentData().putString("gongjizhe", "");
                entity.getPersistentData().putString("linshigongjizhe", "");
            }
        }
    }

    /** 来源 Yaoshou018jvansuogundongProcedure：保留阶段条件、候选顺序及随机调用。 */
    static void Yaoshou018jvansuogundong(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "翻滚");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_018")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou018.skill01");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚") && entity.getPersistentData().getDouble("entitytick") >= 38) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokeskyblue"), (entity.getX()), (entity.getY()), (entity.getZ()), 3, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
            if (entity.getPersistentData().getDouble("LEVELnumber") >= 21) {
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            }
            entity.setDeltaMovement(new Vec3((-(1 * Math.sin(entity.getYRot() * (Math.PI / 180)))), 0, (1 * Math.cos(entity.getYRot() * (Math.PI / 180)))));
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 5) {
                {
                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                            .toList();
                    for (Entity entityiterator : _entfound) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosionskyblue"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 1,
                                        0, 0, 0, 0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "光" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "0.8"));
                            }
                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 0.4), 0.3, ((entityiterator.getZ() - entity.getZ()) * 0.4)));
                            }
                        }
                    }
                }
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 40) {
            Yaoshou037054Support.finish(entity);
        }
    }
}
