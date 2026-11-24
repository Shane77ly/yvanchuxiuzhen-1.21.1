package net.yvanchuxiuzhen.system.economy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.book.api.BookTextData;
import net.yvanchuxiuzhen.system.book.api.BookTexts;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.FuluLearningBook;
import net.yvanchuxiuzhen.system.fulu.api.TalismanInstance;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineInstance;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipe;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/** 商品五元组的来源实例构造；功法、技能和材料通过其所有者API生成可用实例。 */
public final class CommerceGoods {
    private CommerceGoods() { }

    public static ItemStack create(String item, String name, String attributes) {
        ResourceLocation id = ResourceLocation.parse(item.toLowerCase(Locale.ENGLISH).replace("yvanchuxitong:", "yvanchuxiuzhen:"));
        CompoundTag values = new CompoundTag();
        if (!attributes.equals("无")) {
            List<String> fields = Arrays.asList(attributes.split("/"));
            for (int offset = 0; offset < fields.size() / 3 * 3; offset += 3) {
                String key = fields.get(offset);
                String value = fields.get(offset + 2);
                switch (fields.get(offset + 1)) {
                    case "char", "str", "name" -> values.putString(key, value);
                    case "logic" -> values.putBoolean(key, value.equals("真"));
                    case "int" -> values.putInt(key, (int) number(value));
                    case "double", "doube" -> values.putDouble(key, number(value));
                    default -> { }
                }
            }
        }
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(id));
        if (!values.getString("ExercisesName").isEmpty()) {
            Map<String, Double> elements = new HashMap<>();
            for (String element : List.of("Gold", "Water", "Tree", "Fire", "Soil")) elements.put(element.toLowerCase(Locale.ROOT), values.getDouble("Exercises" + element));
            List<String> tiers = Arrays.stream(TechniqueStat.values()).map(stat -> values.getString(stat.tierKey())).toList();
            result = Techniques.createBook(new TechniqueDefinition(id, values.getString("ExercisesName"), values.getString("belongTopinjietext"),
                    values.getDouble("belongTopinjie"), values.getString("belongToLeveltext"), values.getDouble("belongToLevel"),
                    values.getString("belongTobody"), values.getString("belongTojianjie"), elements, tiers));
        } else {
            for (SkillKind kind : SkillKind.values()) {
                String key = kind.getSerializedName();
                if (values.getString(key + "Name").isEmpty()) continue;
                List<String> arguments = new ArrayList<>(List.of(id.toString(), values.getString(key + "Name"),
                        Double.toString(values.getDouble("belongTopinjie")), Double.toString(values.getDouble("belongToLevel")),
                        values.getString("belongTobody"), values.getString("belongTojianjie")));
                List<String> keys = switch (kind) {
                    case SPELL -> List.of("jinengneeditem", "jinengmanaatt", "jinengshanghai", "jinengxiaohao", "jinengjieyin", "LengQueGold", "LengQueWater", "LengQueTree", "LengQueFire", "LengQueSoil", "LengQueMana", "LengQueGoldIgnore", "LengQueWaterIgnore", "LengQueTreeIgnore", "LengQueFireIgnore", "LengQueSoilIgnore", "LengQueManaIgnore");
                    case WEAPON_ART -> List.of("smanshiptype", "smanshipID", "smanshipnumMax");
                    case MOVEMENT -> List.of("shenfatick", "shenfatickMax", "shenfajumptick", "shenfajumptickMax", "shenfaWjump", "shenfaSjump", "shenfaAjump", "shenfaDjump", "shenfaWspeed");
                    case FLIGHT -> List.of("flyskillxiaohao", "flyskillspeed", "flyskilltexiao");
                };
                for (String field : keys) arguments.add(values.contains(field, 8) ? values.getString(field) : Double.toString(values.getDouble(field)));
                result = Skills.createBook(kind, arguments);
                break;
            }
        }
        if (values.contains("cailiaolevel") || values.contains("cailiaopinjie")) Materials.setInstance(result, values.getInt("cailiaolevel"), values.getInt("cailiaopinjie"));
        attachProductionComponents(result, values);
        if (!name.equals("无")) result.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        if (!attributes.equals("无")) {
            String[] fields = attributes.split("/");
            for (int offset = 0; offset < fields.length / 3 * 3; offset += 3) {
                if (fields[offset + 1].equals("name")) result.set(DataComponents.CUSTOM_NAME, Component.literal(fields[offset + 2]));
            }
        }
        return result;
    }

    private static void attachProductionComponents(ItemStack result, CompoundTag values) {
        if (values.getBoolean("fuluget")) {
            Fulu.setLearningBook(result, new FuluLearningBook(true, values.getString("fulumingling")));
        }
        if (values.contains("fululevel")) {
            Fulu.setTalisman(result, new TalismanInstance(
                    values.getInt("fululevel"), values.getString("fululeveltxt"), 0, 0));
        }
        if (!values.getString("danyaoname").isEmpty() && !values.getString("danfangid").isEmpty()) {
            List<MedicineRecipe.Ingredient> ingredients = new ArrayList<>(8);
            for (int slot = 1; slot <= 8; slot++) {
                String key = "cailiao0" + slot;
                ingredients.add(new MedicineRecipe.Ingredient(values.getString(key), values.getString(key + "name")));
            }
            Liandan.setRecipe(result, new MedicineRecipe(values.getString("danyaoname"),
                    values.getString("danfangshuoming"), values.getString("danyaoshuoming"),
                    values.getString("item_level"), values.getInt("item_level_num"),
                    values.getString("danfangid"), ingredients));
        }
        if (!values.getString("tuzhiname").isEmpty()) {
            List<LianqiBlueprint.Ingredient> ingredients = new ArrayList<>(9);
            for (int slot = 0; slot < 9; slot++) {
                String key = "cailiao0" + slot;
                ingredients.add(new LianqiBlueprint.Ingredient(values.getString(key), values.getString(key + "name"),
                        "", values.getInt(key + "num")));
            }
            Lianqi.setBlueprint(result, new LianqiBlueprint(values.getString("tuzhiname"), "",
                    values.getString("tuzhishuoming"), values.getString("fabaoid"),
                    values.getString("fabaoshuoming"), "", values.getString("fabaotype"),
                    values.getString("item_level"), values.getInt("item_level_num"),
                    values.getString("belongToItemID"), ingredients));
        }
        if (!values.getString("textdisplay").isEmpty()) {
            BookTexts.set(result, new BookTextData(values.getString("textdisplay"), List.of()));
        }
        if (!values.getString("TreasureTypeBig").isEmpty()) {
            TreasureContainers.setConfig(result, new TreasureContainerConfig(values.getString("TreasureTypeBig"),
                    values.getString("TreasureTypeSmall"), values.getDouble("TreasureLEVEL"),
                    values.getDouble("minimumvalue"), values.getDouble("Maximumvalue")));
        }
    }

    /** 来源数字解释器对无效数字返回0，限定为来源商品与脚本数值格式。 */
    public static double number(String value) {
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException error) { return 0; }
    }
}
