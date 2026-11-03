package net.yvanchuxiuzhen.platform.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 盔甲部件；提示文本来自语言键 {@code tooltip.<命名空间>.item.<路径>.<序号>}。 */
public class ContentArmorItem extends ArmorItem {
    private final List<Component> tooltip;

    public ContentArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties, List<Component> tooltip) {
        super(material, type, properties);
        this.tooltip = tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.addAll(tooltip);
    }
}
