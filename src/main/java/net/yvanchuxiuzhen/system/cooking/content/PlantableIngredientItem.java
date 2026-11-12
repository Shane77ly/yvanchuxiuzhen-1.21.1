package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 保留食材的食用属性与提示，将方块使用委托给Plant的公开播种操作。 */
public final class PlantableIngredientItem extends ContentItem {
    private final Function<UseOnContext, InteractionResult> planting;

    public PlantableIngredientItem(Properties properties, List<Component> tooltip,
            Function<UseOnContext, InteractionResult> planting) {
        super(properties, tooltip);
        this.planting = planting;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        return planting.apply(context);
    }
}
