package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;

/** 035号妖兽的来源身份、初始化、生命周期与逐刻行为。 */
public final class Yaoshou035Entity extends YaoshouEntity {
    public Yaoshou035Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 19, "勉强", "勉强", "平庸", "勉强", "平庸", "勉强", "平庸", "勉强", "平庸", "勉强", "强劲", "强劲", "平庸");
    }

    @Override protected void sourceTick() { Yaoshou035Behavior.tick(this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou035.attacked"); }
    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }

    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(sound("yaoshou035walk"), 0.15F, 1); }
    @Override protected SoundEvent getAmbientSound() { return sound("yaoshou035idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return sound("yaoshou035attacked"); }
    @Override protected SoundEvent getDeathSound() { return sound("yaoshou035die"); }

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
    }
}
