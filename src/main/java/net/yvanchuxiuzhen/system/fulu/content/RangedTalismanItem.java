package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.fulu.FuluRanged;

/** 需要蓄放的远程符（10、20、28、33、35 号）：右键开始使用，松开时由 {@link FuluRanged} 付精力并射出弹体，来源 {@code Itemfulu10Item.releaseUsing}。 */
public final class RangedTalismanItem extends ContentItem {
    private final int number;

    public RangedTalismanItem(Properties properties, List<Component> tooltip, int number) {
        super(properties, tooltip);
        this.number = number;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            FuluRanged.release(player, stack, number);
        }
    }
}
