package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** 拍卖座椅事件使用的来源实体椅子。 */
public final class DecorSeat extends DecorSeatEntity {
    public DecorSeat(EntityType<? extends DecorSeat> type, Level level) { super(type, level); }
}
