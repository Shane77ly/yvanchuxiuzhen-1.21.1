package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** Pao13 的按住右键/松手发射入口，弹药优先取副手、主手再取背包。 */
public final class Pao13Item extends ContentItem {
    public Pao13Item(Properties properties, List<Component> tooltip) { super(properties, tooltip); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }
    @Override public void releaseUsing(ItemStack held, Level level, LivingEntity entity, int timeLeft) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel server) || !(entity instanceof ServerPlayer player)) return;
        ItemStack ammunition = ProjectileWeaponItem.getHeldProjectile(player, stack -> stack.is(this));
        if (ammunition.isEmpty()) for (ItemStack stack : player.getInventory().items) if (stack.is(this)) { ammunition = stack; break; }
        if (!player.getAbilities().instabuild && ammunition.isEmpty()) return;
        Pao13Entity projectile = Pao13Entity.shoot(server, player);
        if (player.getAbilities().instabuild) projectile.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY;
        else ammunition.shrink(1);
    }
}
