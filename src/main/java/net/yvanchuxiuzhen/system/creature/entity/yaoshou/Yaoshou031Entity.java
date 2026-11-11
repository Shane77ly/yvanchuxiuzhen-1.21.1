package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 031号妖兽的独立实体行为与来源生命周期。 */
public final class Yaoshou031Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou031Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        return YaoshouEntity.attributes(row).add(NeoForgeMod.SWIM_SPEED);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof FloatGoal);
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (YaoshouShared.initialize(this, 6,
                "平庸", "勉强", "平庸", "勉强", "贫弱", "贫弱", "勉强", "绝佳", "强劲", "强劲", "贫弱", "贫弱", "贫弱")) {
            getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(getPersistentData().getDouble("speed_number") * 6.5);
        }
    }

    @Override
    protected void sourceTick() {
        Yaoshou031Behavior.tick(this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou031Behavior.hurt(this, source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.DROWN)) {
            Yaoshou031Behavior.hurt(this, source);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    public static boolean canSpawn(EntityType<? extends YaoshouEntity> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).is(Blocks.WATER);
    }

    public static Heightmap.Types heightmap() {
        return Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(sound("entity.turtle.swim"), 0.15F, 1.0F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return sound("entity.turtle.ambient_land");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return sound("entity.turtle.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return sound("entity.turtle.death");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou031.", state -> {
            if (!procedureAnimation().equals("empty")) return PlayState.STOP;
            if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                    && !isAggressive()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou031.walk"));
            }
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou031.die", Animation.LoopType.PLAY_ONCE));
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou031.sneak"));
            if (isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou031.attacking"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou031.idle"));
        });
    }

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
    }
}
