package net.yvanchuxiuzhen.platform.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 镐；提示文本来自语言键 {@code tooltip.<命名空间>.item.<路径>.<序号>}。 */
public class ContentPickaxe extends PickaxeItem {
    private final List<Component> tooltip;

    public ContentPickaxe(Tier tier, Item.Properties properties, List<Component> tooltip) {
        super(tier, properties);
        this.tooltip = tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.addAll(tooltip);
    }
}
