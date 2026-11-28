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
    private String masterUuid = "";
    private String skill = "";
    private double timer1;
    private double timer2;
    private boolean stageDone;

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
        masterUuid = owner.getStringUUID();
        skill = trapping ? TRAP : CAPTURE;
        entityData.set(ANIMATION, trapping ? "animation.fabao01701.skill01" : "animation.fabao01701.attack");
    }

    public String masterUuid() { return masterUuid; }
    public String skill() { return skill; }
    public double timer1() { return timer1; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("belongToMasterUUID", masterUuid);
        tag.putString("skill", skill);
        tag.putDouble("tick1", timer1);
        tag.putDouble("tick2", timer2);
        tag.putBoolean("jieduan", stageDone);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        masterUuid = tag.getString("belongToMasterUUID");
        skill = tag.getString("skill");
        timer1 = tag.getDouble("tick1");
        timer2 = tag.getDouble("tick2");
        stageDone = tag.getBoolean("jieduan");
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
        if (skill.equals(TRAP)) {
            if (++timer2 >= 20) {
                for (Entity target : nearby(level)) {
                    if (target instanceof LivingEntity living && target.getType().is(EntityTypeTags.ARTHROPOD))
                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20));
                }
                timer2 = 0;
            }
            if (++timer1 >= 200) {
                timer1 = 0;
                discard();
            }
        } else if (skill.equals(CAPTURE)) {
            if (++timer1 >= 40) {
                timer1 = 0;
                discard();
            } else if (!stageDone && timer1 >= 36) {
                for (Entity target : nearby(level)) {
                    if (GuchongCapture.captureWild(level, target, masterUuid) == GuchongCapture.Result.OWNED)
                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY(), getZ(), 5, .5, .5, .5, .1);
                }
                stageDone = true;
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
