package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.system.equipment.EquipmentWeapons;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 18 把速弓与长弓：保留剑属性与词条兼容，接入右键按住蓄力与松手/满蓄射箭流程（Q57）。 */
public final class BowWeaponItem extends ContentSword {
    public BowWeaponItem(Tier tier, Item.Properties properties, List<Component> tooltip) {
        super(tier, properties, tooltip, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || Skills.arrow(player).isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            EquipmentWeapons.setRightClicking(player, true);
        }
        return InteractionResultHolder.consume(stack);
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
        if (entity instanceof ServerPlayer player) {
            EquipmentWeapons.releaseBow(player);
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity instanceof ServerPlayer player) {
            EquipmentWeapons.cancelBow(player);
        }
    }
}
