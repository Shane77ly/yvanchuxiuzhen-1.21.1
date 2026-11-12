package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;

/** 030号妖兽：飞行导航、来源重力约束及金系技能流程。 */
public final class Yaoshou030Entity extends YaoshouEntity {
    private final Yaoshou030Animations sourceAnimations = new Yaoshou030Animations();

    public Yaoshou030Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof FloatGoal);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setNoGravity(true);
        updateSwingTime();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(sound("entity.parrot.fly"), 0.15F, 1.0F);
    }

    @Override
    protected void checkFallDamage(double motionY, boolean onGround, BlockState state, BlockPos position) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
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
        if (YaoshouShared.initialize(this, 7,
                "贫弱", "贫弱", "勉强", "贫弱", "勉强", "贫弱", "贫弱", "贫弱", "强劲", "贫弱", "贫弱", "贫弱", "强劲")) {
            getAttribute(Attributes.FLYING_SPEED).setBaseValue(getPersistentData().getDouble("speed_number") * 0.7);
        }
    }

    @Override
    protected void sourceTick() {
        Yaoshou030Behavior.tick(this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou030Behavior.hurt(this, source);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers);
    }

    private static SoundEvent sound(String path) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(path));
    }
}
