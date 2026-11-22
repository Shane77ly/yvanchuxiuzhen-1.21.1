package net.yvanchuxiuzhen.system.zhenfa.content;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationArrow;

/** 来源阳一弹体物品：按住使用，释放时发射，不消耗堆栈。 */
public final class FormationArrowItem extends Item {
    public FormationArrowItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level instanceof ServerLevel server && entity instanceof ServerPlayer player) {
            FormationArrow arrow = FormationArrow.shoot(server, player);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }
}
