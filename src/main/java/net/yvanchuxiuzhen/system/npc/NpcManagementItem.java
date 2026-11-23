package net.yvanchuxiuzhen.system.npc;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 天道NPC设置工具的物品类型，编辑配置保存在当前手持工具中。 */
public final class NpcManagementItem extends ContentItem {
    public NpcManagementItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SeamlessMenuProvider(
                    (id, inventory, opener) -> new NpcManagementMenu(id, inventory, hand, NpcManagementMenu.readEquipment(opener, hand)),
                    Component.translatable("menu.yvanchuxiuzhen.tiandao_np_cset")),
                    buffer -> buffer.writeByte(hand.ordinal()));
        }
        return result;
    }
}
