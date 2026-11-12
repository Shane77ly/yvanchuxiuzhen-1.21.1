package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 普通作物独立种子的物品入口；提示来自内容表，播种交由Plant公开操作。 */
public final class ProduceItem extends ContentItem {
    private final ProduceCrop crop;

    public ProduceItem(Properties properties, List<Component> tooltip, ProduceCrop crop) {
        super(properties, tooltip);
        this.crop = crop;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        return ProducePlanting.useOn(context, crop);
    }
}
