package net.yvanchuxiuzhen.system.zhenfa.entity;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 阳三落实体：每刻向下运动，按来源筛选锁存写攻击标记，下方为空气时移除。 */
public final class FormationFalling extends FormationActor implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(FormationFalling.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(FormationFalling.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public FormationFalling(EntityType<? extends FormationFalling> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
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
        setDeltaMovement(0, -.4, 0);
        if (!level().isClientSide()) {
            Vec3 center = position();
            boolean allowed = false;
            for (Entity target : level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1.5)).stream()
                    .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                int mode = targetMode();
                if (mode == 1 && this != target) {
                    if (Relations.entityTeam(this).equals(Relations.entityTeam(target))) allowed = true;
                } else if (mode == 2 && this != target) {
                    if (!Relations.entityTeam(this).equals(Relations.entityTeam(target))) allowed = true;
                } else if (this != target) {
                    allowed = true;
                }
                if (allowed) {
                    setDeltaMovement(0, -.4, 0);
                    SpellDamage.markFormationFallingAttack(target, zhenLevel());
                }
            }
            if (level().isEmptyBlock(BlockPos.containing(getX(), getY() - 1, getZ()))) discard();
        }
        refreshDimensions();
    }

    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override protected void tickDeath() { if (++deathTime == 1) remove(RemovalReason.KILLED); }
    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<FormationFalling>(this, "movement", model.lifecycle().animationTransitionTicks(), state ->
                procedureAnimation.equals("empty") ? state.setAndContinue(RawAnimation.begin().thenLoop(model.idle())) : PlayState.STOP));
        controllers.add(new AnimationController<FormationFalling>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
            AnimationController<FormationFalling> controller = state.getController();
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
