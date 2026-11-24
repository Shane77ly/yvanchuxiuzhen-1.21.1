package net.yvanchuxiuzhen.system.fulu.content;

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
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.ReincarnationRequestedEvent;
import net.yvanchuxiuzhen.system.fulu.api.TalismanInstance;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 轮回符 37：确认次数随物品栈保存，前三次提醒，第四次触发轮回。 */
public final class ReincarnationTalismanItem extends ContentItem {
    public ReincarnationTalismanItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = result.getObject();
            TalismanInstance current = Fulu.talisman(stack);
            int count = current.confirmations();
            int next = count < 1 ? 1 : count == 1 ? 2 : count == 2 ? 3 : 0;
            if (next != 0) {
                serverPlayer.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.reincarnation.confirm_" + next), false);
                Fulu.setTalisman(stack, current.withConfirmations(next));
            } else {
                serverPlayer.serverLevel().sendParticles(Particles.type("cailiao_233particle"), player.getX(), player.getY() + 1,
                        player.getZ(), 40, 0, 0, 0, 1);
                Fulu.setTalisman(stack, current.withConfirmations(0));
                NeoForge.EVENT_BUS.post(new ReincarnationRequestedEvent(serverPlayer, serverPlayer.serverLevel()));
            }
        }
        return result;
    }
}
