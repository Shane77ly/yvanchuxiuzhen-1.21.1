package net.yvanchuxiuzhen.system.xiulian;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.scores.Objective;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.LevelUpMenu;

/**
 * 境界提升（来源 {@code LEVELup}、{@code LevelupCorrect}、{@code RealmupCorrect}）。
 * 层数不超过 41 时加 1 并得属性点 5 + ⌊新层数 × 0.1⌋，再按体质等写入的 {@code LevelupCorrect*} 修正成长；
 * 进入 1、11、21、31、41 层与 41 层以上时加寿元，11、21、31 与 41 层以上另按 {@code RealmupCorrect*} 修正成长。
 * 结束后把变化写成升级界面的 10 行文字，20 tick 后（记分项 {@code zdzttick} 为 0 时）打开升级界面。
 */
public final class LevelUp {
    /** 境界名（来源语言键 {@code levelname00}–{@code 41} 的中文；41 层以上同 41 层）。 */
    private static final List<String> REALM_NAMES = List.of("凡人",
            "练气一层", "练气二层", "练气三层", "练气四层", "练气五层", "练气六层", "练气七层", "练气八层", "练气九层", "练气十层",
            "筑基一阶", "筑基二阶", "筑基三阶", "筑基四阶", "筑基五阶", "筑基六阶", "筑基七阶", "筑基八阶", "筑基九阶", "筑基十阶",
            "结丹一转", "结丹二转", "结丹三转", "结丹四转", "结丹五转", "结丹六转", "结丹七转", "结丹八转", "结丹九转", "结丹十转",
            "元婴一重", "元婴二重", "元婴三重", "元婴四重", "元婴五重", "元婴六重", "元婴七重", "元婴八重", "元婴九重", "元婴十重",
            "化神");
    /** 升级修正与境界修正读取的属性项，与来源键的后缀一一对应。 */
    private static final List<Correction> CORRECTIONS = List.of(
            new Correction("Health", Stat.HEALTH_MAX), new Correction("Healthrestore", Stat.HEALTH_REC),
            new Correction("Power", Stat.POWER_MAX), new Correction("Powerrestore", Stat.POWER_REC),
            new Correction("Jingli", Stat.SOUL_MAX), new Correction("Jinglirestore", Stat.SOUL_REC),
            new Correction("Attack", Stat.ATTACK), new Correction("Attdef", Stat.ATTACK_DEF),
            new Correction("Manaatt", Stat.MANA_ATT), new Correction("Manadef", Stat.MANA_DEF),
            new Correction("Soulatt", Stat.SOUL_ATT), new Correction("Souldef", Stat.SOUL_DEF));
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private record Correction(String suffix, Stat stat) {
    }

    private LevelUp() {
    }

    /** 层数 20、30、40 与 41 以上修为圆满时需要渡雷劫（来源 {@code Jiancelevel}）。 */
    public static boolean needsTribulation(double level) {
        return level == 20 || level == 30 || level == 40 || level >= 41;
    }

    /** 突破所需修为 = (100 + 层数² × 100) × 五行灵根之和 × 0.2。 */
    public static double experienceMax(ServerPlayer player) {
        double roots = 0;
        for (CultivationValue root : BirthSelection.ROOTS) {
            roots += Cultivation.get(player, root);
        }
        double level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        return (100 + Math.pow(level, 2) * 100) * roots * 0.2;
    }

