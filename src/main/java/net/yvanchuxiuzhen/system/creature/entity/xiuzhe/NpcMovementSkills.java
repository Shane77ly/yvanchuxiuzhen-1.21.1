package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 修者0101的十一种脱身、地形、恢复及翻滚技能。 */
public final class NpcMovementSkills {
    private NpcMovementSkills() { }

    public static void Npcskill0201(LevelAccessor world, Entity entity) {
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.LARGE_SMOKE, (entity.getX()), (entity.getY()), (entity.getZ()), 20, 1, 0, 1, 0);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (entityiterator instanceof Boat || entityiterator instanceof Minecart) {
                        if (!Relations.isFriendly(entity, entityiterator)) {
                            entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), 10);
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0202(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;
        double attack_Num = 0;
        double shijiaoZ = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 38, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 38, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 15) {
            entity.setDeltaMovement(new Vec3(0, 1, 0));
            entity.getPersistentData().putString("jinengjinxing", "跳跃");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("跳跃") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            attack_Num = ((LivingEntity) entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 1.5;
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            {
                final Vec3 _center = new Vec3(shijiaoX, (entity.getY() + 1.5), shijiaoZ);
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (world instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.CRIT, (linshientity.getX()), (linshientity.getY() + 1), (linshientity.getZ()), 10, 0, 0, 0, 1);
                        if (world instanceof Level _level) {
                            if (!_level.isClientSide()) {
                                _level.playSound(null, BlockPos.containing(entityiterator.getX(), entityiterator.getY() + 1.5, entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"),
                                        SoundSource.NEUTRAL, 1, 1);
                            } else {
                                _level.playLocalSound((entityiterator.getX()), (entityiterator.getY() + 1.5), (entityiterator.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:npcskill010108"), SoundSource.NEUTRAL,
                                        1, 1, false);
                            }
                        }
                        entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) attack_Num);
                        if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                            entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 1.2), ((entityiterator.getY() - entity.getY()) * 0.5), ((entityiterator.getZ() - entity.getZ()) * 1.2)));
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 38) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0203(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;

        double shijiaoZ = 0;
        double zweizhi = 0;
        double xweizhi = 0;
        double yweizhi = 0;
        double shijiaoY = 0;
        double fululevel = 0;
        double suiji = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 36, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 12) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1, entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY() + 1), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:fulushifang"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "释放");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("释放") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            shijiaoX = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getX();
            shijiaoY = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getY();
            shijiaoZ = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ();
            fululevel = 1;
            xweizhi = -(fululevel * 3);
            yweizhi = -(fululevel * 3);
            zweizhi = -(fululevel * 3);
            for (int index0 = 0; index0 < (int) (fululevel * 6 + 1); index0++) {
                yweizhi = -(fululevel * 3);
                for (int index1 = 0; index1 < (int) (fululevel * 6 + 1); index1++) {
                    zweizhi = -(fululevel * 3);
                    for (int index2 = 0; index2 < (int) (fululevel * 6 + 1); index2++) {
                        if (world.getBlockState(BlockPos.containing(shijiaoX + xweizhi, shijiaoY + yweizhi, shijiaoZ + zweizhi)).getDestroySpeed(world, BlockPos.containing(shijiaoX + xweizhi, shijiaoY + yweizhi, shijiaoZ + zweizhi)) < 10
                                && world.getBlockState(BlockPos.containing(shijiaoX + xweizhi, shijiaoY + yweizhi, shijiaoZ + zweizhi)).getDestroySpeed(world, BlockPos.containing(shijiaoX + xweizhi, shijiaoY + yweizhi, shijiaoZ + zweizhi)) >= 0) {
                            world.destroyBlock(BlockPos.containing(shijiaoX + xweizhi, shijiaoY + yweizhi, shijiaoZ + zweizhi), false);
                        }
                        zweizhi = zweizhi + 1;
                    }
                    yweizhi = yweizhi + 1;
                }
                xweizhi = xweizhi + 1;
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 37) {
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            suiji = Xiuzhe0101Support.randomInt(1, 5);
            entity.getPersistentData().putString("jineng", (entity.getPersistentData().getString("skillxvanze") + "" + Xiuzhe0101Support.whole(suiji)));
        }
    }

    public static void Npcskill0204(LevelAccessor world, Entity entity) {
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.HAPPY_VILLAGER, (entity.getX()), (entity.getY() + 1), (entity.getZ()), 10, 0.5, 0.5, 0.5, 0);
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 36) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0205(LevelAccessor world, Entity entity) {
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.LARGE_SMOKE, (entity.getX()), (entity.getY()), (entity.getZ()), 40, 3, 0.5, 3, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.WHITE_ASH, (entity.getX()), (entity.getY()), (entity.getZ()), 40, 3, 0.5, 3, 0);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(7 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (entityiterator.onGround()) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosiondeeporange"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()),
                                        1, 0, 0, 0, 0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "土" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1"));
                            }
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0206(LevelAccessor world, Entity entity) {
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 28, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 20) {
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"), SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), Xiuzhe0101Support.sound("yvanchuxiuzhen:yaoshou011skill02"), SoundSource.NEUTRAL, 1, 1, false);
                }
            }
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.LARGE_SMOKE, (entity.getX()), (entity.getY()), (entity.getZ()), 40, 6, 0.5, 6, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(ParticleTypes.WHITE_ASH, (entity.getX()), (entity.getY()), (entity.getZ()), 40, 6, 0.5, 6, 0);
            {
                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(11 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .collect(Collectors.toList());
                for (Entity entityiterator : _entfound) {
                    if (!Relations.isFriendly(entity, entityiterator)) {
                        if (entityiterator.onGround()) {
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(Particles.type("smallexplosiondeeporange"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()),
                                        1, 0, 0, 0, 0);
                            {
                                Entity _ent = entityiterator;
                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                            ("shanghai" + " " + "土" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1.5"));
                            }
                        }
                    }
                }
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 28) {
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0207(LevelAccessor world, Entity entity) {
        double shijiaoX = 0;

        double shijiaoZ = 0;
        double shijiaoY = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 12, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 4) {
            entity.getPersistentData().putString("jinengjinxing", "滑铲");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("滑铲") && entity.getPersistentData().getDouble("entitytick") >= 11) {
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("滑铲")) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), (-0.2), (entity.getLookAngle().z)));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("结束") && entity.getPersistentData().getDouble("entitytick") >= 12) {
            shijiaoX = entity.getX() + entity.getLookAngle().x * 2;
            shijiaoY = entity.getY() - 1.5;
            shijiaoZ = entity.getZ() + entity.getLookAngle().z * 2;
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("cailiao_231particle"), shijiaoX, shijiaoY, shijiaoZ, 5, 0, 0, 0, 0);
            if (world instanceof ServerLevel _level)
                _level.sendParticles(Particles.type("cailiao_238particle"), shijiaoX, shijiaoY, shijiaoZ, 20, 1, 1, 1, 0);
            if (world.isEmptyBlock(BlockPos.containing(shijiaoX, shijiaoY, shijiaoZ)) && world.isEmptyBlock(BlockPos.containing(shijiaoX, shijiaoY + 1, shijiaoZ))) {
                {
                    Entity _ent = entity;
                    _ent.teleportTo(shijiaoX, shijiaoY, shijiaoZ);
                    if (_ent instanceof ServerPlayer _serverPlayer)
                        _serverPlayer.connection.teleport(shijiaoX, shijiaoY, shijiaoZ, _ent.getYRot(), _ent.getXRot());
                }
            }
            entity.getPersistentData().putString("jineng", "");
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
        }
    }

    public static void Npcskill0208(LevelAccessor world, Entity entity) {
        double suiji = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 18, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 18, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 2) {
            entity.getPersistentData().putString("jinengjinxing", "翻滚");
            entity.setDeltaMovement(new Vec3((-(entity.getLookAngle().x)), 0.5, (-(entity.getLookAngle().z))));
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚") && entity.getPersistentData().getDouble("entitytick") >= 19) {
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            suiji = Xiuzhe0101Support.randomInt(16, 20);
            entity.getPersistentData().putString("jineng", (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(suiji)))));
        }
    }

    public static void Npcskill0209(LevelAccessor world, Entity entity) {
        double suiji = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 17, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 17, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 3) {
            {
                Entity _ent = entity;
                _ent.setYRot((float) (entity.getYRot() + 90));
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
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.5, (entity.getLookAngle().z)));
            entity.getPersistentData().putString("jinengjinxing", "翻滚");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            suiji = Xiuzhe0101Support.randomInt(6, 10);
            entity.getPersistentData().putString("jineng", (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(suiji)))));
        }
    }

    public static void Npcskill0210(LevelAccessor world, Entity entity) {
        double suiji = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 17, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 17, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 3) {
            {
                Entity _ent = entity;
                _ent.setYRot((float) (entity.getYRot() - 90));
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
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.5, (entity.getLookAngle().z)));
            entity.getPersistentData().putString("jinengjinxing", "翻滚");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚") && entity.getPersistentData().getDouble("entitytick") >= 17) {
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            suiji = Xiuzhe0101Support.randomInt(6, 10);
            entity.getPersistentData().putString("jineng", (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(suiji)))));
        }
    }

    public static void Npcskill0211(LevelAccessor world, Entity entity) {
        double suiji = 0;
        Entity linshientity = Xiuzhe0101Support.target(world, entity);
        if ((entity.getPersistentData().getString("jinengjinxing")).equals("开始")) {
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 18, 1, (false), (false)));
            if (entity instanceof LivingEntity _entity)
                _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 18, 20, (false), (false)));
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("蓄力") && entity.getPersistentData().getDouble("entitytick") >= 2) {
            entity.setDeltaMovement(new Vec3((entity.getLookAngle().x), 0.5, (entity.getLookAngle().z)));
            entity.getPersistentData().putString("jinengjinxing", "翻滚");
        } else if ((entity.getPersistentData().getString("jinengjinxing")).equals("翻滚") && entity.getPersistentData().getDouble("entitytick") >= 18) {
            entity.getPersistentData().putString("jinengjinxing", "");
            entity.getPersistentData().putDouble("entitytick", 0);
            suiji = Xiuzhe0101Support.randomInt(11, 15);
            entity.getPersistentData().putString("jineng", (entity.getPersistentData().getString(("skillxvanze" + Xiuzhe0101Support.whole(suiji)))));
        }
    }
}
