package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.cooking.WineRuntime;

/** 八种酒共用开始饮用快照、完成结算和上表面摆坛入口。 */
public final class WineItem extends ContentItem {
    public WineItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) WineRuntime.snapshot(serverPlayer, result.getObject());
        return result;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer player) WineRuntime.drink(player);
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        WineRuntime.place(context);
        return InteractionResult.SUCCESS;
    }
}
