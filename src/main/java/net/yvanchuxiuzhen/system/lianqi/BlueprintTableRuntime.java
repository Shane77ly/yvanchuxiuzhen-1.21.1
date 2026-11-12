package net.yvanchuxiuzhen.system.lianqi;

import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.BlueprintTableBlockEntity;
import net.yvanchuxiuzhen.system.material.api.MaterialDefinition;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 天道图纸台按九个实际材料栈计算预览，再把材料清单写入原槽载体。 */
public final class BlueprintTableRuntime {
    private BlueprintTableRuntime() {
    }

    /** 每次开窗重置来源指定的一百九十一栈材料，保留目录末尾十九槽。 */
    public static void populate(BlueprintTableBlockEntity table) {
        int slot = 13;
        for (int[] range : new int[][] {{1, 146}, {181, 189}, {201, 209}, {211, 219}, {221, 229}, {231, 239}}) {
            for (int material = range[0]; material <= range[1]; material++) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                        String.format(Locale.ROOT, "cailiao_%03d", material));
                table.setItem(slot++, new ItemStack(BuiltInRegistries.ITEM.get(id), 64));
            }
        }
        table.changed();
    }

    /** 预览逐条处理完整材料定义并逐次截断整数，不消耗输入，也不写附魔总数字段。 */
    public static void refresh(BlueprintTableBlockEntity table) {
        CompoundTag raw = new CompoundTag();
        Accumulator accumulator = new Accumulator(raw);
        double weight = 0;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack input = table.getItem(slot);
            MaterialDefinition definition = Materials.definition(input);
            if (definition == null) continue;
            int unitWeight = definition.fixedWeight().orElseThrow();
            weight += unitWeight * input.getCount();
            accumulator.add(definition, Materials.instance(input), input.getCount());
        }
        raw.putInt("weightnum", (int) weight);
        derive(raw);
        type(raw, table.getItem(10));
        grade(raw);
        table.getPersistentData().put("zong", raw);
        table.changed();
    }

    /** 保存沿用载体原有组件，将九槽配方写入根字段，并把载体栈数量设为一。 */
    public static void save(BlueprintTableBlockEntity table, String inputName) {
        refresh(table);
        CompoundTag raw = table.getPersistentData().getCompound("zong");
        ItemStack template = table.getItem(10);
        type(raw, template);
        ItemStack result = table.getItem(9).copy();
        CompoundTag recipe = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        recipe.putString("tuzhiname", inputName.isEmpty() ? template.getDisplayName().getString() : inputName);
        Component name = inputName.isEmpty() ? template.getHoverName().copy()
                : Component.literal(inputName.replace("[", "").replace("]", ""));
        recipe.putString("tuzhiname_component", Component.Serializer.toJson(name, table.getLevel().registryAccess()));
        recipe.putString("fabaoid", BuiltInRegistries.ITEM.getKey(template.getItem()).toString());
        recipe.putString("fabaoshuoming", table.getItem(11).getDisplayName().getString());
        recipe.putString("fabaoshuoming_component", Component.Serializer.toJson(
                Component.literal("[").append(table.getItem(11).getHoverName().copy()).append("]"), table.getLevel().registryAccess()));
        recipe.putString("fabaotype", raw.getString("fabaotype"));
        for (int slot = 0; slot < 9; slot++) {
            ItemStack ingredient = table.getItem(slot);
            String key = "cailiao0" + slot;
            recipe.putString(key, BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString());
            recipe.putString(key + "name", ingredient.getDisplayName().getString());
            recipe.putString(key + "name_component", Component.Serializer.toJson(
                    Component.literal("[").append(ingredient.getHoverName().copy()).append("]"), table.getLevel().registryAccess()));
            recipe.putInt(key + "num", ingredient.getCount());
        }
        recipe.putString("item_level", raw.getString("item_level"));
        recipe.putInt("item_level_num", raw.getInt("item_level_num"));
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(recipe));
        result.setCount(1);
        table.setItem(9, result);
        table.changed();
    }

    private static void type(CompoundTag raw, ItemStack template) {
        String type;
        if (template.getItem() instanceof SwordItem || tagged(template, ModId.VALUE, "wuqi")) type = "武器";
        else if (tagged(template, ModId.VALUE, "fabao")) type = "法宝";
        else if (tagged(template, ModId.VALUE, "toukui") || tagged(template, "minecraft", "head_armor")) type = "头盔";
        else if (tagged(template, ModId.VALUE, "xiongjia") || tagged(template, "minecraft", "chest_armor")) type = "胸甲";
        else if (tagged(template, ModId.VALUE, "kuzi") || tagged(template, "minecraft", "leg_armor")) type = "裤子";
        else if (tagged(template, ModId.VALUE, "zhanxve") || tagged(template, "minecraft", "foot_armor")) type = "战靴";
        else if (tagged(template, ModId.VALUE, "headornaments")) type = "头饰";
        else if (tagged(template, ModId.VALUE, "necklaceornaments")) type = "项坠";
        else if (tagged(template, ModId.VALUE, "handornaments")) type = "手饰";
        else if (tagged(template, ModId.VALUE, "beltornaments")) type = "腰饰";
        else return;
        raw.putString("fabaotype", type);
    }

    private static boolean tagged(ItemStack stack, String namespace, String path) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, path)));
    }

    private static void grade(CompoundTag raw) {
        int grade = 41;
        for (int value = 41; value >= 0; value--) {
            if (raw.getInt("expnum") >= (value == 0 ? 1 : value * 100)) {
                grade = value;
                break;
            }
        }
        String name;
        if (grade == 41) name = "超品";
        else if (grade == 0) name = "不入品";
        else name = new String[] {"黄阶", "玄阶", "地阶", "天阶"}[(grade - 1) / 10]
                    + new String[] {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}[(grade - 1) % 10] + "级";
        raw.putString("item_level", name);
        raw.putInt("item_level_num", grade);
    }

    private static void derive(CompoundTag raw) {
        int weight = raw.getInt("weightnum");
        double factor = weight > 150 ? 0 : weight >= 140 ? .1 : weight >= 130 ? .4
                : weight >= 120 ? .65 : weight >= 110 ? .85 : weight > 100 ? .95 : 1;
        integerValue(raw, "heanum", "health_Max", factor, 10);
        integerValue(raw, "hearecnum", "health_Rec", factor, 1);
        integerValue(raw, "powernum", "power_Max", factor, 40);
        integerValue(raw, "powerrecnum", "power_Rec", factor, 1);
        integerValue(raw, "jingnum", "soul_Max", factor, 40);
        integerValue(raw, "jingrecnum", "soul_Rec", factor, 1);
        integerValue(raw, "attnum", "attack_num", 1, 1);
        integerValue(raw, "attdefnum", "attack_Def", factor, 1);
        integerValue(raw, "mananum", "mana_Att", factor, 1);
        integerValue(raw, "manadefnum", "mana_Def", factor, 1);
        integerValue(raw, "soulnum", "soul_Att", factor, 1);
        integerValue(raw, "souldefnum", "soul_Def", factor, 1);
        integerValue(raw, "armnum", "armor_num", 1, 1);
        integerValue(raw, "armortoughness", "Armor_Toughness", 1, 1);
        for (String[] entry : new String[][] {{"spenum", "speed_num"}, {"attspeednum", "attack_speed"},
                {"knockbacknum", "knockback_resistance"}}) {
            if (raw.getDouble(entry[0]) > 0) raw.putDouble(entry[1], Math.floor(raw.getDouble(entry[0]) / 100) * .05);
        }
        double penalty = weight > 150 ? 1 : weight >= 140 ? .9 : weight >= 130 ? .6
                : weight >= 120 ? .35 : weight >= 110 ? .15 : weight > 100 ? .05 : 0;
        if (weight >= 110) {
            raw.putDouble("attack_speed", -penalty);
            raw.putDouble("speed_num", -penalty);
        } else if (weight > 100) {
            raw.putDouble("attack_speed", raw.getDouble("attack_speed") - penalty);
            raw.putDouble("speed_num", raw.getDouble("speed_num") - penalty);
        }
        if (weight > 100) raw.putDouble("knockback_resistance", raw.getDouble("knockback_resistance") + penalty);
    }

    private static void integerValue(CompoundTag raw, String source, String target, double factor, int scale) {
        if (raw.getDouble(source) > 0) raw.putInt(target, (int) (Math.floor(raw.getDouble(source) * factor / 100) * scale));
    }

    /** 未识别属性沿用上次工作值；附魔只按名称合并，命名空间保留首次出现值。 */
    private static final class Accumulator {
        private final CompoundTag raw;
        private int enchantments;
        private double attribute;

        private Accumulator(CompoundTag raw) {
            this.raw = raw;
        }

        private void add(MaterialDefinition definition, MaterialInstance instance, int count) {
            int level = Math.max(1, instance.level());
            int quality = instance.quality();
            double multiplier = quality == 4 ? 1.15 : quality == 3 ? 1.05 : quality == 1 ? .95 : .85;
            for (MaterialDefinition.Entry entry : definition.entries()) {
                String name = entry.name();
                if (entry.namespace().equals("attribute")) {
                    attribute = switch (name) {
                        case "heanum" -> 3 * level + 5 * Math.floor(level / 10.0);
                        case "powernum", "jingnum" -> 10 * level + 10 * Math.floor(level / 10.0) + 8;
                        case "attnum", "mananum", "soulnum" -> 6 * level + 4 * Math.floor(level / 10.0) + 22;
                        case "attdefnum", "manadefnum", "souldefnum" -> 1.5 * level + Math.floor(level / 10.0) + 6;
                        case "spenum", "expnum" -> 5 * level;
                        case "attspeednum", "knockbacknum" -> 2 * level + 8;
                        case "armortoughness", "armnum" -> level;
                        default -> attribute;
                    };
                    attribute *= multiplier;
                    attribute *= definition.fixedWeight().orElseThrow();
                    attribute *= .2;
                    raw.putInt(name, (int) (raw.getInt(name) + attribute * count));
                } else if (entry.namespace().equals("teshu")) {
                    raw.putBoolean(name, true);
                } else if (!entry.namespace().isEmpty()) {
                    double amount = (level + 5) * multiplier;
                    amount *= definition.fixedWeight().orElseThrow();
                    amount *= .2;
                    for (int group = 0; group <= enchantments; group++) {
                        String key = "fumo" + group;
                        if (raw.getString(key).isEmpty()) {
                            raw.putString(key, name);
                            raw.putString(key + "kongjian", entry.namespace());
                            raw.putInt(key + "num", (int) (amount * count));
                            enchantments++;
                            break;
                        } else if (raw.getString(key).equals(name)) {
                            raw.putInt(key + "num", (int) (raw.getInt(key + "num") + amount * count));
                            break;
                        }
                    }
                } else break;
            }
        }
    }
}
