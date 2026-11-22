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

/** 成品符箓的服务端右键入口：按来源编号分派即时效果、催熟或持续效果登记。 */
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
