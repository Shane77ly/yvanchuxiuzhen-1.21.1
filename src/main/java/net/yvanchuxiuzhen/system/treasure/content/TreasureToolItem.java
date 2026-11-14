package net.yvanchuxiuzhen.system.treasure.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.treasure.TreasureToolSettings;

/** 左键挥动空配置或潜行工具打开设置页；右键蓄放在服务端发出设置弹体。 */
public final class TreasureToolItem extends ContentItem {
    public TreasureToolItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        boolean result = super.onEntitySwing(stack, entity, hand);
        if (entity instanceof ServerPlayer player && (entity.isShiftKeyDown()
                || TreasureToolSettings.data(stack).getString("TreasureTypeBig").isEmpty())) {
            TreasureMenus.open(player);
        }
        return result;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof ServerPlayer player && !entity.isShiftKeyDown()
                && !TreasureToolSettings.data(stack).getString("TreasureTypeBig").isEmpty()) {
            TreasureToolProjectile.shoot(player);
            stack.hurtAndBreak(1, entity, entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
    }
}
