package net.yvanchuxiuzhen.system.equipment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.BenmingData;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct.Enchantment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct.Materials;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties.Value;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats.Recovery;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 自然炼器结果的数值转换、原生属性与实例写入；炉次和材料消费由炼器系统负责。 */
public final class EquipmentRefining {
    private static final int[] REVERSED_EXPERIENCE = {50, 80, 96, 112, 128, 144, 160, 176, 192, 208, 224,
            256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 592, 640, 688, 736, 784, 832, 880, 928,
            976, 1024, 1088, 1152, 1216, 1280, 1344, 1408, 1472, 1536, 1600, 1664, 1728};

    private EquipmentRefining() {
    }

    public static ItemStack create(HolderLookup.Provider registries, CompoundTag recipe, CompoundTag raw, ItemStack previous) {
        CompoundTag data = raw.copy();
        for (String key : List.of("tuzhiname", "fabaoid", "fabaoshuoming", "fabaotype")) {
            data.putString(key, recipe.getString(key));
        }
        convert(data);
        Item item = BuiltInRegistries.ITEM.get(targetId(data.getString("fabaoid").toLowerCase(Locale.ENGLISH)));
        if (item == Items.AIR) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        boolean weapon = stack.is(tag("wuqi")) || stack.is(tag("fabao"));
        EquipmentSlotGroup slot = weapon ? EquipmentSlotGroup.MAINHAND : switch (data.getString("fabaotype")) {
            case "头盔" -> EquipmentSlotGroup.HEAD;
            case "胸甲" -> EquipmentSlotGroup.CHEST;
            case "裤子" -> EquipmentSlotGroup.LEGS;
            case "战靴" -> EquipmentSlotGroup.FEET;
            default -> null;
        };
        if (slot == null && (data.getInt("health_Max") > 0 || data.getDouble("speed_num") != 0
                || data.getDouble("attack_speed") != 0 || data.getInt("knockback_resistance") > 0
                || data.getInt("armor_num") > 0 || data.getInt("Armor_Toughness") > 0)) return ItemStack.EMPTY;
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers(data, weapon, slot));

