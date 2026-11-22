package net.yvanchuxiuzhen.collection.festival;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 三类抛出烟花共用来源几何；引燃状态和燃放刻数保存在各实体自身的持久数据。 */
public final class ThrownFirework extends PathfinderMob implements GeoEntity, AnimatedLook {
    public enum Kind {
        PAO_04("pao_0401", 200), PAO_05("pao_0501", 40), PAO_06("pao_0601", 160);

        private final String id;
        private final int duration;

        Kind(String id, int duration) {
            this.id = id;
            this.duration = duration;
        }

        public String id() {
            return id;
        }
    }

    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(ThrownFirework.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(ThrownFirework.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final Kind kind;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public ThrownFirework(EntityType<? extends ThrownFirework> type, Level level, EntityModelRow model, Kind kind) {
        super(type, level);
        this.model = model;
        this.kind = kind;
        xpReward = 0;
        setNoAi(model.noAi());
        entityData.set(TEXTURE, model.texture());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    public void baseTick() {
        super.baseTick();
        CompoundTag data = getPersistentData();
        double tick = data.getDouble("paotick") + 1;
        if (!data.getBoolean("yinran")) {
            data.putDouble("paotick", tick >= 20 ? 0 : tick);
            if (tick >= 20) {
                if (kind == Kind.PAO_06) setAnimation("animation.pao0401.luoxva");
                data.putBoolean("yinran", true);
            }
        } else {
            if (level() instanceof ServerLevel server) {
                double spread = kind == Kind.PAO_06 ? 1.2 : 0.3;
                server.sendParticles(Particles.type("pao_0201"), getX(), getY(), getZ(), 5, spread, 0, spread, 0);
            }
            data.putDouble("paotick", tick);
            if (tick >= kind.duration) {
                if (kind != Kind.PAO_06) burst();
                data.putDouble("paotick", 0);
                if (!level().isClientSide()) discard();
            }
            if (kind == Kind.PAO_05) setDeltaMovement(new Vec3(0, (40 - data.getDouble("paotick")) * 0.02, 0));
            if (kind == Kind.PAO_06) setDeltaMovement(new Vec3(0, 0.06, 0));
        }
        refreshDimensions();
    }

    private void burst() {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao0801"));
        float volume = (float) (1 + Mth.nextDouble(RandomSource.create(), 0, 1));
        float pitch = (float) (kind == Kind.PAO_04 ? 1 + Mth.nextDouble(RandomSource.create(), 0, 1)
                : Mth.nextDouble(RandomSource.create(), 0.5, 1.5));
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), sound, SoundSource.NEUTRAL, volume, pitch);
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 5, 0, 0, 0, 0.2);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        } else {
            level().playLocalSound(getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, volume, pitch, false);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (kind == Kind.PAO_04 && source.getDirectEntity() instanceof AbstractArrow) return false;
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud) return false;
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.WITHER)
                || source.getMsgId().equals("witherSkull")) return false;
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
    protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) remove(RemovalReason.KILLED);
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ThrownFirework>(this, "movement", model.lifecycle().animationTransitionTicks(), state ->
                procedureAnimation.equals("empty") ? state.setAndContinue(RawAnimation.begin().thenLoop(model.idle())) : PlayState.STOP));
        controllers.add(new AnimationController<ThrownFirework>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
            AnimationController<ThrownFirework> controller = state.getController();
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

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
