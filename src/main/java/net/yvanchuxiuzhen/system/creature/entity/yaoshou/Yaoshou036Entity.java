package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;

/** 036号妖兽的来源身份、初始化、生命周期与逐刻行为。 */
public final class Yaoshou036Entity extends YaoshouEntity {
    public Yaoshou036Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 20, "强劲", "贫弱", "勉强", "贫弱", "贫弱", "贫弱", "强劲", "平庸", "勉强", "平庸", "贫弱", "贫弱", "勉强");
    }

    @Override protected void sourceTick() { Yaoshou036Behavior.tick(this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou036.attacked"); }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.DROWN)) {
            sourceHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }
    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(sound("yaoshou036walk"), 0.15F, 1); }
    @Override protected SoundEvent getAmbientSound() { return sound("yaoshou036idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return sound("yaoshou036attacked"); }
    @Override protected SoundEvent getDeathSound() { return sound("yaoshou036die"); }

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
    }
}
