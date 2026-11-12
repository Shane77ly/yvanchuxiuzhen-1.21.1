package net.yvanchuxiuzhen.system.creature.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou023BaodanEntity;

/** 023号彩羽轰炸弹物品：按来源开始使用并在服务端松手时发射弹体。 */
public final class Yaoshou023BaodanItem extends ContentItem {
    public Yaoshou023BaodanItem(Properties properties, List<Component> tooltip) {
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
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;
        Yaoshou023BaodanEntity projectile = new Yaoshou023BaodanEntity(
                CreatureSetup.<Yaoshou023BaodanEntity>entityType("projectile_yaoshou_023baodan").get(), level);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1F, player.getZ());
        projectile.configure(1, 0);
        projectile.shoot(player.getViewVector(1).x, player.getViewVector(1).y, player.getViewVector(1).z, 0.2F, 0);
        level.addFreshEntity(projectile);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        projectile.pickup = AbstractArrow.Pickup.DISALLOWED;
    }
}
