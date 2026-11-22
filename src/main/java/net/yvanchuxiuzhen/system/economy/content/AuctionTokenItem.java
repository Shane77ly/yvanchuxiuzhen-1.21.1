package net.yvanchuxiuzhen.system.economy.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.economy.Auctions;

/** 拍卖令牌沿来源在顶面放置一个拍卖台和至多十个座位，不消耗令牌。 */
public final class AuctionTokenItem extends ContentItem {
    public AuctionTokenItem(Properties properties, List<Component> tooltip) { super(properties, tooltip); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer player) Auctions.place(player, context.getItemInHand(), context.getClickedPos(), context.getClickedFace());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
