package net.yvanchuxiuzhen.system.guchong.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/** 蛊虫升级后由死者所属系统选择来源的死者属性重算分支。 */
public final class GuchongUpgradeEvent extends Event {
    private final LivingEntity victim;
    private boolean victimRecalculationSelected;

    public GuchongUpgradeEvent(LivingEntity victim) {
        this.victim = victim;
    }

    public LivingEntity victim() {
        return victim;
    }

    public boolean victimRecalculationSelected() {
        return victimRecalculationSelected;
    }

    public void selectVictimRecalculation() {
        victimRecalculationSelected = true;
    }
}
