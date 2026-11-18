package net.yvanchuxiuzhen.system.creature.item;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou016duyeEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou026huoyanEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou028shuiqiangEntity;

/** 来源三件妖兽投射物品：开始使用后松手，以固定参数生成本系统已有弹体。 */
public final class YaoshouProjectileItem extends ContentItem {
    public YaoshouProjectileItem(Properties properties, List<Component> tooltip) {
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
        ServerLevel server = player.serverLevel();
        String itemId = BuiltInRegistries.ITEM.getKey(this).getPath();
        AbstractArrow projectile = switch (itemId) {
            case "yaoshou_016duye" -> {
                Yaoshou016duyeEntity arrow = CreatureSetup.<Yaoshou016duyeEntity>entityType("projectile_yaoshou_016duye").get().create(server);
                arrow.configure(1, 0, (byte) 0);
                yield arrow;
            }
            case "yaoshou_026huoyan" -> {
                Yaoshou026huoyanEntity arrow = CreatureSetup.<Yaoshou026huoyanEntity>entityType("projectile_yaoshou_026huoyan").get().create(server);
                arrow.configure(player, 0);
                yield arrow;
            }
            case "yaoshou_028shuiqiang" -> {
                Yaoshou028shuiqiangEntity arrow = CreatureSetup.<Yaoshou028shuiqiangEntity>entityType("projectile_yaoshou_028shuiqiang").get().create(server);
                arrow.configure(player, 0, (byte) 0);
                yield arrow;
            }
            default -> throw new IllegalStateException("未登记的妖兽投射物品 " + itemId);
        };
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
        Vec3 direction = player.getViewVector(1);
        projectile.shoot(direction.x, direction.y, direction.z, 2, 0);
        projectile.setSilent(true);
        projectile.setCritArrow(false);
        server.addFreshEntity(projectile);
        float pitch = 1 / (level.random.nextFloat() * 0.5F + 1) + 0.5F;
        // 来源仅026蓝焰火提供声音标识；016毒液与028水枪的标识为空。
        if (itemId.equals("yaoshou_026huoyan")) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1, pitch);
        }
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        projectile.pickup = AbstractArrow.Pickup.DISALLOWED;
    }
}
