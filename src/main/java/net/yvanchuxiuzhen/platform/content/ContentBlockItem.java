package net.yvanchuxiuzhen.platform.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** 内容表中的方块物品；提示文本来自来源方块类的 appendHoverText。 */
public class ContentBlockItem extends BlockItem {
    private final List<Component> tooltip;

    public ContentBlockItem(Block block, Item.Properties properties, List<Component> tooltip) {
        super(block, properties);
        this.tooltip = tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.addAll(tooltip);
    }
}
