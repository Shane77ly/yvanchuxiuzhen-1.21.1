package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;

/** 003 号蛊虫：保留飞行动画、跳跃目标和 FALL 免疫。 */
public final class Guchong03Entity extends GuchongEntity {
    public Guchong03Entity(EntityType<? extends GuchongEntity> type, Level level, EntityModelRow model) { super(type, level, model); }
    @Override protected boolean ignoresFallDamage() { return true; }
    @Override public SoundEvent getAmbientSound() { return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong03")); }
    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(8, new LeapAtTargetGoal(this, 1));
        targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Guchong03Entity.class, true, true) {
            @Override public boolean canUse() { return StatusEffects.has(Guchong03Entity.this, "huxiangtunshi") && super.canUse(); }
            @Override public boolean canContinueToUse() { return StatusEffects.has(Guchong03Entity.this, "huxiangtunshi") && super.canContinueToUse(); }
        });
    }
    @Override protected void initializeSourceData() {
        GuchongStats.initialize(this, "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "勉强", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "平庸");
    }
    @Override protected void sourceTick() { GuchongBehavior.tick(this); }
}
