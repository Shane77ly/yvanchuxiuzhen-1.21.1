package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 本 Mod 附魔的等级查询。来源附魔只是物品上的等级标记，效果由各订阅过程读取手持物或四件护甲的等级；
 * 1.21.1 版本的附魔是数据包定义（{@code data/yvanchuxiuzhen/enchantment}），按注册表键取得，等级仍直接读物品。
 */
public final class Enchants {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private Enchants() {
    }

    public static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    /** 物品上该附魔的等级，没有为 0。 */
    public static int level(LivingEntity holder, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        Holder<Enchantment> entry = holder.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(enchantment);
        return stack.getEnchantmentLevel(entry);
    }

    /** 主手物品上的等级（来源 {@code getMainHandItem}）。 */
    public static int mainHand(LivingEntity holder, ResourceKey<Enchantment> enchantment) {
        return level(holder, holder.getMainHandItem(), enchantment);
    }

    /** 指定槽位物品上的等级。 */
    public static int slot(LivingEntity holder, EquipmentSlot slot, ResourceKey<Enchantment> enchantment) {
        return level(holder, holder.getItemBySlot(slot), enchantment);
    }

    /** 头、胸、腿、脚四件护甲上的等级之和。 */
    public static int armorSum(LivingEntity holder, ResourceKey<Enchantment> enchantment) {
        int sum = 0;
        for (EquipmentSlot slot : ARMOR) {
            sum += slot(holder, slot, enchantment);
        }
        return sum;
    }

    /** 头、胸、腿、脚四件护甲各自等级的平方之和（不是等级之和的平方）。 */
    public static int armorSquares(LivingEntity holder, ResourceKey<Enchantment> enchantment) {
        int sum = 0;
        for (EquipmentSlot slot : ARMOR) {
            int level = slot(holder, slot, enchantment);
            sum += level * level;
        }
        return sum;
    }
}
