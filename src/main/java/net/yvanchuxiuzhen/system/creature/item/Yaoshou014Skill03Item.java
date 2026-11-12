package net.yvanchuxiuzhen.system.creature.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou014skill03Entity;

/** 014号飞羽物品：保留来源的弓使用动作、发射参数与耐久消耗。 */
public final class Yaoshou014Skill03Item extends ContentItem {
    public Yaoshou014Skill03Item(Item.Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (level.isClientSide() || !(user instanceof ServerPlayer player)) return;
        Yaoshou014skill03Entity arrow = CreatureSetup.<Yaoshou014skill03Entity>entityType("projectile_yaoshou_014skill_03")
                .get().create(level);
        arrow.setOwner(player);
        arrow.configure(5, 0);
        arrow.setCritArrow(true);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 2, 0);
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1,
                1 / (player.getRandom().nextFloat() * 0.5F + 1) + 0.5F);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
    }
}
