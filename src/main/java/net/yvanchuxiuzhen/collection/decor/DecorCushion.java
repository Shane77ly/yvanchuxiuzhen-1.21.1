package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 蒲团载具保持来源禁止推动和降低0.1格的乘客位置。 */
public final class DecorCushion extends DecorSeatEntity {
    public DecorCushion(EntityType<? extends DecorCushion> type, Level level) { super(type, level); }

    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) { }
    @Override protected void pushEntities() { }
    @Override protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        return super.getPassengerAttachmentPoint(passenger, dimensions, partialTick).add(0, -.1, 0);
    }
}
