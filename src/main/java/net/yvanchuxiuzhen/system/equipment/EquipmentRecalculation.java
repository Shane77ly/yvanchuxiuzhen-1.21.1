package net.yvanchuxiuzhen.system.equipment;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.equipment.data.ArmorComparison;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;

/** 来源每秒护甲物品与根标记比较；多个部位变化只执行一次完整属性重算。 */
final class EquipmentRecalculation {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzxyz";

    private EquipmentRecalculation() {
    }

    static void onSecond(CultivationSecondEvent event) {
        ServerPlayer player = event.player();
        ServerLevel level = player.serverLevel();
        ItemStack[] current = {player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET)};
        ArmorComparison previous = player.getData(EquipmentData.ARMOR_COMPARISON);
        boolean changed = false;
        for (int index = 0; index < ARMOR.length; index++) {
            EquipmentSlot slot = ARMOR[index];
            ItemStack stack = current[index];
            ItemStack saved = previous.armor(slot);
            if (!ArmorComparison.marker(stack).equals(ArmorComparison.marker(saved)) || stack.getItem() != saved.getItem()) {
                previous.armor(slot, stack);
                StringBuilder marker = new StringBuilder(8);
                for (int character = 0; character < 8; character++) {
                    marker.append(ALPHABET.charAt(new Random().nextInt(25)));
                }
                ArmorComparison.mark(stack, marker.toString());
                // 来源腿部分支第二次写入的是已保存的足部槽；该引用可能仍指向实际穿戴的靴子。
                ArmorComparison.mark(previous.armor(slot == EquipmentSlot.LEGS ? EquipmentSlot.FEET : slot), marker.toString());
                changed = true;
            }
        }
        if (changed) {
            EquipmentAttributes.submit(player);
            PlayerAttributes.recalculate(level, player);
        }
    }
}
