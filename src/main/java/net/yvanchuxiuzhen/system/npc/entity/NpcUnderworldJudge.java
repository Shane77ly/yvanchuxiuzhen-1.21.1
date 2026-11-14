package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;

/** 阎王的静止实体生命周期及首次生成的位置和朝向初始化。 */
public final class NpcUnderworldJudge extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(NpcUnderworldJudge.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(NpcUnderworldJudge.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final NpcUnderworldAnimations<NpcUnderworldJudge> animations;

    public NpcUnderworldJudge(EntityType<? extends NpcUnderworldJudge> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        animations = new NpcUnderworldAnimations<>(this, model);
        entityData.set(TEXTURE, model.texture());
        xpReward = 0;
        setPersistenceRequired();
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "yanwang");
        builder.define(ANIMATION, "undefined");
    }

    @Override protected void registerGoals() { super.registerGoals(); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        setYRot(0);
        setXRot(0);
        setYBodyRot(0);
        setYHeadRot(0);
        yRotO = 0;
        xRotO = 0;
        yBodyRotO = 0;
        yHeadRotO = 0;
        teleportTo(getX() + .5, getY(), getZ());
        return result;
    }

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

    @Override public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public String getSyncedAnimation() { return entityData.get(ANIMATION); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { animations.register(controllers); }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations.cache(); }
}
