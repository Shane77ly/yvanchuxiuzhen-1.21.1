package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 011–014及016不可食种子的原生播种入口，外观、提示和基础属性来自内容表。 */
public final class VegetableSeedItem extends ContentItem {
    private final VegetableCrop crop;

    public VegetableSeedItem(Properties properties, List<Component> tooltip, VegetableCrop crop) {
        super(properties, tooltip);
        this.crop = crop;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return VegetablePlanting.plant(context, crop);
    }
}
