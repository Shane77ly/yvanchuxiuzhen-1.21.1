package net.yvanchuxiuzhen.system.equipment.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.guchong.api.GuchongCapture;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 摄虫网实体持有法宝时序；捕获目标的个体数据和交付由蛊虫公开操作维护。 */
public final class Fabao01701Entity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final String CAPTURE = "摄虫网";
    private static final String TRAP = "困虫兜";
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Fabao01701Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedure = "";

    public Fabao01701Entity(EntityType<Fabao01701Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        setNoAi(false);
        setPersistenceRequired();
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes();
    }

    public void configure(Player owner, boolean trapping) {
        getPersistentData().putString("belongToMasterUUID", owner.getStringUUID());
        getPersistentData().putString("skill", trapping ? TRAP : CAPTURE);
        entityData.set(ANIMATION, trapping ? "animation.fabao01701.skill01" : "animation.fabao01701.attack");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, "undefined");
    }

    @Override
    public String texture() { return model.texture(); }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return null; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.FALLING_ANVIL)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(Entity entity) { }

    @Override
    protected void pushEntities() { }

    @Override
    public void baseTick() {
        super.baseTick();
        if (level() instanceof ServerLevel server) updateNet(server);
        refreshDimensions();
        String requested = entityData.get(ANIMATION);
        if (!requested.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedure = requested;
        }
    }

    private void updateNet(ServerLevel level) {
        CompoundTag data = getPersistentData();
        if (data.getString("skill").equals(TRAP)) {
            data.putDouble("tick2", data.getDouble("tick2") + 1);
            if (data.getDouble("tick2") >= 20) {
                for (Entity target : nearby(level)) {
                    if (target instanceof LivingEntity living && target.getType().is(EntityTypeTags.ARTHROPOD))
                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20));
                }
                data.putDouble("tick2", 0);
            }
            data.putDouble("tick1", data.getDouble("tick1") + 1);
            if (data.getDouble("tick1") >= 200) {
                data.putDouble("tick1", 0);
                discard();
            }
        } else if (data.getString("skill").equals(CAPTURE)) {
            data.putDouble("tick1", data.getDouble("tick1") + 1);
            if (data.getDouble("tick1") >= 40) {
                data.putDouble("tick1", 0);
                discard();
            } else if (!data.getBoolean("jieduan") && data.getDouble("tick1") >= 36) {
                for (Entity target : nearby(level)) {
                    if (GuchongCapture.captureWild(level, target, data.getString("belongToMasterUUID")) == GuchongCapture.Result.OWNED)
                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY(), getZ(), 5, .5, .5, .5, .1);
                }
                data.putBoolean("jieduan", true);
            }
        }
    }

    private List<Entity> nearby(ServerLevel level) {
        Vec3 center = position();
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == 2) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 0, state -> {
            if (!procedure.isEmpty()) return PlayState.STOP;
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        controllers.add(new AnimationController<>(this, "procedure", 0, state -> {
            AnimationController<?> controller = state.getController();
            if (!procedure.isEmpty() && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedure, Animation.LoopType.PLAY_ONCE));
                procedure = "";
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
