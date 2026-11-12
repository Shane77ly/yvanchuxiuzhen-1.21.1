package net.yvanchuxiuzhen.system.creature.item;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou013taoziwuqiEntity;

/** 013号投桃武器物品：保留来源的持续使用、服务端发射与弹药消耗。 */
public final class Yaoshou013TaoziwuqiItem extends ContentItem {
    public Yaoshou013TaoziwuqiItem(Item.Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (level.isClientSide() || !(user instanceof ServerPlayer player)) return;
        ItemStack ammunition = player.getInventory().offhand.stream().filter(candidate -> candidate.is(this)).findFirst().orElse(ItemStack.EMPTY);
        if (ammunition.isEmpty()) {
            ammunition = player.getInventory().items.stream().filter(candidate -> candidate.is(this)).findFirst().orElse(ItemStack.EMPTY);
        }
        if (!player.getAbilities().instabuild && ammunition.isEmpty()) return;
        Yaoshou013taoziwuqiEntity arrow = CreatureSetup.<Yaoshou013taoziwuqiEntity>entityType("projectile_yaoshou_013taoziwuqi")
                .get().create(level);
        arrow.setOwner(player);
        arrow.setBaseDamage(1);
        arrow.setSilent(true);
        arrow.setCritArrow(false);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 2, 0);
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1,
                1 / (player.getRandom().nextFloat() * 0.5F + 1) + 0.5F);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        if (player.getAbilities().instabuild) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else if (!ammunition.isDamageableItem()) {
            ammunition.shrink(1);
        }
    }
}
