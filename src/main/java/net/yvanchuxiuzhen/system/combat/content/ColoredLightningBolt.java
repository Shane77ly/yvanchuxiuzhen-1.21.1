package net.yvanchuxiuzhen.system.combat.content;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;

/** 共享落雷的颜色载体，沿用原生闪电生命周期；来源 ColorfulLightningBoltEntity，不保存到世界。 */
public final class ColoredLightningBolt extends LightningBolt {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(ColoredLightningBolt.class, EntityDataSerializers.INT);

    public ColoredLightningBolt(EntityType<? extends LightningBolt> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, -1);
    }

    public int color() {
        return entityData.get(COLOR);
    }

    public void setColor(int color) {
        entityData.set(COLOR, color);
    }
}
