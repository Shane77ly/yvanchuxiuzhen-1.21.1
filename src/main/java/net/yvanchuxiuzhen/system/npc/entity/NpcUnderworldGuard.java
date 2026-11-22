package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;

/** 黑白无常、牛头与马面的反击目标、近战、环境音及死亡表现。 */
public final class NpcUnderworldGuard extends Monster implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(NpcUnderworldGuard.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(NpcUnderworldGuard.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final NpcUnderworldAnimations<NpcUnderworldGuard> animations;

    public NpcUnderworldGuard(EntityType<? extends NpcUnderworldGuard> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        animations = new NpcUnderworldAnimations<>(this, model);
        entityData.set(TEXTURE, model.texture());
        xpReward = 0;
        setPersistenceRequired();
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
            @Override protected boolean canPerformAttack(LivingEntity target) {
                return isTimeToAttack() && distanceToSqr(target) <= getBbWidth() * getBbWidth() + target.getBbWidth();
            }
        });
        goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        goalSelector.addGoal(5, new FloatGoal(this));
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }

    @Override protected SoundEvent getAmbientSound() {
        return switch (model.id()) {
            case "niutou" -> SoundEvents.COW_AMBIENT;
            case "mamian" -> SoundEvents.HORSE_AMBIENT;
            default -> super.getAmbientSound();
        };
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override public void tick() {
        String request = getSyncedAnimation();
        if (!request.equals("undefined")) {
            setAnimation("undefined");
            animations.request(request);
        }
        super.tick();
    }

    @Override public void baseTick() {
        super.baseTick();
        refreshDimensions();
    }

    @Override protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
            if (level() instanceof ServerLevel server) {
                if (model.id().equals("wuchangbai"))
                    server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.2, getZ(), 40, 0, 2, 0, .05);
                if (model.id().equals("wuchanghei"))
                    server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2, getZ(), 40, 0, 2, 0, .05);
            }
        }
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public String getSyncedAnimation() { return entityData.get(ANIMATION); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { animations.register(controllers); }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations.cache(); }
}
