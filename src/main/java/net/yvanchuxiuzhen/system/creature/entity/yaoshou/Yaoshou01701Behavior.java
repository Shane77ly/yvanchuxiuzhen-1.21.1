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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;



/** 017蜕壳动物形态的独立来源逐刻与长壳流程。 */
final class Yaoshou01701Behavior {
    private Yaoshou01701Behavior() { }

    /** 来源 Yaoshou01701DangShiTiGengXinKeShiProcedure：保留阶段顺序与数值。 */
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
                                    if (jvli2 > 12) {
                                        if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(MobEffects.WEAKNESS) : false)) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "长壳");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 20);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 80);
                                        }
                                    }
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "走位");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        if (jvli2 <= 7) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "魂灵泡沫");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        }
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "逃跑");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        if (entity instanceof TamableAnimal _tamEnt ? _tamEnt.isTame() : false) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 100);
                                        } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5) {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 60);
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        }
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 20);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "走位");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "魂灵泡沫");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "逃跑");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 60);
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
                                    if (entity instanceof Yaoshou01701Entity) {
                                        ((Yaoshou01701Entity) entity).setAnimation("animation.yaoshou01701.attack");
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
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("攻击") && entity.getPersistentData().getDouble("entitytick") >= 8) {
                                Yaoshou037054Support.finish(entity);
                            }
                        } else if ((entity.getPersistentData().getString("jineng")).equals("长壳")) {
                            Yaoshou017zhangke(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("走位")) {
                            Yaoshou017zouwei(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("魂灵泡沫")) {
                            Yaoshou017hunlingpaomo(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("逃跑")) {
                            Yaoshou017taopao(world, entity);
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

    /** 来源 Yaoshou017taopaoProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017taopao(LevelAccessor world, Entity entity) {
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
            entity.getPersistentData().putString("jinengjinxing", "准备");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 5) {
            jiaodu = entity.getYRot() + Yaoshou037054Support.randomInt(160, 200);
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
            entity.getPersistentData().putString("jinengjinxing", "逃跑");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("逃跑") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("逃跑")) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0, (entity.getLookAngle().z)));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokedeeppurple"), (entity.getX()), (entity.getY()), (entity.getZ()), 5, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0.1);
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 25) {
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

    /** 来源 Yaoshou017hunlingpaomoProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017hunlingpaomo(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 3) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            entity.getPersistentData().putString("jinengjinxing", "第一击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("第一击") && entity.getPersistentData().getDouble("entitytick") >= 6) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            entity.getPersistentData().putString("jinengjinxing", "第二击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("第二击") && entity.getPersistentData().getDouble("entitytick") >= 9) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            entity.getPersistentData().putString("jinengjinxing", "第三击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("第三击") && entity.getPersistentData().getDouble("entitytick") >= 12) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            entity.getPersistentData().putString("jinengjinxing", "第四击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("第四击") && entity.getPersistentData().getDouble("entitytick") >= 15) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            if (world instanceof ServerLevel projectileLevel) {
                Projectile _entityToSpawn = new Object() {
                    public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new Yaoshou017skill03Entity(CreatureSetup.<Yaoshou017skill03Entity>entityType("projectile_yaoshou_017skill_03").get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        ((Yaoshou017skill03Entity) entityToSpawn).setSourceKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                    }
                }.getArrow(projectileLevel, entity, (float) 0.1, 1);
                _entityToSpawn.setPos((entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()));
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + 0.1), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou017zouweiProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017zouwei(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.getPersistentData().putString("jinengjinxing", "准备");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 5) {
            if (entity instanceof Mob _entity)
                _entity.getNavigation().moveTo((entity.getX() + Yaoshou037054Support.randomInt(-10, 10)), (entity.getY()), (entity.getZ() + Yaoshou037054Support.randomInt(-10, 10)), 2);
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 25) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou017zhangkeProcedure：保留阶段顺序与数值。 */
    static void Yaoshou017zhangke(LevelAccessor world, Entity entity) {
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.getPersistentData().putString("jinengjinxing", "长壳");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("长壳") && entity.getPersistentData().getDouble("entitytick") >= 60) {

            if (world instanceof ServerLevel _serverLevelForEntitySpawn) {
                Entity _entityForSpawning = new Yaoshou01701Entity(CreatureSetup.<Yaoshou01701Entity>entityType("yaoshou_01701").get(), _serverLevelForEntitySpawn);
                _entityForSpawning.moveTo((entity.getX()), (entity.getY()), (entity.getZ()), world.getRandom().nextFloat() * 360F, 0);
                Relations.shareEntityAllegiance(entity, _entityForSpawning);
                _entityForSpawning.getPersistentData().putString("LEVELname", (entity.getPersistentData().getString("LEVELname")));
                _entityForSpawning.getPersistentData().putDouble("LEVELnumber", (entity.getPersistentData().getDouble("LEVELnumber")));
                _entityForSpawning.getPersistentData().putDouble("experience_Max", (entity.getPersistentData().getDouble("experience_Max")));
                _entityForSpawning.getPersistentData().putDouble("experience", (entity.getPersistentData().getDouble("experience")));
                _entityForSpawning.getPersistentData().putDouble("experience_Stand", (entity.getPersistentData().getDouble("experience_Stand")));
                _entityForSpawning.getPersistentData().putDouble("experience_rate", (entity.getPersistentData().getDouble("experience_rate")));
                _entityForSpawning.getPersistentData().putDouble("experience_hinder", (entity.getPersistentData().getDouble("experience_hinder")));
                ((LivingEntity) _entityForSpawning).getAttribute(YaoshouAttributes.SIZE).setBaseValue(((LivingEntity) entity).getAttribute(YaoshouAttributes.SIZE).getValue());
                _entityForSpawning.getPersistentData().putDouble("tame_num", (entity.getPersistentData().getDouble("tame_num")));
                _entityForSpawning.getPersistentData().putString("character", (entity.getPersistentData().getString("character")));
                _entityForSpawning.getPersistentData().putDouble("character_def", (entity.getPersistentData().getDouble("character_def")));
                _entityForSpawning.getPersistentData().putDouble("character_att", (entity.getPersistentData().getDouble("character_att")));
                _entityForSpawning.getPersistentData().putDouble("character_interval", (entity.getPersistentData().getDouble("character_interval")));
                _entityForSpawning.getPersistentData().putDouble("character_resist", (entity.getPersistentData().getDouble("character_resist")));
                _entityForSpawning.getPersistentData().putString("health_Max_LEVEL", (entity.getPersistentData().getString("health_Max_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("health_Max", (entity.getPersistentData().getDouble("health_Max")));
                ((LivingEntity) _entityForSpawning).getAttribute(Attributes.MAX_HEALTH)
                        .setBaseValue(((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).getValue());
                if (_entityForSpawning instanceof LivingEntity _entity)
                    _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1);
                _entityForSpawning.getPersistentData().putString("health_Rec_LEVEL", (entity.getPersistentData().getString("health_Rec_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("health_Rec", (entity.getPersistentData().getDouble("health_Rec")));
                _entityForSpawning.getPersistentData().putString("power_Max_LEVEL", (entity.getPersistentData().getString("power_Max_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("power_Max", (entity.getPersistentData().getDouble("power_Max")));
                _entityForSpawning.getPersistentData().putString("power_Rec_LEVEL", (entity.getPersistentData().getString("power_Rec_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("power_Rec", (entity.getPersistentData().getDouble("power_Rec")));
                _entityForSpawning.getPersistentData().putString("soul_Max_LEVEL", (entity.getPersistentData().getString("soul_Max_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("soul_Max", (entity.getPersistentData().getDouble("soul_Max")));
                _entityForSpawning.getPersistentData().putString("soul_Rec_LEVEL", (entity.getPersistentData().getString("soul_Rec_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("soul_Rec", (entity.getPersistentData().getDouble("soul_Rec")));
                _entityForSpawning.getPersistentData().putString("attack_Num_LEVEL", (entity.getPersistentData().getString("attack_Num_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("attack_Num", (entity.getPersistentData().getDouble("attack_Num")));
                ((LivingEntity) _entityForSpawning).getAttribute(Attributes.ATTACK_DAMAGE)
                        .setBaseValue(((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                _entityForSpawning.getPersistentData().putString("attack_Def_LEVEL", "强劲");
                _entityForSpawning.getPersistentData().putDouble("attack_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 2.7, 2)));
                _entityForSpawning.getPersistentData().putString("mana_Att_LEVEL", (entity.getPersistentData().getString("mana_Att_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("mana_Att", (entity.getPersistentData().getDouble("mana_Att")));
                _entityForSpawning.getPersistentData().putString("mana_Def_LEVEL", "勉强");
                _entityForSpawning.getPersistentData().putDouble("mana_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 2.9, 2)));
                _entityForSpawning.getPersistentData().putString("soul_Att_LEVEL", (entity.getPersistentData().getString("soul_Att_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("soul_Att", (entity.getPersistentData().getDouble("soul_Att")));
                _entityForSpawning.getPersistentData().putString("soul_Def_LEVEL", "强劲");
                _entityForSpawning.getPersistentData().putDouble("soul_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 2.7, 2)));
                _entityForSpawning.getPersistentData().putString("speed_LEVEL", (entity.getPersistentData().getString("speed_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("speed_number", (entity.getPersistentData().getDouble("speed_number")));
                ((LivingEntity) _entityForSpawning).getAttribute(Attributes.MOVEMENT_SPEED)
                        .setBaseValue(((LivingEntity) entity).getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                if (_entityForSpawning instanceof Mob _mobForSpawning)
                    _mobForSpawning.finalizeSpawn(_serverLevelForEntitySpawn, world.getCurrentDifficultyAt(_entityForSpawning.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                world.addFreshEntity(_entityForSpawning);
            }
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            if (!entity.level().isClientSide())
                entity.discard();
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("长壳")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SOUL, (entity.getX()), (entity.getY()), (entity.getZ()), 5, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smokeblack"), (entity.getX()), (entity.getY()), (entity.getZ()), 3, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
        }
    }
}
