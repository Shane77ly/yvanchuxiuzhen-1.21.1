package net.yvanchuxiuzhen.system.storage.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 材料袋只提供玩家材料仓的开窗入口，库存不属于这件物品。 */
public final class MaterialBagItem extends ContentItem {
    public MaterialBagItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos pos = BlockPos.containing(player.getX(), player.getY() + 1, player.getZ());
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new MaterialBagMenu(id, inventory, pos),
                    Component.literal("Cailiaodai Gui")), pos);
        }
        return result;
    }
}
