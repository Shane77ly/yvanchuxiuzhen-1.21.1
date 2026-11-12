package net.yvanchuxiuzhen.system.xiulian.tribulation;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 雷劫动画实体的共用生命周期：来源模型与属性、默认动画、持久存在及药水、跌落、仙人掌、溺水免疫。 */
public abstract class TribulationEntity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final EntityModelRow model;
    private final int transition;
    private final int deathTicks;

    protected TribulationEntity(EntityType<? extends TribulationEntity> type, Level level, EntityModelRow model, int transition, int deathTicks) {
        super(type, level);
        this.model = model;
        this.transition = transition;
        this.deathTicks = deathTicks;
        xpReward = 0;
        setNoAi(model.noAi());
        setPersistenceRequired();
    }

    @Override
    public final String texture() { return model.texture(); }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public final void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) {
            tickTribulation();
        }
    }

    protected abstract void tickTribulation();

    @Override
    protected void tickDeath() {
        if (++deathTime == deathTicks) {
            remove(RemovalReason.KILLED);
        }
    }

    protected AnimationController<TribulationEntity> movementController() {
        return new AnimationController<>(this, "main", transition,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop(model.idle())));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(movementController());
    }

    @Override
    public final AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
