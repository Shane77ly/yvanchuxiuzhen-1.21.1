package net.yvanchuxiuzhen.system.xiulian;

import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;

/**
 * 进入结丹（21 层）时凝结金丹、进入元婴（31 层）时孕育元婴（来源 {@code Jindanhuoqv01}–{@code 05}、{@code Yvanyinghuoqv01}）。
 * 结果写成体质标记 {@code tizhiopen_<编号>} 与对应修正值。主修功法只在本人习得时参与判定，否则按无功法（五行为空、品阶与层数为 0）。
 *
 * <p>金丹自第 1 档起依次判定，每档内按“五行均衡 → 妖丹 → 单一灵根（金水木火土）”取第一个满足的结果，都不满足时进入下一档；
 * 第 5 档只看单一灵根，最后得废丹（101）。五行均衡要求功法五行为“无”“五”或“金水木火土”，妖丹另接受两个字以上的五行；
 * 单一灵根第 1–4 档要求功法五行恰为该行并开启对应灵体（{@code tizhiopen_003}–{@code 007}），第 5 档只要求功法五行含该行。
 * 妖丹结果清空本命槽。
 */
final class GoldenCore {
    private static final TagKey<Item> YAODAN = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaodan"));
    private static final Set<String> FIVE_ELEMENTS = Set.of("无", "五", "金水木火土");
    /** 单一灵根：功法五行、灵根、对应灵体、击杀积分键、修正键中该行的写法与被克制一行的写法。 */
    private static final List<Element> ELEMENTS = List.of(
            new Element("金", CultivationValue.ROOT_GOLD, "003", "gold", "Gold", "Tree"),
            new Element("水", CultivationValue.ROOT_WATER, "004", "water", "Water", "Fire"),
            new Element("木", CultivationValue.ROOT_TREE, "005", "tree", "Tree", "Soil"),
            new Element("火", CultivationValue.ROOT_FIRE, "006", "fire", "Fire", "Gold"),
            new Element("土", CultivationValue.ROOT_SOIL, "007", "soil", "Soil", "Water"));
    /**
     * 第 1–4 档的门槛与奖励。五行均衡第 1 档要求灵根之和不少于 44，其余档要求每行不少于 {@code rootEach}。
     * 妖丹接受 {@code cailiao_<firstPill>}–{@code cailiao_189}。单一灵根结果编号为 110 + 5 × 行序 + (5 − 档)。
     */
    private static final List<Tier> TIERS = List.of(
            new Tier(1, 0, 31, 2500, 187, 720, 9, 400, 6, 0.08),
            new Tier(2, 8, 21, 2200, 186, 700, 8, 300, 4, 0.06),
            new Tier(3, 7, 11, 1900, 185, 650, 7, 200, 2, 0.04),
            new Tier(4, 5, 1, 1500, 183, 500, 5, 100, 1, 0.02));

    private record Element(String name, CultivationValue root, String body, String killKey, String key, String restrained) {
    }

    private record Tier(int number, double rootEach, double grade, double powerMax, int firstPill, double healthMax,
                        double rootSingle, double killPoints, double attack, double defensePercentage) {
    }

    private GoldenCore() {
    }

    static void formCore(ServerPlayer player) {
        add(player, "tizhinumber", 1);
        MainTechnique technique = technique(player);
        for (Tier tier : TIERS) {
            if (technique.grade() < tier.grade() || technique.level() < 15) {
                continue;
            }
            if (FIVE_ELEMENTS.contains(technique.element()) && balanced(player, tier)
                    && PlayerAttributes.value(player, Stat.POWER_MAX) >= tier.powerMax()) {
                open(player, 106 - tier.number());
                balancedReward(player, tier.number());
                return;
            }
            if (pill(player, tier) && (FIVE_ELEMENTS.contains(technique.element()) || technique.element().length() >= 2)
                    && player.getMaxHealth() >= tier.healthMax()) {
                open(player, 110 - tier.number());
                if (tier.number() == 1) {
                    add(player, "HealthAddSkillCorrect", 1);
                }
                Cultivation.setBenming(player, ItemStack.EMPTY);
                return;
            }
            for (int i = 0; i < ELEMENTS.size(); i++) {
                Element element = ELEMENTS.get(i);
                if (Cultivation.get(player, element.root()) >= tier.rootSingle() && technique.element().equals(element.name())
                        && Cultivation.tizhiBoolean(player, "tizhiopen_" + element.body())
                        && Cultivation.tizhiDouble(player, "killentitypoints" + element.killKey()) >= tier.killPoints()) {
                    open(player, 110 + 5 * i + 5 - tier.number());
                    add(player, element.key() + "AttCorrect", tier.attack());
                    add(player, element.restrained() + "DefCorrectPercentage", tier.defensePercentage());
                    return;
                }
            }
        }
        Cultivation.add(player, CultivationValue.EXPERIENCE_HINDER, -0.3);
        if (technique.grade() >= 1 && technique.level() >= 15) {
            for (int i = 0; i < ELEMENTS.size(); i++) {
                Element element = ELEMENTS.get(i);
                if (Cultivation.get(player, element.root()) >= 3 && technique.element().contains(element.name())
                        && Cultivation.tizhiBoolean(player, "tizhiopen_" + element.body())) {
                    open(player, 110 + 5 * i);
                    add(player, element.restrained() + "DefCorrect", -5);
                    return;
                }
            }
        }
        open(player, 101);
        add(player, "LevelupCorrectDaotu", 0.5);
    }

