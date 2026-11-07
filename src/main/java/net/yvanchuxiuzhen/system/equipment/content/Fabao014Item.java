package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.projectile.Fabao014;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 明光镜的蓄力释放入口；来源只在松开右键时检查沉默与灵力并扣除一次施放成本。 */
public final class Fabao014Item extends ContentItem {
    public Fabao014Item(Properties properties, List<Component> tooltip) {
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
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !canUse(player)) {
            return;
        }
        Cultivation.add(player, CultivationValue.POWER, -PlayerAttributes.value(player, Stat.MANA_ATT));
        Fabao014 projectile = Fabao014.shoot(player.serverLevel(), player);
        projectile.pickup = AbstractArrow.Pickup.DISALLOWED;
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putBoolean("sneak", player.isShiftKeyDown());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        player.getCooldowns().addCooldown(this, player.isShiftKeyDown() ? 300 : 80);
    }

    private static boolean canUse(ServerPlayer player) {
        return !StatusEffects.has(player, "effect_002")
                && Cultivation.get(player, CultivationValue.POWER) >= PlayerAttributes.value(player, Stat.MANA_ATT);
    }
}
