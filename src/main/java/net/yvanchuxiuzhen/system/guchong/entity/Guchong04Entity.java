package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 004 号蛊虫：保留来源步行动画和 003 号吞噬目标分支。 */
public final class Guchong04Entity extends GuchongEntity {
    public Guchong04Entity(EntityType<? extends GuchongEntity> type, Level level, EntityModelRow model) { super(type, level, model); }
    @Override protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Guchong03Entity.class, true, true) {
            @Override public boolean canUse() { return StatusEffects.has(Guchong04Entity.this, "huxiangtunshi") && super.canUse(); }
            @Override public boolean canContinueToUse() { return StatusEffects.has(Guchong04Entity.this, "huxiangtunshi") && super.canContinueToUse(); }
        });
    }
    @Override protected void initializeSourceData() {
        GuchongStats.initialize(this, "平庸", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "勉强", "贫弱", "勉强", "贫弱", "贫弱", "勉强");
    }
    @Override protected void sourceTick() { GuchongBehavior.tick(this); }
}