    /**
     * 元婴：依次判定五行均衡（需五行均衡金丹）、妖丹（需妖丹金丹）、单一灵根（需同行金丹），都不满足时得普通元婴（201）。
     * 来源在取得体质载体之前给空物品加 {@code tizhinumber}，体质计数不变（Q23）。
     */
    static void formNascentSoul(ServerPlayer player) {
        MainTechnique technique = technique(player);
        boolean technical = technique.grade() >= 31 && technique.level() >= 25;
        boolean balanced = true;
        for (Element element : ELEMENTS) {
            balanced &= Cultivation.get(player, element.root()) >= 8;
        }
        if (technical && balanced && FIVE_ELEMENTS.contains(technique.element())
                && PlayerAttributes.value(player, Stat.POWER_MAX) >= 6000 && anyOpen(player, 102, 105)) {
            open(player, 202);
            add(player, "XiulianColdShangxianCorrect", 20);
            add(player, "XiulianColdCorrect", 0.1);
            add(player, "ShouyvanAddSkillCorrect", 20);
            add(player, "LevelupCorrectShouyvan", 730);
            add(player, "RebirthCorrectHealthPercentage", 0.15);
            add(player, "RebirthCorrectPowerPercentage", 0.15);
            return;
        }
        if (technical && (FIVE_ELEMENTS.contains(technique.element()) || technique.element().length() >= 2)
                && player.getMaxHealth() >= 1200 && anyOpen(player, 106, 109)) {
            open(player, 203);
            add(player, "ShouyvanAddSkillCorrect", 30);
            return;
        }
        for (int i = 0; i < ELEMENTS.size(); i++) {
            Element element = ELEMENTS.get(i);
            if (technical && Cultivation.get(player, element.root()) >= 8 && technique.element().equals(element.name())
                    && anyOpen(player, 111 + 5 * i, 114 + 5 * i) && Cultivation.tizhiBoolean(player, "tizhiopen_" + element.body())
                    && Cultivation.tizhiDouble(player, "killentitypoints" + element.killKey()) >= 400) {
                open(player, 204 + i);
                add(player, element.key() + "DefCorrectPercentage", 0.05);
                add(player, "WeaponCultivateSpeed", 3);
                return;
            }
        }
        open(player, 201);
        add(player, "WeaponCultivateSpeed", 1);
    }

    private static MainTechnique technique(ServerPlayer player) {
        MainTechnique technique = Cultivation.mainTechnique(player);
        return technique.owned() ? technique : MainTechnique.NONE;
    }

    private static boolean balanced(ServerPlayer player, Tier tier) {
        double sum = 0;
        boolean each = true;
        for (Element element : ELEMENTS) {
            double root = Cultivation.get(player, element.root());
            sum += root;
            each &= root >= tier.rootEach();
        }
        return tier.number() == 1 ? sum >= 44 : each;
    }

    /** 本命槽是妖丹且为 {@code cailiao_<firstPill>}–{@code cailiao_189} 之一。来源另接受实例等级 {@code cailiaolevel} 达标的妖丹，随 material 接入。 */
    private static boolean pill(ServerPlayer player, Tier tier) {
        ItemStack benming = Cultivation.benming(player);
        if (!benming.is(YAODAN)) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(benming.getItem());
        for (int pill = tier.firstPill(); pill <= 189; pill++) {
            if (id.equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_" + pill))) {
                return true;
            }
        }
        return false;
    }

    /** 五行均衡金丹第 1–4 档（105、104、103、102）的修正。 */
    private static void balancedReward(ServerPlayer player, int tier) {
        switch (tier) {
            case 1 -> {
                add(player, "ColddownCorrectPercentage", -0.2);
                add(player, "ShouyvanAddSkillCorrect", 10);
                add(player, "LevelupCorrectShouyvan", 365);
                add(player, "RebirthCorrectHealthPercentage", 0.15);
                add(player, "RebirthCorrectPowerPercentage", 0.15);
            }
            case 2 -> {
                add(player, "ColddownCorrectPercentage", -0.1);
                add(player, "ShouyvanAddSkillCorrect", 3);
                add(player, "LevelupCorrectShouyvan", 90);
                add(player, "RebirthCorrectHealthPercentage", 0.1);
            }
            case 3 -> {
                add(player, "ColddownCorrectPercentage", -0.05);
                add(player, "ShouyvanAddSkillCorrect", 1);
                add(player, "LevelupCorrectShouyvan", 30);
            }
            default -> add(player, "ColddownCorrect", -4);
        }
    }

    private static boolean anyOpen(ServerPlayer player, int from, int to) {
        for (int number = from; number <= to; number++) {
            if (Cultivation.tizhiBoolean(player, "tizhiopen_" + number)) {
                return true;
            }
        }
        return false;
    }

    private static void open(ServerPlayer player, int number) {
        Cultivation.setTizhiBoolean(player, "tizhiopen_" + number, true);
    }

    private static void add(ServerPlayer player, String key, double delta) {
        Cultivation.setTizhiDouble(player, key, Cultivation.tizhiDouble(player, key) + delta);
    }
}
