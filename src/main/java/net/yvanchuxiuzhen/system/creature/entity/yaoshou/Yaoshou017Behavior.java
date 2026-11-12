package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;


/** 017号妖兽的完整来源逐刻技能闭包。 */
final class Yaoshou017Behavior {
    private Yaoshou017Behavior() { }

    /** 来源 Yaoshou017DangShiTiGengXinKeShiProcedure：保留阶段顺序、条件和数值。 */
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
                                    if (!(entity instanceof TamableAnimal _tamEnt ? _tamEnt.isTame() : false)) {
                                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.3) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "脱壳而逃");
                                            entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        }
                                    }
                                    if (jvli2 > 3) {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "走位");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
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
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 50);
                                        } else {
                                            entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 10);
                                        }
                                    } else {
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "普攻");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "走位");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 5);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "魂灵泡沫");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "旋转喷射");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        shunxvjishu = shunxvjishu + 1;
                                        entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "水球术");
                                        entity.getPersistentData().putDouble(("jinengjishi" + Integer.toString((int) shunxvjishu)), 10);
                                        entity.getPersistentData().putDouble(("jinengqvanzhong" + Integer.toString((int) shunxvjishu)), 30);
                                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5) {
                                            shunxvjishu = shunxvjishu + 1;
                                            entity.getPersistentData().putString(("linshijineng" + Integer.toString((int) shunxvjishu)), "缩壳");
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
                                    if (Yaoshou037054Support.isKind(entity, "yaoshou_017")) {
                                        ((YaoshouEntity) entity).setAnimation("animation.yaoshou017.attack");
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                            } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("准备") && entity.getPersistentData().getDouble("entitytick") >= 3) {
                                buchong = entity.getBbWidth() / 2;
                                shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getX();
                                shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                                        .getBlockPos().getY();
                                shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale((0.5 + buchong))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
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
                        } else if ((entity.getPersistentData().getString("jineng")).equals("水球术")) {
                            Yaoshou017shuiqiushu(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("旋转喷射")) {
                            Yaoshou017xvanzhuanpenshe(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("走位")) {
                            Yaoshou017zouwei(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("魂灵泡沫")) {
                            Yaoshou017hunlingpaomo(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("逃跑")) {
                            Yaoshou017taopao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("脱壳而逃")) {
                            Yaoshou017tuokeertao(world, entity);
                        } else if ((entity.getPersistentData().getString("jineng")).equals("缩壳")) {
                            Yaoshou017suoke(world, entity);
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

    /** 来源 Yaoshou017suokeProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou017suoke(LevelAccessor world, Entity entity) {
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_017")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou017.skill01");
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "缩壳");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("缩壳") && entity.getPersistentData().getDouble("entitytick") >= 9) {
            entity.setShiftKeyDown((true));
            entity.getPersistentData().putString("jinengjinxing", "霸体");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("霸体") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 210, 1, (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "恢复");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("恢复") && entity.getPersistentData().getDouble("entitytick") >= 210) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("恢复")) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.SOUL, (entity.getX()), (entity.getY()), (entity.getZ()), 1, (entity.getBbWidth()), 0, (entity.getBbWidth()), 0);
            entity.getPersistentData().putDouble("entitytick2", (entity.getPersistentData().getDouble("entitytick2") + 1));
            if (entity.getPersistentData().getDouble("entitytick2") >= 20) {
                if (entity instanceof LivingEntity _entity)
                    _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 15, (int) Math.round(entity.getPersistentData().getDouble("LEVELnumber") / 5)));
                if (entity instanceof LivingEntity _entity)
                    _entity.setHealth((float) ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + 1));
                entity.getPersistentData().putDouble("entitytick2", 0);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 210) {
            entity.setShiftKeyDown((false));
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou017tuokeertaoProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou017tuokeertao(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_017")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou017.skill02");
                }
            }
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 9, 1, (false), (false)));
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Yaoshou037054Support.sound("block.honey_block.break"), SoundSource.HOSTILE, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Yaoshou037054Support.sound("block.honey_block.break"), SoundSource.HOSTILE, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 9) {

            if (world instanceof ServerLevel _serverLevelForEntitySpawn) {
                Entity _entityForSpawning = new Yaoshou01701Entity(CreatureSetup.<Yaoshou01701Entity>entityType("yaoshou_01701").get(), _serverLevelForEntitySpawn);
                _entityForSpawning.moveTo((entity.getX()), (entity.getY()), (entity.getZ()), world.getRandom().nextFloat() * 360F, 0);
                Relations.shareEntityAllegiance(entity, _entityForSpawning);
                if (entity instanceof TamableAnimal _tamEnt ? _tamEnt.isTame() : false) {
                    if (_entityForSpawning instanceof TamableAnimal _toTame && (entity instanceof TamableAnimal _tamEnt ? (Entity) _tamEnt.getOwner() : null) instanceof Player _owner)
                        _toTame.tame(_owner);
                }
                if (_entityForSpawning instanceof LivingEntity _entity)
                    _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
                if (_entityForSpawning instanceof LivingEntity _entity)
                    _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
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
                _entityForSpawning.getPersistentData().putString("attack_Def_LEVEL", "贫弱");
                _entityForSpawning.getPersistentData().putDouble("attack_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 3, 2)));
                _entityForSpawning.getPersistentData().putString("mana_Att_LEVEL", (entity.getPersistentData().getString("mana_Att_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("mana_Att", (entity.getPersistentData().getDouble("mana_Att")));
                _entityForSpawning.getPersistentData().putString("mana_Def_LEVEL", "贫弱");
                _entityForSpawning.getPersistentData().putDouble("mana_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 3, 2)));
                _entityForSpawning.getPersistentData().putString("soul_Att_LEVEL", (entity.getPersistentData().getString("soul_Att_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("soul_Att", (entity.getPersistentData().getDouble("soul_Att")));
                _entityForSpawning.getPersistentData().putString("soul_Def_LEVEL", "贫弱");
                _entityForSpawning.getPersistentData().putDouble("soul_Def", Math.ceil(2 + Math.pow(entity.getPersistentData().getDouble("LEVELnumber") / 3, 2)));
                _entityForSpawning.getPersistentData().putString("speed_LEVEL", (entity.getPersistentData().getString("speed_LEVEL")));
                _entityForSpawning.getPersistentData().putDouble("speed_number", (entity.getPersistentData().getDouble("speed_number")));
                ((LivingEntity) _entityForSpawning).getAttribute(Attributes.MOVEMENT_SPEED)
                        .setBaseValue(((LivingEntity) entity).getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                _entityForSpawning.getPersistentData().putString("jineng", "逃跑");
                if (_entityForSpawning instanceof Mob _mobForSpawning)
                    _mobForSpawning.finalizeSpawn(_serverLevelForEntitySpawn, world.getCurrentDifficultyAt(_entityForSpawning.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                world.addFreshEntity(_entityForSpawning);
            }
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            if (!entity.level().isClientSide())
                entity.discard();
        }
    }

    /** 来源 Yaoshou017taopaoProcedure：保留阶段顺序、条件和数值。 */
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

    /** 来源 Yaoshou017hunlingpaomoProcedure：保留阶段顺序、条件和数值。 */
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

    /** 来源 Yaoshou017zouweiProcedure：保留阶段顺序、条件和数值。 */
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

    /** 来源 Yaoshou017xvanzhuanpensheProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou017xvanzhuanpenshe(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 3) {
            entity.getPersistentData().putString("jinengjinxing", "预热旋转");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("预热旋转") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.getPersistentData().putString("jinengjinxing", "旋转攻击");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("预热旋转")) {
            {
                Entity _ent = entity;
                _ent.setYRot((float) (entity.getYRot() + 5));
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
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("旋转攻击") && entity.getPersistentData().getDouble("entitytick") >= 100) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("旋转攻击")) {
            {
                Entity _ent = entity;
                _ent.setYRot((float) (entity.getYRot() + 10));
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
                _entityToSpawn.shoot((entity.getLookAngle().x), (entity.getLookAngle().y + Yaoshou037054Support.randomDouble(0.1, 0.5)), (entity.getLookAngle().z), 1, 10);
                projectileLevel.addFreshEntity(_entityToSpawn);
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 110) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou017shuiqiushuProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou017shuiqiushu(LevelAccessor world, Entity entity) {
        Entity linshientity = null;
        linshientity = Yaoshou037054Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            if (!entity.level().isClientSide) {
                if (Yaoshou037054Support.isKind(entity, "yaoshou_017")) {
                    ((YaoshouEntity) entity).setAnimation("animation.yaoshou017.attack");
                }
            }
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 3) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
            SkillSummons.shootWater03(entity, entity.position().add(0, entity.getBbHeight() / 2, 0), entity.getLookAngle().add(0, 0.1, 0));
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 8) {
            Yaoshou037054Support.finish(entity);
        }
    }

    /** 来源 Yaoshou017skill03DangTouZhiWuJiZhongShiTiShiProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou017skill03DangTouZhiWuJiZhongShiTiShi(LevelAccessor world, Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        if (!Relations.isFriendly(sourceentity, entity)) {
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("smallexplosiondarkblue"), (entity.getX()), (entity.getY() + entity.getBbHeight() / 2), (entity.getZ()), 1, 0, 0, 0, 0);
            {
                Entity _ent = entity;
                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                            ("shanghai" + " " + "灵魂" + " " + sourceentity.getStringUUID() + " " + "false" + " " + "1" + " " + "0.5"));
            }
        }
    }

    /** 来源 Yaoshou017skill03TouZhiWuFeiXingTickProcedure：保留阶段顺序、条件和数值。 */
    static void Yaoshou017skill03TouZhiWuFeiXingTick(LevelAccessor world, Entity immediatesourceentity) {
        if (immediatesourceentity == null)
            return;
        if (world instanceof ServerLevel _level)
            _level.sendParticles(ParticleTypes.BUBBLE_POP, (immediatesourceentity.getX()), (immediatesourceentity.getY() + immediatesourceentity.getBbHeight() / 2), (immediatesourceentity.getZ()), 3, 0.2, 0.2, 0.2, 0);
        if (!immediatesourceentity.isNoGravity()) {
            immediatesourceentity.setNoGravity((true));
        }
        immediatesourceentity.getPersistentData().putDouble("flytick", (immediatesourceentity.getPersistentData().getDouble("flytick") + 1));
        if (immediatesourceentity.getPersistentData().getDouble("flytick") >= 40) {
            immediatesourceentity.getPersistentData().putDouble("flytick", 0);
            if (!immediatesourceentity.level().isClientSide())
                immediatesourceentity.discard();
        }
    }
}
