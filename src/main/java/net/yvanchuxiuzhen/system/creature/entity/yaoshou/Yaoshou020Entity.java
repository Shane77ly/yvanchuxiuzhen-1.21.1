package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;

/** 020号妖兽：同源分身、受击反击及限时召唤实体生命周期。 */
public final class Yaoshou020Entity extends YaoshouEntity {
    private final Yaoshou019020Animations sourceAnimations = new Yaoshou019020Animations();

    public Yaoshou020Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(1.0F);
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(sound("yaoshou050walk"), 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return sound("yaoshou020attacked");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return sound("yaoshou020die");
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 5,
                "强劲", "强劲", "强劲", "平庸", "平庸", "平庸", "平庸", "贫弱", "勉强", "贫弱", "勉强", "平庸", "勉强");
    }

    @Override
    protected void sourceTick() {
        Yaoshou020Behavior.tick(this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou020Behavior.hurt(this, source);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, "animation.yaoshou020.", "spring");
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    private static SoundEvent sound(String path) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
