package net.yvanchuxiuzhen.system.creature.entity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 桃木精来源逐刻技能与受击流程；原生主手物品以CUSTOM_DATA承载阶段，按来源写回数量一的副本。 */
final class TaomujingBehavior {
    private TaomujingBehavior() { }

    static void tick(TaomujingEntity entity) {
        LevelAccessor world = entity.level();
        Entity linshientity = null;
        ItemStack mainhand = ItemStack.EMPTY;
        ItemStack linshiitem = ItemStack.EMPTY;
        CompoundTag state = new CompoundTag();
        double jvli1 = 0;
        double suiji = 0;
        if (entity.isAlive()) {
            if (entity.getMainHandItem().copy().getItem() == (ItemStack.EMPTY).getItem()) {
                {
                    final ItemStack _setstack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_201")));
                    _setstack.setCount(1);
                    entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
                }
            }
            linshiitem = entity.getMainHandItem().copy();
        state = linshiitem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            state.putDouble("entitytick", (state.getDouble("entitytick") + 1));
            if (!(state.getString("gongjizhe")).equals(state.getString("linshigongjizhe"))) {
                state.putString("gongjizhe", (state.getString("linshigongjizhe")));
            }
            if (!(state.getString("gongjizhe")).equals("")) {
                linshientity = find(entity, state.getString("gongjizhe"));
            }
            if (!(linshientity == null)) {
                if ((state.getString("jineng")).equals("")) {
                    if ((state.getString("yvdingjineng")).equals("")) {
                        jvli1 = Math.sqrt(Math.pow(entity.getX() - linshientity.getX(), 2) + Math.pow(entity.getY() - linshientity.getY(), 2) + Math.pow(entity.getZ() - linshientity.getZ(), 2));
                        entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY() + linshientity.getBbHeight() / 2), (linshientity.getZ())));
                        if (jvli1 <= 3) {
                            suiji = Mth.nextInt(RandomSource.create(), 1, 100);
                            if (suiji >= 50) {
                                state.putString("yvdingjineng", " 王八拳");
                                state.putDouble("yvdingjinengjishi", 5);
                            } else {
                                state.putString("yvdingjineng", "大回环");
                                state.putDouble("yvdingjinengjishi", 5);
                            }
                        } else {
                            state.putString("yvdingjineng", "钻地");
                            state.putDouble("yvdingjinengjishi", 5);
                        }
                    } else {
                        if (state.getDouble("entitytick") >= state.getDouble("yvdingjinengjishi")) {
                            state.putString("jineng", (state.getString("yvdingjineng")));
                            state.putString("yvdingjineng", "");
                            state.putDouble("entitytick", 0);
                        }
                    }
                } else {
                    if ((state.getString("jineng")).equals("钻地")) {
                        if ((state.getString("jinengjinxing")).equals("")) {
                            if (entity instanceof TaomujingEntity) {
                                ((TaomujingEntity) entity).setAnimation("animation.taoshujing.skill01");
                            }
                            entity.setShiftKeyDown((true));
                            state.putString("jinengjinxing", "潜入");
                        } else if ((state.getString("jinengjinxing")).equals("潜入") && state.getDouble("entitytick") >= 20) {
                            {
                                Entity _ent = entity;
                                _ent.teleportTo((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()));
                                if (_ent instanceof ServerPlayer _serverPlayer)
                                    _serverPlayer.connection.teleport((linshientity.getX()), (linshientity.getY()), (linshientity.getZ()), _ent.getYRot(), _ent.getXRot());
                            }
                            if (entity instanceof TaomujingEntity) {
                                ((TaomujingEntity) entity).setAnimation("animation.taoshujing.skill02");
                            }
                            entity.setShiftKeyDown((false));
                            state.putString("jinengjinxing", "钻出");
                        } else if ((state.getString("jinengjinxing")).equals("钻出") && state.getDouble("entitytick") >= 40) {
                            {
                                final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                        .toList();
                                for (Entity entityiterator : _entfound) {
                                    if (!(entity == entityiterator)) {
                                        if (!Relations.isFriendly(entity, entityiterator)) {
                                            if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                                entityiterator.setDeltaMovement(new Vec3(0, 1, 0));
                                            }
                                            if (world instanceof ServerLevel _level)
                                                _level.sendParticles(Particles.type("smallexplosiondarkgreen"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2),
                                                        (entityiterator.getZ()), 1, 0, 0, 0, 0);
                                            {
                                                Entity _ent = entityiterator;
                                                if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                                    _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                                            ("shanghai" + " " + "木" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1"));
                                            }
                                        }
                                    }
                                }
                            }
                            state.putString("jinengjinxing", "结束");
                        } else if ((state.getString("jinengjinxing")).equals("结束") && state.getDouble("entitytick") >= 48) {
                            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
                            state.putString("jineng", "");
                            state.putString("jinengjinxing", "");
                            state.putDouble("entitytick", 0);
                        }
                    } else if ((state.getString("jineng")).equals("大回环")) {
                        if ((state.getString("jinengjinxing")).equals("")) {
                            if (entity instanceof TaomujingEntity) {
                                ((TaomujingEntity) entity).setAnimation("animation.taoshujing.skill04");
                            }
                            state.putString("jinengjinxing", "起手式");
                        } else if ((state.getString("jinengjinxing")).equals("起手式") && state.getDouble("entitytick") >= 20) {
                            state.putDouble("entitytick2", (state.getDouble("entitytick2") + 1));
                            if (state.getDouble("entitytick2") >= 4) {
                                {
                                    final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                            .toList();
                                    for (Entity entityiterator : _entfound) {
                                        if (!(entity == entityiterator)) {
                                            if (!Relations.isFriendly(entity, entityiterator)) {
                                                if (!(entityiterator instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                                                    entityiterator.setDeltaMovement(new Vec3(((entityiterator.getX() - entity.getX()) * 0.5), 0.3, ((entityiterator.getZ() - entity.getZ()) * 0.5)));
                                                }
                                                if (world instanceof ServerLevel _level)
                                                    _level.sendParticles(Particles.type("smallexplosiondarkgreen"), (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2),
                                                            (entityiterator.getZ()), 1, 0, 0, 0, 0);
                                                {
                                                    Entity _ent = entityiterator;
                                                    if (!_ent.level().isClientSide() && _ent.getServer() != null)
                                                        _ent.getServer().getCommands().performPrefixedCommand(_ent.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                                                ("shanghai" + " " + "木" + " " + entity.getStringUUID() + " " + "false" + " " + "2" + " " + "1"));
                                                }
                                            }
                                        }
                                    }
                                }
                                state.putDouble("entitytick2", 0);
                            }
                            if (state.getDouble("entitytick") >= 40) {
                                state.putString("jinengjinxing", "结束");
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
                            }
                        } else if ((state.getString("jinengjinxing")).equals("结束") && state.getDouble("entitytick") >= 48) {
                            state.putString("jineng", "");
                            state.putString("jinengjinxing", "");
                            state.putDouble("entitytick", 0);
                            state.putDouble("entitytick2", 0);
                        }
                    } else if ((state.getString("jineng")).equals(" 王八拳")) {
                        if ((state.getString("jinengjinxing")).equals("")) {
                            if (entity instanceof TaomujingEntity) {
                                ((TaomujingEntity) entity).setAnimation("animation.taoshujing.skill03");
                            }
                            state.putString("jinengjinxing", "起手式");
                        } else if ((state.getString("jinengjinxing")).equals("起手式") && state.getDouble("entitytick") >= 20) {
                            state.putDouble("entitytick2", (state.getDouble("entitytick2") + 1));
                            if (state.getDouble("entitytick2") >= 4) {
                                {
                                    final Vec3 _center = new Vec3(
                                            (entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(1.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                                                    .getX()),
                                            (entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(1.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                                                    .getY()),
                                            (entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(1.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos()
                                                    .getZ()));
                                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                            .toList();
                                    for (Entity entityiterator : _entfound) {
                                        if (!(entity == entityiterator)) {
                                            if (!Relations.isFriendly(entity, entityiterator)) {
                                                if (world instanceof ServerLevel _level)
                                                    _level.sendParticles(ParticleTypes.CRIT, (entityiterator.getX()), (entityiterator.getY() + entityiterator.getBbHeight() / 2), (entityiterator.getZ()), 10, 0, 0, 0, 0.5);
                                                entityiterator.hurt((entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity)), (float) entity.getPersistentData().getDouble("attack_Num"));
                                            }
                                        }
                                    }
                                }
                                state.putDouble("entitytick2", 0);
                            }
                            if (state.getDouble("entitytick") >= 40) {
                                state.putString("jinengjinxing", "结束");
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((linshientity.getX()), (linshientity.getY()), (linshientity.getZ())));
                            }
                        } else if ((state.getString("jinengjinxing")).equals("结束") && state.getDouble("entitytick") >= 48) {
                            state.putString("jineng", "");
                            state.putString("jinengjinxing", "");
                            state.putDouble("entitytick", 0);
                            state.putDouble("entitytick2", 0);
                        }
                    }
                }
            } else {
                if (state.getDouble("entitytick") >= 100) {
                    {
                        final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                        List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                .toList();
                        for (Entity entityiterator : _entfound) {
                            if (entityiterator instanceof Player && !Relations.isFriendly(entity, entityiterator)) {
                                state.putString("linshigongjizhe", (entityiterator.getStringUUID()));
                            }
                        }
                    }
                    if ((state.getString("linshigongjizhe")).equals("")) {
                        {
                            final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
                            List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                                    .toList();
                            for (Entity entityiterator : _entfound) {
                                if ((entityiterator instanceof LivingEntity _livEnt ? _livEnt.getType().is(EntityTypeTags.UNDEAD) : false) && !Relations.isFriendly(entity, entityiterator)) {
                                    state.putString("linshigongjizhe", (entityiterator.getStringUUID()));
                                }
                            }
                        }
                    }
                    state.putDouble("entitytick", 0);
                }
            }
            {
                linshiitem = copyWithState(linshiitem, state);
                final ItemStack _setstack = (linshiitem.copy());
                _setstack.setCount(1);
                entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
            }
        }
    }

    static void hurt(TaomujingEntity entity, Entity sourceentity) {
        if (sourceentity == null) return;
        ItemStack linshiitem = ItemStack.EMPTY;
        CompoundTag state = new CompoundTag();
        if (entity.isAlive()) {
            if (!(entity instanceof LivingEntity _livEnt ? _livEnt.hasEffect(StatusEffects.holder("skilleffect_08")) : false)) {
                linshiitem = entity.getMainHandItem().copy();
        state = linshiitem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                if (!(state.getString("jineng")).equals("")) {
                    if (entity instanceof TaomujingEntity) {
                        ((TaomujingEntity) entity).setAnimation("empty");
                    }
                    state.putString("jineng", "");
                    state.putString("jinengjinxing", "");
                    state.putDouble("entitytick", 0);
                    state.putDouble("entitytick2", 0);
                }
                if (entity instanceof TaomujingEntity) {
                    ((TaomujingEntity) entity).setAnimation("animation.taoshujing.attacked");
                }
            }
            state.putString("linshigongjizhe", (sourceentity.getStringUUID()));
            {
                linshiitem = copyWithState(linshiitem, state);
                final ItemStack _setstack = (linshiitem.copy());
                _setstack.setCount(1);
                entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
            }
        }
    }
    private static ItemStack copyWithState(ItemStack stack, CompoundTag state) {
        ItemStack result = stack.copy();
        if (!result.isEmpty()) result.set(DataComponents.CUSTOM_DATA, CustomData.of(state));
        return result;
    }

    private static Entity find(TaomujingEntity entity, String id) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try {
            return level.getEntity(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
