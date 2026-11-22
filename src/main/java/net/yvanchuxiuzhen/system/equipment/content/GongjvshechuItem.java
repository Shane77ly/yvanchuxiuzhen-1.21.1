package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.equipment.projectile.KailingProjectile;

/** 隐藏开灵物品的长按松手入口；发射速度沿用来源物品的固定二倍速度。 */
public final class GongjvshechuItem extends ContentItem {
    public GongjvshechuItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
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
        if (entity instanceof ServerPlayer player) {
            KailingProjectile.shoot(player.serverLevel(), player, 2).pickup = AbstractArrow.Pickup.DISALLOWED;
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        }
    }
}
