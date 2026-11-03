package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 家族令牌（来源 {@code Ling10}，出身“世家纨绔”的初始物品）：右键只显示两行说明。 */
public final class ClanTokenItem extends ContentItem {
    public ClanTokenItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.literal("家族令牌"), false);
            player.displayClientMessage(Component.literal("凭借此令牌可以去家族开设的店铺内领取补给。"), false);
        }
        return super.use(level, player, hand);
    }
}
