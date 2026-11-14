package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 空笼、兔笼和鸡笼共用来源的按面放置与动物重新生成入口。 */
public final class RabbitCageItem extends ContentItem {
    public RabbitCageItem(Item.Properties properties, List<Component> tooltip) { super(properties, tooltip); }

    @Override public InteractionResult useOn(UseOnContext context) {
        RabbitCages.place(context);
        return InteractionResult.SUCCESS;
    }

    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.yvanchuxiuzhen.decor.cage.place"));
        lines.add(Component.translatable("tooltip.yvanchuxiuzhen.decor.cage.vegetable"));
        lines.add(Component.translatable("tooltip.yvanchuxiuzhen.decor.cage.seed"));
        lines.add(Component.translatable("tooltip.yvanchuxiuzhen.decor.cage.recover"));
    }
}
