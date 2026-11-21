package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 003 号蛊虫：保留飞行动画、跳跃目标和 FALL 免疫。 */
public final class Guchong03Entity extends GuchongEntity {
    public Guchong03Entity(EntityType<? extends GuchongEntity> type, Level level, EntityModelRow model) { super(type, level, model); }
    @Override protected boolean ignoresFallDamage() { return true; }
    @Override public SoundEvent getAmbientSound() { return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong03")); }
    @Override protected void registerGoals() {
        registerGroundGoals(true);
    }
    @Override protected void initializeSourceData() {
        GuchongStats.initialize(this, "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "勉强", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "平庸");
    }
    @Override protected void sourceTick() { GuchongBehavior.tick(this); }
}