        Map<String, Value> root = new LinkedHashMap<>();
        // 来源先在放置结果时写顶层等级，随后才计算嵌套等级；升级保留旧等级，首次制作通常为0。
        root.put("item_level_num", Value.integer(data.getInt("item_level_num")));
        if (!previous.isEmpty()) copyBenming(previous, stack, root);
        List<Enchantment> enchantments = enchant(registries, stack, data);
        String name = data.getString("tuzhiname");
        if (previous.isEmpty() || !name.isEmpty()) {
            name = name.replace("[", "").replace("]", "");
            if (!previous.isEmpty()) data.putString("tuzhiname", name);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }
        int level = sourceLevel(data.getInt("expnum"));
        data.putInt("item_level_num", level);
        data.putString("item_level", grade(level));
        stack.set(EquipmentData.REFINEMENT.get(), CustomData.of(data));
        stack.set(EquipmentData.PROPERTIES.get(), new EquipmentProperties(root));
        stack.set(EquipmentData.STATS.get(), stats(data));
        stack.set(EquipmentData.PRODUCT.get(), new EquipmentProduct(data.getString("fabaoid"), data.getString("tuzhiname"),
                data.getString("fabaoshuoming"), data.getString("fabaotype"), data.getString("item_level"),
                level, data.getInt("expnum"), data.getInt("weightnum"), materialProjection(data),
                data.getInt("fumozongshu"), enchantments));
        return stack;
    }

    /** 新炉次读取完整累计量，宝箱实例按已有字段重建；两种来源均执行升级前的整数反推。 */
    public static CompoundTag materials(ItemStack stack) {
        CustomData refinement = stack.get(EquipmentData.REFINEMENT.get());
        if (refinement != null) {
            CompoundTag data = refinement.copyTag();
            writeFinalStats(data, stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY));
            return prepareUpgrade(data);
        }
        EquipmentProduct product = stack.get(EquipmentData.PRODUCT.get());
        if (product == null) return new CompoundTag();
        CompoundTag data = new CompoundTag();
        data.putString("tuzhiname", product.name());
        data.putString("fabaoid", product.itemId());
        data.putString("fabaoshuoming", product.description());
        data.putString("fabaotype", product.type());
        data.putString("item_level", product.grade());
        data.putInt("item_level_num", product.level());
        data.putInt("expnum", product.experience());
        data.putInt("weightnum", product.weight());
        Materials material = product.materials();
        data.putInt("attnum", (int) material.physicalAttack());
        data.putInt("mananum", (int) material.spellAttack());
        data.putInt("soulnum", (int) material.soulAttack());
        data.putInt("heanum", (int) material.health());
        data.putInt("powernum", (int) material.power());
        data.putInt("jingnum", (int) material.soul());
        data.putInt("armnum", (int) material.armor());
        data.putInt("attdefnum", (int) material.physicalDefense());
        data.putInt("manadefnum", (int) material.spellDefense());
        data.putInt("souldefnum", (int) material.soulDefense());
        data.putDouble("knockbacknum", material.knockbackResistance());
        data.putDouble("attspeednum", material.attackSpeed());
        data.putDouble("spenum", material.moveSpeed());
        data.putInt("fumozongshu", product.enchantmentCount());
        for (Enchantment enchantment : product.enchantments()) {
            String prefix = "fumo" + enchantment.slot();
            data.putString(prefix, enchantment.id().getPath());
            data.putString(prefix + "kongjian", enchantment.id().getNamespace());
            data.putInt(prefix + "num", enchantment.level() * 100);
        }
        writeFinalStats(data, stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY));
        return prepareUpgrade(data);
    }

    /** 本命培养同时推进最终攻击与对应原料，原料沿用来源getInt后加100的取整。 */
    static void cultivateAttack(ItemStack stack, boolean spell) {
        EquipmentStats current = stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
        EquipmentStats next = spell ? current.addSpellAttack(1) : current.addSoulAttack(1);
        stack.set(EquipmentData.STATS.get(), next);
        if (stack.has(EquipmentData.REFINEMENT.get())) {
            CustomData.update(EquipmentData.REFINEMENT.get(), stack, data -> {
                data.putInt(spell ? "mana_Att" : "soul_Att", spell ? next.spellAttack() : next.soulAttack());
                String key = spell ? "mananum" : "soulnum";
                data.putInt(key, data.getInt(key) + 100);
            });
        }
        EquipmentProduct product = stack.get(EquipmentData.PRODUCT.get());
        if (product != null) {
            Materials raw = product.materials();
            Materials materials = new Materials(raw.physicalAttack(), spell ? Mth.floor(raw.spellAttack()) + 100 : raw.spellAttack(),
                    spell ? raw.soulAttack() : Mth.floor(raw.soulAttack()) + 100, raw.health(), raw.power(), raw.soul(),
                    raw.armor(), raw.physicalDefense(), raw.spellDefense(), raw.soulDefense(),
                    raw.knockbackResistance(), raw.attackSpeed(), raw.moveSpeed());
            stack.set(EquipmentData.PRODUCT.get(), new EquipmentProduct(product.itemId(), product.name(), product.description(),
                    product.type(), product.grade(), product.level(), product.experience(), product.weight(), materials,
                    product.enchantmentCount(), product.enchantments()));
        }
    }

    private static CompoundTag prepareUpgrade(CompoundTag data) {
        if (data.getInt("expnum") == 0) {
            data.putInt("expnum", REVERSED_EXPERIENCE[Math.min(data.getInt("item_level_num"), 41)]);
        }
        reverse(data, "heanum", "health_Max", 5);
        reverse(data, "hearecnum", "health_Rec", 1);
        reverse(data, "powernum", "power_Max", 40);
        reverse(data, "powerrecnum", "power_Rec", 1);
        reverse(data, "jingnum", "soul_Max", 40);
        reverse(data, "jingrecnum", "soul_Rec", 1);
        reverse(data, "attnum", "attack_num", 1);
        reverse(data, "attdefnum", "attack_Def", 1);
        reverse(data, "mananum", "mana_Att", 1);
        reverse(data, "manadefnum", "mana_Def", 1);
        reverse(data, "soulnum", "soul_Att", 1);
        reverse(data, "souldefnum", "soul_Def", 1);
        reverseMultiplier(data, "spenum", "speed_num");
        reverse(data, "armnum", "armor_num", 1);
        reverse(data, "armortoughness", "Armor_Toughness", 1);
        reverseMultiplier(data, "attspeednum", "attack_speed");
        reverseMultiplier(data, "knockbacknum", "knockback_resistance");
        return data;
    }

    private static void reverse(CompoundTag data, String raw, String value, int divisor) {
        if (data.getInt(raw) == 0) data.putInt(raw, data.getInt(value) / divisor * 100);
    }

    private static void reverseMultiplier(CompoundTag data, String raw, String value) {
        if (data.getInt(raw) == 0) data.putInt(raw, (int) (data.getInt(value) / .05 * 100));
    }

    private static void convert(CompoundTag data) {
        int weight = data.getInt("weightnum");
        double scale = weight > 150 ? 0 : weight >= 140 ? .1 : weight >= 130 ? .4 : weight >= 120 ? .65
                : weight >= 110 ? .85 : weight > 100 ? .95 : 1;
        convert(data, "heanum", "health_Max", scale, 5);
        convert(data, "hearecnum", "health_Rec", scale, 1);
        convert(data, "powernum", "power_Max", scale, 40);
        convert(data, "powerrecnum", "power_Rec", scale, 1);
        convert(data, "jingnum", "soul_Max", scale, 40);
        convert(data, "jingrecnum", "soul_Rec", scale, 1);
        convert(data, "attnum", "attack_num", 1, 1);
        convert(data, "attdefnum", "attack_Def", scale, 1);
        convert(data, "mananum", "mana_Att", scale, 1);
        convert(data, "manadefnum", "mana_Def", scale, 1);
        convert(data, "soulnum", "soul_Att", scale, 1);
        convert(data, "souldefnum", "soul_Def", scale, 1);
        convertMultiplier(data, "spenum", "speed_num");
        convert(data, "armnum", "armor_num", 1, 1);
        convert(data, "armortoughness", "Armor_Toughness", 1, 1);
        convertMultiplier(data, "attspeednum", "attack_speed");
        convertMultiplier(data, "knockbacknum", "knockback_resistance");
        if (weight > 100) {
            double penalty = weight > 150 ? 1 : weight >= 140 ? .9 : weight >= 130 ? .6
                    : weight >= 120 ? .35 : weight >= 110 ? .15 : .05;
            data.putDouble("attack_speed", weight >= 110 ? -penalty : data.getDouble("attack_speed") - penalty);
            data.putDouble("speed_num", weight >= 110 ? -penalty : data.getDouble("speed_num") - penalty);
            data.putDouble("knockback_resistance", data.getDouble("knockback_resistance") + penalty);
        }
    }

    private static void convert(CompoundTag data, String raw, String value, double scale, int multiplier) {
        if (data.getDouble(raw) > 0) data.putInt(value, (int) (Math.floor(data.getDouble(raw) * scale / 100) * multiplier));
    }

    private static void convertMultiplier(CompoundTag data, String raw, String value) {
        if (data.getDouble(raw) > 0) data.putDouble(value, Math.floor(data.getDouble(raw) / 100) * .05);
    }

    private static ItemAttributeModifiers modifiers(CompoundTag data, boolean weapon, EquipmentSlotGroup slot) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        if (weapon) add(builder, Attributes.ATTACK_SPEED, "attack_speed_base", -2.6, AttributeModifier.Operation.ADD_VALUE, slot);
        if (data.getDouble("attack_speed") != 0) add(builder, Attributes.ATTACK_SPEED, "attack_speed", data.getDouble("attack_speed"), AttributeModifier.Operation.ADD_MULTIPLIED_BASE, slot);
        if (data.getDouble("speed_num") != 0) add(builder, Attributes.MOVEMENT_SPEED, "move_speed", data.getDouble("speed_num"), AttributeModifier.Operation.ADD_MULTIPLIED_BASE, slot);
        if (data.getInt("knockback_resistance") > 0) add(builder, Attributes.KNOCKBACK_RESISTANCE, "knockback",
                weapon ? data.getInt("knockback_resistance") : data.getDouble("knockback_resistance"), AttributeModifier.Operation.ADD_MULTIPLIED_BASE, slot);
        if (data.getInt("armor_num") > 0) add(builder, Attributes.ARMOR, "armor", data.getInt("armor_num"), AttributeModifier.Operation.ADD_VALUE, slot);
        if (data.getInt("Armor_Toughness") > 0) add(builder, Attributes.ARMOR_TOUGHNESS, "toughness", data.getInt("Armor_Toughness"), AttributeModifier.Operation.ADD_VALUE, slot);
        // Q17：生命与物攻由统一最终值写入原生基础属性，只计算一次。
        return builder.build();
    }

    private static void add(ItemAttributeModifiers.Builder builder, Holder<Attribute> attribute, String name, double amount,
                            AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        builder.add(attribute, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "equipment/refining/" + slot.getSerializedName() + "/" + name), amount, operation), slot);
    }

    private static List<Enchantment> enchant(HolderLookup.Provider registries, ItemStack stack, CompoundTag data) {
        double weight = data.getDouble("weightnum");
        double scale = weight >= 150 ? 0 : weight >= 140 ? .2 : weight >= 130 ? .5 : weight >= 120 ? .75 : weight >= 110 ? .9 : 1;
        List<Enchantment> enchantments = new ArrayList<>();
        for (int index = 0; index < data.getInt("fumozongshu"); index++) {
            String prefix = "fumo" + index;
            int level = (int) Math.floor(data.getDouble(prefix + "num") * scale / 100);
            if (level < 1) continue;
            ResourceLocation sourceId = ResourceLocation.fromNamespaceAndPath(data.getString(prefix + "kongjian"), data.getString(prefix));
            stack.enchant(registries.lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, targetId(sourceId))), level);
            enchantments.add(new Enchantment(index, sourceId, level));
        }
        return enchantments;
    }

    private static void copyBenming(ItemStack previous, ItemStack stack, Map<String, Value> root) {
        EquipmentProperties properties = Equipment.properties(previous);
        CompoundTag old = previous.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        for (String key : List.of("belongToMasterUUID", "benminglevel", "benmingexpmax", "benmingexp")) {
            Value value = properties.fields().get(key);
            if (value != null) {
                if (value.kind() == EquipmentProperties.Kind.STRING) old.putString(key, value.text());
                else if (value.kind() == EquipmentProperties.Kind.INTEGER) old.putInt(key, (int) value.number());
                else old.putDouble(key, value.number());
            }
        }
        BenmingData progress = previous.get(EquipmentData.BENMING.get());
        int level = progress == null ? old.getInt("benminglevel") : progress.level();
        int experience = progress == null ? old.getInt("benmingexp") : Mth.floor(progress.experience());
        int maximum = progress == null ? old.getInt("benmingexpmax") : Mth.floor(progress.experienceMax());
        root.put("belongToMasterUUID", Value.text(old.getString("belongToMasterUUID")));
        root.put("benminglevel", Value.integer(level));
        root.put("benmingexpmax", Value.integer(maximum));
        root.put("benmingexp", Value.integer(experience));
        stack.set(EquipmentData.BENMING.get(), new BenmingData(level, experience, maximum));
    }

    private static EquipmentStats stats(CompoundTag data) {
        return new EquipmentStats(data.getInt("health_Max"), data.getInt("power_Max"), data.getInt("soul_Max"),
                data.getInt("attack_Def"), data.getInt("mana_Def"), data.getInt("soul_Def"), data.getDouble("knockback_resistance"),
                data.getDouble("attack_speed"), data.getDouble("speed_num"), data.getInt("attack_num"), data.getInt("mana_Att"),
                data.getInt("soul_Att"), data.getInt("armor_num"),
                new Recovery(data.getInt("health_Rec"), data.getInt("power_Rec"), data.getInt("soul_Rec")), data.getInt("Armor_Toughness"));
    }

    private static void writeFinalStats(CompoundTag data, EquipmentStats stats) {
        data.putInt("health_Max", stats.health());
        data.putInt("health_Rec", stats.recovery().health());
        data.putInt("power_Max", stats.power());
        data.putInt("power_Rec", stats.recovery().power());
        data.putInt("soul_Max", stats.soul());
        data.putInt("soul_Rec", stats.recovery().soul());
        data.putInt("attack_num", stats.physicalAttack());
        data.putInt("mana_Att", stats.spellAttack());
        data.putInt("soul_Att", stats.soulAttack());
        data.putInt("attack_Def", stats.physicalDefense());
        data.putInt("mana_Def", stats.spellDefense());
        data.putInt("soul_Def", stats.soulDefense());
        data.putDouble("knockback_resistance", stats.knockbackResistance());
        data.putDouble("attack_speed", stats.attackSpeed());
        data.putDouble("speed_num", stats.moveSpeed());
        data.putInt("armor_num", stats.armor());
        data.putInt("Armor_Toughness", stats.toughness());
    }

    private static Materials materialProjection(CompoundTag data) {
        return new Materials(data.getDouble("attnum"), data.getDouble("mananum"), data.getDouble("soulnum"),
                data.getDouble("heanum"), data.getDouble("powernum"), data.getDouble("jingnum"), data.getDouble("armnum"),
                data.getDouble("attdefnum"), data.getDouble("manadefnum"), data.getDouble("souldefnum"),
                data.getDouble("knockbacknum"), data.getDouble("attspeednum"), data.getDouble("spenum"));
    }

    private static int sourceLevel(int experience) {
        if (experience <= 0 || experience >= 4100) return 41;
        return experience / 100;
    }

    private static String grade(int level) {
        if (level == 41) return "超品";
        if (level == 0) return "不入品";
        return List.of("黄阶", "玄阶", "地阶", "天阶").get((level - 1) / 10)
                + List.of("十", "一", "二", "三", "四", "五", "六", "七", "八", "九").get(level % 10) + "级";
    }

    private static ResourceLocation targetId(String id) {
        return targetId(ResourceLocation.parse(id));
    }

    private static ResourceLocation targetId(ResourceLocation id) {
        return id.getNamespace().equals("yvanchuxitong") ? ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id.getPath()) : id;
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
