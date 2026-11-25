package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.Event;

/** 元婴出窍时，将正在以本体玩家为仇恨目标的生物切换为元婴投影。 */
public final class ProjectionRedirectEvent extends Event {
    private final Mob mob;
    private final String targetPlayerId;
    private final String projectionId;

    public ProjectionRedirectEvent(Mob mob, String targetPlayerId, String projectionId) {
        this.mob = mob;
        this.targetPlayerId = targetPlayerId;
        this.projectionId = projectionId;
    }

    public Mob mob() {
        return mob;
    }

    public String targetPlayerId() {
        return targetPlayerId;
    }

    public String projectionId() {
        return projectionId;
    }
}
