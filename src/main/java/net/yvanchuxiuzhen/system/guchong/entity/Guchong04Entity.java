package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 004 号蛊虫：保留来源步行动画和 003 号吞噬目标分支。 */
public final class Guchong04Entity extends GuchongEntity {
    public Guchong04Entity(EntityType<? extends GuchongEntity> type, Level level, EntityModelRow model) { super(type, level, model); }

    @Override protected void registerGoals() {
        registerGroundGoals(false);
    }
    @Override protected void initializeSourceData() {
        GuchongStats.initialize(this, "平庸", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "勉强", "贫弱", "勉强", "贫弱", "贫弱", "勉强");
    }
    @Override protected void sourceTick() { GuchongBehavior.tick(this); }
}
