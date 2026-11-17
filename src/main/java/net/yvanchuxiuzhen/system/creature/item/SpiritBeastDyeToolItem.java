package net.yvanchuxiuzhen.system.creature.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastBagDyeMenus;

/** 打开绑定于物品自身的灵兽袋染色菜单。 */
public final class SpiritBeastDyeToolItem extends ContentItem {
    public SpiritBeastDyeToolItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) SpiritBeastBagDyeMenus.open(serverPlayer, hand);
        return result;
    }
}
