package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 050飒风的飞行实体：每五刻按来源范围结算风伤并推进，二十刻后消散。 */
public final class Yaoshou050safengentityEntity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Yaoshou050safengentityEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Yaoshou050safengentityEntity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public Yaoshou050safengentityEntity(EntityType<? extends Yaoshou050safengentityEntity> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(false);
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.FLYING_SPEED, 0.3).add(NeoForgeMod.SWIM_SPEED, 0.3);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, "undefined");
        builder.define(TEXTURE, "kong");
    }

    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    @Override public String texture() { return entityData.get(TEXTURE); }
    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }
    @Override public void setNoGravity(boolean ignored) { super.setNoGravity(true); }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); setNoGravity(true); }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypes.WITHER) || source.getMsgId().equals("witherSkull")) return false;
        return super.hurt(source, amount);
    }

    @Override public void baseTick() {
        super.baseTick();
        tickWind();
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    private void tickWind() {
        getPersistentData().putDouble("entitytick", getPersistentData().getDouble("entitytick") + 1);
        if (getPersistentData().getDouble("entitytick") >= 20) {
            if (level() instanceof ServerLevel level)
                level.sendParticles(Particles.type("brokenlightgray"), getX(), getY(), getZ(), 16, 2, 2, 2, 0);
            getPersistentData().putDouble("entitytick", 0);
            if (!level().isClientSide()) discard();
        }
        getPersistentData().putDouble("entitytick2", getPersistentData().getDouble("entitytick2") + 1);
        if (getPersistentData().getDouble("entitytick2") >= 5) {
            if (level() instanceof ServerLevel level)
                level.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 16, 0.2, 3, 0.2, 0);
            Vec3 center = position();
            List<Entity> targets = level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(3, 6, 3), entity -> true)
                    .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
            for (Entity target : targets) {
                if (!Relations.isFriendly(this, target) && target != this && level() instanceof ServerLevel level) {
                    level.sendParticles(Particles.type("smallexplosionlightgray"), target.getX(), target.getY() + target.getBbHeight() / 2,
                            target.getZ(), 16, 2, 2, 2, 0);
                    if (target instanceof LivingEntity living)
                        SpellDamage.command(level, living, "shanghai 风 " + getStringUUID() + " false 2 1");
                }
            }
            setDeltaMovement(new Vec3(-Math.sin(getPersistentData().getDouble("jiaodu") * (Math.PI / 180)),
                    -Math.sin(getPersistentData().getDouble("headjiaodu") * (Math.PI / 180)),
                    Math.cos(getPersistentData().getDouble("jiaodu") * (Math.PI / 180))));
            getPersistentData().putDouble("entitytick2", 0);
        }
    }

    @Override protected void tickDeath() {
        if (++deathTime == 1) { remove(RemovalReason.KILLED); dropExperience(this); }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Yaoshou050safengentityEntity>(this, "movement", 4, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou050safeng.idle"));
        }));
        controllers.add(new AnimationController<Yaoshou050safengentityEntity>(this, "procedure", 4, state -> {
            AnimationController<Yaoshou050safengentityEntity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    procedureAnimation = "empty";
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
