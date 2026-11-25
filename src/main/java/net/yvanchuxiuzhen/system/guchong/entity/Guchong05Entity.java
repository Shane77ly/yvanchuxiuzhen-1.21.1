package net.yvanchuxiuzhen.system.guchong.entity;

import java.text.DecimalFormat;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTracking;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTrackingData;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 荧光蛊仅用来源粒子显示；追踪一百刻后回到同维主人，普通个体随机飞行。 */
public final class Guchong05Entity extends PathfinderMob {
    private GuchongTrackingData tracking = GuchongTrackingData.EMPTY;
    private double entityTick;
    private double entityTick2;

    public Guchong05Entity(EntityType<? extends Guchong05Entity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public GuchongTrackingData tracking() {
        return tracking;
    }

    public void setTracking(GuchongTrackingData tracking) {
        this.tracking = tracking == null ? GuchongTrackingData.EMPTY : tracking;
    }

    public double entityTick() {
        return entityTick;
    }

    public double entityTick2() {
        return entityTick2;
    }

    public void setEntityTick(double entityTick) {
        this.entityTick = entityTick;
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.FLYING_SPEED, .3)
                .add(Attributes.MAX_HEALTH, 20).add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RandomStrollGoal(this, 1, 20) {
            @Override
            protected Vec3 getPosition() {
                return position().add((random.nextFloat() * 2 - 1) * 16, (random.nextFloat() * 2 - 1) * 16, (random.nextFloat() * 2 - 1) * 16);
            }
        });
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setNoGravity(true);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double motionY, boolean onGround, BlockState state, BlockPos pos) {
        // 来源飞行实体不累积坠落距离，也不执行方块落地回调。
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow || source.is(DamageTypes.FALL)
                || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.FALLING_ANVIL)) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag data) {
        super.addAdditionalSaveData(data);
        data.put("tracking", GuchongTrackingData.CODEC.encodeStart(NbtOps.INSTANCE, tracking).getOrThrow());
        data.putDouble("entity_tick", entityTick);
        data.putDouble("entity_tick2", entityTick2);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag data) {
        super.readAdditionalSaveData(data);
        tracking = data.contains("tracking")
                ? GuchongTrackingData.CODEC.parse(NbtOps.INSTANCE, data.get("tracking")).result().orElse(GuchongTrackingData.EMPTY)
                : GuchongTrackingData.EMPTY;
        entityTick = data.getDouble("entity_tick");
        entityTick2 = data.getDouble("entity_tick2");
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        super.mobInteract(player, hand);
        if (!level().isClientSide()) {
            ItemHandlerHelper.giveItemToPlayer(player, GuchongTracking.fromState(tracking));
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!(level() instanceof ServerLevel level)) return;
        level.sendParticles(Particles.type("guchong_05lizi"), getX(), getY(), getZ(), 1, .2, .2, .2, .1);
        if (!tracking.tracking()) return;
        Vec3 destination = new Vec3(tracking.targetX(), tracking.targetY(), tracking.targetZ());
        lookAt(EntityAnchorArgument.Anchor.EYES, destination);
        setDeltaMovement(getLookAngle().scale(.2));
        entityTick += 1;
        if (entityTick >= 100) {
            entityTick = 0;
            level.sendParticles(Particles.type("yaoshou_040skill_03"), getX(), getY(), getZ(), 8, 0, 0, 0, 1);
            for (Player player : level.players()) {
                if (player.getStringUUID().equals(tracking.ownerUuid())) {
                    ItemHandlerHelper.giveItemToPlayer(player, GuchongTracking.fromState(tracking));
                }
            }
            discard();
        }
        entityTick2 += 1;
        if (entityTick2 >= 5) {
            for (Player player : level.players()) {
                if (player.getStringUUID().equals(tracking.ownerUuid())) {
                    player.displayClientMessage(Component.literal("距离：" + new DecimalFormat("0").format(destination.distanceTo(player.position()))), true);
                }
            }
            entityTick2 = 0;
        }
    }
}
