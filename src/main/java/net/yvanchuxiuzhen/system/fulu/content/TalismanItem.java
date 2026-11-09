package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.fulu.FuluUse;

/** 持续效果符（02、03、24、31 等）：右键经共享施用过程付精力并登记效果，来源 {@code Itemfulu<编号>Item.use}。 */
public final class TalismanItem extends ContentItem {
    private final int number;

    public TalismanItem(Properties properties, List<Component> tooltip, int number) {
        super(properties, tooltip);
        this.number = number;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            FuluUse.activate(serverPlayer, result.getObject(), number);
        }
        return result;
    }
}
