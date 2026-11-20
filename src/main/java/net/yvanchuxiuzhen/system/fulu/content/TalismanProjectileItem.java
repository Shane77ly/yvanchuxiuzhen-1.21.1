package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.fulu.FuluSetup;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;

/** 七种符箓特效物品的独立使用入口，松手后发射对应的符箓弹体。 */
public final class TalismanProjectileItem extends ContentItem {
    public TalismanProjectileItem(Properties properties, List<Component> tooltip) {
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        String id = BuiltInRegistries.ITEM.getKey(this).getPath();
        Kind kind = switch (id) {
            case "fulutexiao_05" -> Kind.QI_SWORD;
            case "fulutexiao_17" -> Kind.METEOR;
            case "fulutexiao_19" -> Kind.SWORD;
            case "fulutexiao_29" -> Kind.FROST;
            case "fulutexiao_32" -> Kind.CLUSTER;
            case "fulutexiao_45" -> Kind.CANNON;
            case "fulutexiao_46" -> Kind.FIREBALL;
            default -> throw new IllegalStateException("未知符箓弹体物品：" + id);
        };
        TalismanProjectile projectile = FuluSetup.projectile(kind).get().create(player.serverLevel())
                .launchedWithKnockback(kind == Kind.METEOR ? 1 : 0);
        projectile.setTalismanPierce(0);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
        projectile.setSilent(true);
        projectile.setCritArrow(false);
        projectile.setBaseDamage(0.1);
        Vec3 direction = player.getViewVector(1);
        projectile.shoot(direction.x, direction.y, direction.z, 2, 0);
        level.addFreshEntity(projectile);
        // 来源声音标识为空，但音高计算仍消费一次随机数。
        level.random.nextFloat();
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        projectile.pickup = AbstractArrow.Pickup.DISALLOWED;
    }
}
