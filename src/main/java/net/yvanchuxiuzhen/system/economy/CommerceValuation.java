package net.yvanchuxiuzhen.system.economy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.CommerceSession;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineInstance;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 灵石收购按来源类别优先级和实例字段估价；结果与两行显示来自同一次计算。 */
final class CommerceValuation {
    private static final JsonObject TABLES = CommerceCatalog.load("valuation");
    private static final Map<String, Double> HERBS = herbs();
    private static final Map<String, Double> TALISMANS = talismans();

    private CommerceValuation() { }

    static double value(ServerLevel level, ItemStack stack, CommerceSession.Builder state) {
        EconomyPrices prices = EconomyPrices.get(level);
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        double unit = 0;
        double total = 0;
        int count = stack.getCount();
        double money = Equipment.properties(stack).number("money");
        if (money < 1) money = stack.getOrDefault(CommerceData.MARKET_VALUE.get(), 0.0);
        if (money >= 1) {
            unit = money;
            total = count * unit;
        } else if (tag(stack, "book")) {
            int grade = Techniques.book(stack).map(book -> (int) book.definition().grade())
                    .orElseGet(() -> Skills.book(stack).map(book -> (int) book.grade()).orElse(0));
            boolean technique = Techniques.book(stack).isPresent();
            Optional<LianqiBlueprint> blueprint = Lianqi.blueprint(stack);
            if (grade >= 1) {
                unit = table("Gongfajiage", grade) * prices.multiplier("gongfajiage") * (technique ? 2 : 1);
                total = count * unit;
            } else if (blueprint.isPresent() && blueprint.get().level() >= 1) {
                int blueprintLevel = blueprint.get().level();
                unit = 9 * Math.pow(4, Math.min(8, (blueprintLevel - 1) / 5)) * prices.multiplier("tuzhijiage");
                // 来源重量过程把材料列表索引1的“attribute”交给数字解释器，所有匹配条目均加0。
                total = count * unit;
            }
        } else if (anyTag(stack, "wuqi", "fabao", "toukui", "xiongjia", "kuzi", "zhanxve")) {
            String owner = Equipment.ownerId(stack);
            int equipmentLevel = Equipment.refiningLevel(stack);
            int weight = Equipment.weight(stack);
            if (owner.isEmpty() && equipmentLevel >= 1) {
                unit = table("Lianqishoumaijizhi", equipmentLevel) * 60 * prices.multiplier("zhuangbeijiage");
                total = count * unit;
                if (weight > 145) total = 1;
                else if (weight > 140) total *= 0.05;
                else if (weight > 130) total *= 0.2;
                else if (weight > 120) total *= 0.5;
                else if (weight > 110) total *= 0.75;
                else if (weight > 100) total *= 0.9;
            }
        } else if (tag(stack, "danyaoguan")) {
            unit = medicine(Liandan.medicine(stack)) * prices.multiplier("danyaojiage");
            total = count * unit;
        } else if (tag(stack, "fulu")) {
            int talismanLevel = Fulu.talisman(stack).level();
            unit = talismanLevel <= 1 ? 12 : talismanLevel == 2 ? 192 : talismanLevel == 3 ? 3072 : 49152;
            if (TALISMANS.containsKey(id)) unit = Math.ceil(unit * TALISMANS.get(id) * 1.5);
            unit *= prices.multiplier("fulujiage");
            total = count * unit;
        } else if (anyTag(stack, "headornaments", "necklaceornaments", "handornaments", "beltornaments")) {
            int accessoryLevel = Equipment.properties(stack).fields().containsKey("item_level_num")
                    ? (int) Equipment.properties(stack).number("item_level_num") : Equipment.refiningLevel(stack);
            if (accessoryLevel >= 1) {
                unit = (accessoryLevel <= 40 ? table("accessories", accessoryLevel) : 98304)
                        * prices.multiplier("shipinjiage") * 2;
                total = count * unit;
            }
        } else if (tag(stack, "caoyao") && !tag(stack, "yaodan") && !tag(stack, "duyao")) {
            unit = HERBS.getOrDefault(id, 0.0) * prices.multiplier("yaocaijiage");
            total = count * unit;
        } else if (tag(stack, "cailiao")) {
            if (tag(stack, "lingshi")) unit = fixedTier(id, 191, 199, 1);
            else if (tag(stack, "huoyan")) unit = fixedTier(id, 221, 229, 10) + fixedTier(id, 231, 239, 10);
            else {
                MaterialInstance instance = Materials.instance(stack);
                unit = table("Cailiaochushou", Math.max(1, instance.level())) * (instance.quality() + 1) * prices.multiplier("cailiaojiage");
            }
            total = count * unit;
        }
        state.bankTest1(unit >= 1 ? "单价：" + format(unit) + " 数量：" + format(count) + "个" : "无法售卖")
                .bankTest2("");
        return total;
    }

    private static double medicine(MedicineInstance data) {
        boolean positive = false;
        String effect = "";
        double sum = 0;
        double sideEffects = 0;
        JsonObject effects = TABLES.getAsJsonObject("medicine_effects");
        for (MedicineInstance.Effect entry : data.effects()) {
            if (entry.level() < 1) break;
            effect = entry.id();
            int effectLevel = entry.level();
            if (!data.treasure()) effectLevel /= 100;
            if (effects.has(effect)) positive = effects.get(effect).getAsBoolean();
            sum += (positive ? 1 : -1) * Math.pow(3, effectLevel);
            if (!positive) sideEffects += effectLevel;
        }
        double unit = Math.abs(sum) * data.doses();
        if (sideEffects > 5) unit = Math.max(1, unit - unit * (sideEffects - 10) * 0.2);
        if (unit <= 0 && !effect.isEmpty()) unit = 1;
        return unit;
    }

    private static double table(String key, int level) {
        JsonObject table = TABLES.getAsJsonObject(key);
        return table.has(Integer.toString(level)) ? table.get(Integer.toString(level)).getAsDouble() : 0;
    }

    private static double fixedTier(String id, int start, int end, int unit) {
        for (int item = start; item <= end; item++) if (id.equals("yvanchuxiuzhen:cailiao_" + item)) return unit * Math.pow(4, item - start);
        return 0;
    }

    private static boolean tag(ItemStack stack, String name) { return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name))); }
    private static boolean anyTag(ItemStack stack, String... names) {
        for (String name : names) if (tag(stack, name)) return true;
        return false;
    }
    private static String format(double value) { return new DecimalFormat("0").format(value); }
    private static Map<String, Double> herbs() {
        return doubleMap("herbs");
    }

    private static Map<String, Double> talismans() {
        return doubleMap("talismans");
    }

    private static Map<String, Double> doubleMap(String key) {
        JsonObject table = TABLES.getAsJsonObject(key);
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : table.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAsDouble());
        }
        return Map.copyOf(result);
    }
}
