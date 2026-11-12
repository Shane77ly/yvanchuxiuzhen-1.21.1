package net.yvanchuxiuzhen.system.equipment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.equipment.content.AnimatedFabaoItem;
import net.yvanchuxiuzhen.system.equipment.content.DemonArmorItem;
import net.yvanchuxiuzhen.system.equipment.content.EquipmentGeoArmorItem;
import software.bernie.geckolib.animatable.GeoItem;

/** 按来源玩家刻顺序消费物品实例的 geckoAnim；两侧清空请求，仅客户端更新物品单例动画槽。 */
final class EquipmentAnimations {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private EquipmentAnimations() {
    }

    static void tickItems(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (mainHand.getItem() instanceof GeoItem || offHand.getItem() instanceof GeoItem) {
            consumeHand(player, mainHand);
            consumeHand(player, offHand);
        }
    }

    static void tickArmor(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!isAnimatedArmor(stack)) continue;
            String animation = consumeRequest(stack);
            if (animation.isEmpty() || !player.level().isClientSide()) continue;
            if (stack.getItem() instanceof EquipmentGeoArmorItem armor) armor.requestAnimation(animation);
            if (stack.getItem() instanceof DemonArmorItem armor) armor.requestAnimation(animation);
        }
    }

    private static void consumeHand(Player player, ItemStack stack) {
        if (isAnimatedArmor(stack)) return;
        String animation = consumeRequest(stack);
        if (!animation.isEmpty() && player.level().isClientSide()
                && stack.getItem() instanceof AnimatedFabaoItem item) {
            item.requestAnimation(animation);
        }
    }

    private static boolean isAnimatedArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && stack.getItem() instanceof GeoItem;
    }

    private static String consumeRequest(ItemStack stack) {
        String animation = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
        if (!animation.isEmpty()) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putString("geckoAnim", ""));
        }
        return animation;
    }
}
