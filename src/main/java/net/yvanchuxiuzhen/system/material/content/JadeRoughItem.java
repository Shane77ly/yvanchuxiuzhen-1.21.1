package net.yvanchuxiuzhen.system.material.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.material.api.JadeCarvingEvent;

/** 璞玉使用入口，结果由跨系统雕琢流程处理。 */
public final class JadeRoughItem extends ContentItem {
    private final int number;

    public JadeRoughItem(Properties properties, List<Component> tooltip, int number) {
        super(properties, tooltip);
        this.number = number;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            NeoForge.EVENT_BUS.post(new JadeCarvingEvent(serverPlayer, result.getObject(), number));
        }
        return result;
    }
}
