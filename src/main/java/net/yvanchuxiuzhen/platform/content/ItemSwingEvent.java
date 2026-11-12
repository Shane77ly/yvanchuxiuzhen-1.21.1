package net.yvanchuxiuzhen.platform.content;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * 内容表中标记 {@code swing} 的工具被实体挥动时在 {@code NeoForge.EVENT_BUS} 上发出（来源物品类覆写的 {@code onEntitySwing}，
 * 客户端与服务端各自触发一次，监听者按需只处理服务端）。事件携带被挥动的物品与挥动者，监听者按物品种类决定后续玩法。
 */
public final class ItemSwingEvent extends Event {
    private final ItemStack stack;
    private final LivingEntity entity;

    public ItemSwingEvent(ItemStack stack, LivingEntity entity) {
        this.stack = stack;
        this.entity = entity;
    }

    public ItemStack stack() {
        return stack;
    }

    public LivingEntity entity() {
        return entity;
    }
}
