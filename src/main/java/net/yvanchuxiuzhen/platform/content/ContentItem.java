package net.yvanchuxiuzhen.platform.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 内容表中的普通物品；提示文本来自语言键 {@code tooltip.<命名空间>.item.<路径>.<序号>}。 */
public class ContentItem extends Item {
    private final List<Component> tooltip;

    public ContentItem(Item.Properties properties, List<Component> tooltip) {
        super(properties);
        this.tooltip = tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.addAll(tooltip);
    }
}
