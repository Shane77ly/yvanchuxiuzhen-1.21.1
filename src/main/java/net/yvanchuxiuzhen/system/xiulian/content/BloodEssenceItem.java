package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 血精（来源 {@code XvejingItem}、{@code XvejingDangYouJianDianJiKongQiShiShiTiDeWeiZhi}）：右键回复 2 点生命并消耗 1 个。 */
public final class BloodEssenceItem extends ContentItem {
    public BloodEssenceItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY(), player.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            player.setHealth(player.getHealth() + 2);
        }
        result.getObject().shrink(1);
        return result;
    }
}
