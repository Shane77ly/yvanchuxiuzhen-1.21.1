package net.yvanchuxiuzhen.system.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.system.lianqi.api.BlueprintReward;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipeReward;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/** 副本奖励格式独立于开箱表；业务实例分别交由各数据所有者构造。 */
final class DungeonRewards {
    private DungeonRewards() {
    }

    static ItemStack create(String[] fields) {
        Item item = BuiltInRegistries.ITEM.get(id(fields[0].toLowerCase(Locale.ENGLISH)));
        CompoundTag data = new CompoundTag();
        Map<String, Double> numbers = new HashMap<>();
        Map<String, String> texts = new HashMap<>();
        for (int index = 4; index + 2 < fields.length; index += 3) {
            String key = fields[index];
            String value = fields[index + 2];
            switch (fields[index + 1]) {
                case "char", "str" -> { data.putString(key, value); texts.put(key, value); }
                case "int" -> { int number = (int) number(value); data.putInt(key, number); numbers.put(key, (double) number); }
                case "double", "doube" -> { double number = number(value); data.putDouble(key, number); numbers.put(key, number); }
                case "logic" -> data.putBoolean(key, value.equals("真"));
                default -> { }
            }
        }
        ItemStack stack;
        if (texts.containsKey("jinengName")) {
            stack = Skills.createBook(item, new SkillBook(SkillKind.SPELL, data.getString("jinengName"), data.getDouble("belongTopinjie"),
                    data.getDouble("belongToLevel"), data.getString("belongTobody"), data.getString("belongTojianjie"),
                    Optional.empty(), "", Map.copyOf(numbers), Map.copyOf(texts)));
        } else if (texts.containsKey("tuzhiname")) {
            List<BlueprintReward.Ingredient> ingredients = new ArrayList<>();
            for (int index = 0; index < 9; index++) {
                String key = "cailiao0" + index;
                ingredients.add(new BlueprintReward.Ingredient(remap(data.getString(key)), data.getString(key + "name"), data.getInt(key + "num")));
            }
            stack = Lianqi.createBlueprintReward(item, new BlueprintReward(data.getString("tuzhiname"), data.getString("tuzhishuoming"),
                    remap(data.getString("fabaoid")), data.getString("fabaoshuoming"), data.getString("fabaotype"),
                    data.getString("item_level"), data.getInt("item_level_num"), ingredients));
        } else if (texts.containsKey("danyaoname")) {
            List<MedicineRecipeReward.Ingredient> ingredients = new ArrayList<>();
            for (int index = 1; index <= 8; index++) {
                String key = "cailiao0" + index;
                ingredients.add(new MedicineRecipeReward.Ingredient(remap(data.getString(key)), data.getString(key + "name")));
            }
            stack = Liandan.createRecipeReward(item, new MedicineRecipeReward(data.getString("danyaoname"), data.getString("danfangshuoming"),
                    data.getString("danyaoshuoming"), data.getString("item_level"), data.getInt("item_level_num"), ingredients));
        } else if (texts.containsKey("TreasureTypeBig")) {
            stack = TreasureContainers.create(item, data.getString("TreasureTypeBig"), data.getString("TreasureTypeSmall"),
                    data.getDouble("TreasureLEVEL"), data.getDouble("minimumvalue"), data.getDouble("Maximumvalue"));
        } else {
            stack = new ItemStack(item);
            if (data.contains("cailiaolevel") || data.contains("cailiaopinjie")) Materials.setInstance(stack, data.getInt("cailiaolevel"), data.getInt("cailiaopinjie"));
        }
        // 所有者已写入的根字段优先；其余来源字段随奖励保存，不反写类型化业务组件。
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            for (String key : data.getAllKeys()) if (!tag.contains(key)) tag.put(key, data.get(key).copy());
        });
        if (!fields[1].equals("无")) stack.set(DataComponents.CUSTOM_NAME, Component.literal(fields[1]));
        stack.setCount((int) number(fields[3]));
        return stack;
    }

    static ResourceLocation id(String value) { return ResourceLocation.parse(remap(value)); }
    static String remap(String value) { return value.replace("yvanchuxitong:", "yvanchuxiuzhen:"); }

    /** MCreator来源数值转换失败为零。 */
    static double number(String value) {
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException exception) { return 0; }
    }
}