    public static void levelUp(ServerPlayer player) {
        for (int line = 1; line <= 10; line++) {
            Cultivation.setTizhiString(player, "uplevelxianshi" + line, "");
        }
        double skillBefore = Cultivation.get(player, CultivationValue.SKILL_POINTS);
        double lifetimeBefore = Cultivation.get(player, CultivationValue.LIFETIME);
        double[] before = new double[CORRECTIONS.size()];
        for (int i = 0; i < CORRECTIONS.size(); i++) {
            before[i] = PlayerAttributes.baseValue(player, CORRECTIONS.get(i).stat());
        }
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) <= 41) {
            Cultivation.add(player, CultivationValue.LEVEL_NUMBER, 1);
            double level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
            Cultivation.add(player, CultivationValue.SKILL_POINTS, 5 + Math.floor(level * 0.1));
            correct(player, "LevelupCorrect");
        }
        int level = (int) Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        Cultivation.set(player, CultivationText.LEVEL_NAME, REALM_NAMES.get(Math.min(level, 41)));
        String advance = "";
        switch (level) {
            case 1 -> {
                Cultivation.add(player, CultivationValue.LIFETIME, 7300);
                advance = "进阶练气";
            }
            case 11 -> {
                advance = "进阶筑基";
                Cultivation.add(player, CultivationValue.LIFETIME, 36500);
                correct(player, "RealmupCorrect");
            }
            case 21 -> {
                advance = "进阶结丹";
                Cultivation.add(player, CultivationValue.LIFETIME, 73000);
                correct(player, "RealmupCorrect");
            }
            case 31 -> {
                advance = "进阶元婴";
                Cultivation.add(player, CultivationValue.LIFETIME, 146000);
                correct(player, "RealmupCorrect");
            }
            default -> {
                if (level >= 41) {
                    if (level == 41) {
                        advance = "进阶化神";
                    }
                    Cultivation.add(player, CultivationValue.LIFETIME, level == 41 ? 292000 : 18250);
                    correct(player, "RealmupCorrect");
                }
            }
        }
        boolean realm = level == 11 || level == 21 || level == 31 || level == 41;
        player.level().playSound(null, player.blockPosition(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, realm ? "levelup" : "littlelevelup")),
                SoundSource.NEUTRAL, 1, 1);
        if (Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) >= 1) {
            Cultivation.add(player, CultivationValue.CULTIVATION_COOLDOWN, realm ? 100 : 50);
            Cultivation.setTizhiDouble(player, "XiulianColdShangxianCorrect",
                    Cultivation.tizhiDouble(player, "XiulianColdShangxianCorrect") + (realm ? 50 : 5));
        }
        Cultivation.setTizhiString(player, "uplevelxianshi1", advance.isEmpty() ? "境界提升" : advance);
        Cultivation.setTizhiString(player, "uplevelxianshi2", WHOLE.format(level - 1) + "→" + WHOLE.format(level));
        Cultivation.setTizhiString(player, "uplevelxianshi3", "属性点获得：" + WHOLE.format(Cultivation.get(player, CultivationValue.SKILL_POINTS) - skillBefore));
        Cultivation.setTizhiString(player, "uplevelxianshi4", "寿元增加：" + WHOLE.format(Cultivation.get(player, CultivationValue.LIFETIME) - lifetimeBefore));
        // 来源第 9 行法术防御增加时写的是“旧值 − 新值”，第 10 行神识的两项沿用了“物理攻击”“物理防御”的标签（Q22）。
        String[][] labels = {{"血量上限", "血量恢复"}, {"灵力上限", "灵力恢复"}, {"精力上限", "精力恢复"},
                {"物理攻击", "物理防御"}, {"法术攻击", "法术防御"}, {"物理攻击", "物理防御"}};
        for (int pair = 0; pair < 6; pair++) {
            String text = change(labels[pair][0], before[pair * 2], PlayerAttributes.baseValue(player, CORRECTIONS.get(pair * 2).stat()), false);
            String second = change(labels[pair][1], before[pair * 2 + 1], PlayerAttributes.baseValue(player, CORRECTIONS.get(pair * 2 + 1).stat()), pair == 4);
            Cultivation.setTizhiString(player, "uplevelxianshi" + (pair + 5), second.isEmpty() ? text : text + " " + second);
        }
        if (score(player, "zdzttick") == 0) {
            Delayed.run(20, () -> openScreen(player));
        }
    }

    private static String change(String label, double before, double after, boolean reversedIncrease) {
        if (before > after) {
            return "§f" + label + ":§4-" + WHOLE.format(before - after);
        }
        if (before == after) {
            return "";
        }
        return "§f" + label + ":§a+" + WHOLE.format(reversedIncrease ? before - after : after - before);
    }

    /** 把 {@code tizhiitem} 中前缀为 {@code prefix} 的修正加到成长、寿元、属性点、修为效率与道途上。 */
    private static void correct(ServerPlayer player, String prefix) {
        for (Correction correction : CORRECTIONS) {
            double amount = Cultivation.tizhiDouble(player, prefix + correction.suffix());
            if (amount != 0) {
                Cultivation.addGrowth(player, correction.stat(), amount);
            }
        }
        addIfSet(player, prefix + "Shouyvan", CultivationValue.LIFETIME);
        double lifetimePercentage = Cultivation.tizhiDouble(player, prefix + "ShouyvanPercentage");
        if (lifetimePercentage != 0) {
            Cultivation.set(player, CultivationValue.LIFETIME, Cultivation.get(player, CultivationValue.LIFETIME) * (lifetimePercentage + 1));
        }
        addIfSet(player, prefix + "Skillnumber", CultivationValue.SKILL_POINTS);
        addIfSet(player, prefix + "Xiuweixishou", CultivationValue.EXPERIENCE_RATE);
        addIfSet(player, prefix + "Daotu", CultivationValue.EXPERIENCE_HINDER);
    }

    private static void addIfSet(ServerPlayer player, String key, CultivationValue value) {
        double amount = Cultivation.tizhiDouble(player, key);
        if (amount != 0) {
            Cultivation.add(player, value, amount);
        }
    }

    static int score(ServerPlayer player, String objectiveName) {
        Objective objective = player.getScoreboard().getObjective(objectiveName);
        return objective == null ? 0 : player.getScoreboard().getOrCreatePlayerScore(player, objective).get();
    }

    private static void openScreen(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new LevelUpMenu(id, inventory), Component.literal("Playershengjixianshi")));
    }
}
