package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import net.yvanchuxiuzhen.system.creature.entity.SourceFollowOwnerGoal;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 023号彩羽飞禽：保留来源飞行导航、资质、攻击阶段与三个动画控制器。 */
public final class Yaoshou023Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou023Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return YaoshouEntity.attributes(model);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new SourceFollowOwnerGoal(this, 1, 10, 2, false));
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double distance, boolean grounded, BlockState state, BlockPos position) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(sound("entity.parrot.fly"), 0.15F, 1.0F);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return sound("entity.parrot.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return sound("entity.parrot.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return sound("entity.parrot.death");
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (YaoshouShared.initialize(this, 8,
                "勉强", "贫弱", "勉强", "平庸", "勉强", "贫弱", "勉强", "贫弱", "强劲", "平庸", "贫弱", "贫弱", "强劲")) {
            getAttribute(Attributes.FLYING_SPEED)
                    .setBaseValue(getPersistentData().getDouble("speed_number") * 0.7);
        }
    }

    @Override
    protected void sourceTick() {
        Yaoshou023Behavior.tick(this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou023Behavior.hurt(this, source.getEntity());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        setNoGravity(true);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou023.", state -> {
            if (!procedureAnimation().equals("empty")) return PlayState.STOP;
            if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                    && onGround() && !isAggressive()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou023.walk"));
            }
            if (isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou023.die", Animation.LoopType.PLAY_ONCE));
            }
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou023.sneak"));
            if (!onGround()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou023.fly"));
            if (isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou023.attacking"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou023.idle"));
        });
    }

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
    }
}
