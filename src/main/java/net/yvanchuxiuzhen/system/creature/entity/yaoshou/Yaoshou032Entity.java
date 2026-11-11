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
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;

/** 032号妖兽的来源身份、初始化、生命周期与逐刻行为。 */
public final class Yaoshou032Entity extends YaoshouEntity {
    public Yaoshou032Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 14, "绝佳", "平庸", "平庸", "勉强", "贫弱", "贫弱", "强劲", "绝佳", "勉强", "强劲", "贫弱", "贫弱", "平庸");
    }

    @Override protected void sourceTick() { Yaoshou032Behavior.tick(this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou032.attacked"); }
    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }

    @Override protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(1.2F);
    }

    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(sound("yaoshou032walk"), 0.15F, 1); }
    @Override protected SoundEvent getAmbientSound() { return sound("yaoshou032idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return sound("yaoshou032attacked"); }
    @Override protected SoundEvent getDeathSound() { return sound("yaoshou032die"); }

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
    }
}
