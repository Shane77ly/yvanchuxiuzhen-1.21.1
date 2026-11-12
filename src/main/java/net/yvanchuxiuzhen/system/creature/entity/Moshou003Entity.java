package net.yvanchuxiuzhen.system.creature.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** 003 号魔兽；来源 Monster，不带浮游目标，死亡动画名为 death。 */
public final class Moshou003Entity extends MoshouEntity {
    public Moshou003Entity(EntityType<? extends MoshouEntity> type, Level level) { super(type, level, "moshou003", 20); }
    @Override protected double meleeSpeed() { return 1.6; }
    @Override protected boolean hasFloatGoal() { return false; }
    @Override protected void initializeSourceData() {
        CreatureSourceStats.initialize(this, 7, false,
                "强劲", "平庸", "平庸", "勉强", "贫弱", "贫弱", "勉强", "强劲", "勉强", "强劲", "贫弱", "平庸", "贫弱");
    }
}
