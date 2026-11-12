package net.yvanchuxiuzhen.platform.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForge;

/** 多用途工具（工具行为随所属系统迁移）；提示文本来自语言键 {@code tooltip.<命名空间>.item.<路径>.<序号>}；{@code swing} 为真时挥动会发出 {@link ItemSwingEvent}。 */
public class ContentTieredItem extends TieredItem {
    private final List<Component> tooltip;
    private final boolean swing;

    public ContentTieredItem(Tier tier, Item.Properties properties, List<Component> tooltip, boolean swing) {
        super(tier, properties);
        this.tooltip = tooltip;
        this.swing = swing;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.addAll(tooltip);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (swing) {
            NeoForge.EVENT_BUS.post(new ItemSwingEvent(stack, entity));
        }
        return false;
    }
}
