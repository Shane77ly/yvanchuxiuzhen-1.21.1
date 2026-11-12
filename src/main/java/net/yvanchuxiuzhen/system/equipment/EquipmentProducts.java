package net.yvanchuxiuzhen.system.equipment;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct.Enchantment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct.Materials;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 宝箱成品的实例构造（来源 BaoxiangChengpin），不承担容器、掉落或正式炼器炉次。 */
public final class EquipmentProducts {
    private EquipmentProducts() {
    }

    public static ItemStack create(HolderLookup.Provider registries, List<String> arguments) {
        ResourceLocation id = ResourceLocation.tryParse(text(arguments, 0));
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) return ItemStack.EMPTY;
        String type = text(arguments, 3);
        boolean weapon = type.equals("武器") || type.equals("法宝");
        EquipmentSlotGroup slot = switch (type) {
            case "武器", "法宝" -> EquipmentSlotGroup.MAINHAND;
            case "头盔" -> EquipmentSlotGroup.HEAD;
            case "胸甲" -> EquipmentSlotGroup.CHEST;
            case "裤子" -> EquipmentSlotGroup.LEGS;
            case "战靴" -> EquipmentSlotGroup.FEET;
            default -> null;
        };
        double a = number(arguments, 6), b = number(arguments, 7), c = number(arguments, 8);
        double d = number(arguments, 9), e = number(arguments, 10), f = number(arguments, 11);
        // 来源未知类型只在需要写入原生修饰符时产生无效槽名，导致临时物品命令失败。
        if (slot == null && (a > 0 || d > 0 || e > 0 || f > 0)) return ItemStack.EMPTY;
        EquipmentStats stats;
        Materials materials;
        if (weapon) {
            stats = new EquipmentStats(0, 0, 0, 0, 0, 0, f, d, e, (int) a, (int) b, (int) c, 0);
            materials = new Materials((int) (a * 100), (int) (b * 100), (int) (c * 100), 0, 0, 0, 0, 0, 0, 0,
                    f * 2000, d * 2000, e * 2000);
        } else {
            double physical = number(arguments, 12), spell = number(arguments, 13), soul = number(arguments, 14);
            stats = new EquipmentStats((int) a, (int) b, (int) c, (int) physical, (int) spell, (int) soul, f, 0, e, 0, 0, 0, (int) d);
            materials = new Materials(0, 0, 0, (int) (a * 20), (int) (b * 5), (int) (c * 5), (int) (d * 5),
                    (int) (physical * 100), (int) (spell * 100), (int) (soul * 100), f * 2000, 0, e * 2000);
        }
        ItemStack stack = new ItemStack(item);
        ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.builder();
        if (weapon) {
            add(modifiers, Attributes.ATTACK_SPEED, "attack_speed_base", -2.6, AttributeModifier.Operation.ADD_VALUE, slot);
            if (d != 0) add(modifiers, Attributes.ATTACK_SPEED, "attack_speed", d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, slot);
            if (e != 0) add(modifiers, Attributes.MOVEMENT_SPEED, "move_speed", e, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, slot);
        } else {
            if (d > 0) add(modifiers, Attributes.ARMOR, "armor", d, AttributeModifier.Operation.ADD_VALUE, slot);
            if (e > 0) add(modifiers, Attributes.MOVEMENT_SPEED, "move_speed", e, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, slot);
        }
        if (f > 0) add(modifiers, Attributes.KNOCKBACK_RESISTANCE, "knockback", f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, slot);
        // Q17：生命和物攻由统一最终值写入原版基础值，不再叠加成品的同项修饰符。
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers.build());
        double count = number(arguments, weapon ? 13 : 16);
        List<Enchantment> enchantments = new ArrayList<>();
        int start = weapon ? 14 : 17;
        if (count == 3) enchantments.add(enchantment(arguments, start, 2));
        if (count >= 2) enchantments.add(enchantment(arguments, start, 1));
        if (count >= 1) enchantments.add(enchantment(arguments, start, 0));
        for (Enchantment enchantment : enchantments) {
            ResourceLocation enchantmentId = enchantment.id();
            if (enchantmentId.getNamespace().equals("yvanchuxitong")) {
                enchantmentId = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, enchantmentId.getPath());
            }
            stack.enchant(registries.lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, enchantmentId)), enchantment.level());
        }
        EquipmentProduct product = new EquipmentProduct(text(arguments, 0), text(arguments, 1), text(arguments, 2), type,
                text(arguments, 4), (int) number(arguments, 5), (int) (number(arguments, 5) * 100),
                (int) number(arguments, weapon ? 12 : 15), materials, count >= 1 ? (int) count : 0, enchantments);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(product.name()));
        stack.set(EquipmentData.STATS.get(), stats);
        stack.set(EquipmentData.PRODUCT.get(), product);
        return stack;
    }

    private static Enchantment enchantment(List<String> arguments, int start, int slot) {
        int index = start + slot * 3;
        return new Enchantment(slot, ResourceLocation.fromNamespaceAndPath(text(arguments, index), text(arguments, index + 1)),
                (int) number(arguments, index + 2));
    }

    private static void add(ItemAttributeModifiers.Builder modifiers, Holder<Attribute> attribute, String name, double value,
                            AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        modifiers.add(attribute, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "equipment/product/" + slot.getSerializedName() + "/" + name), value, operation), slot);
    }

    private static String text(List<String> arguments, int index) {
        return index < arguments.size() ? arguments.get(index) : "";
    }

    /** MCreator 的数值块对缺失和非数值参数取 0。 */
    private static double number(List<String> arguments, int index) {
        try {
            return Double.parseDouble(text(arguments, index));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
