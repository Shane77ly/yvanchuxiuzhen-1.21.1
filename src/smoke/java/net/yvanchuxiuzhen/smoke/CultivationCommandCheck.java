package net.yvanchuxiuzhen.smoke;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.yvanchuxiuzhen.system.xiulian.XiulianRules;
import net.yvanchuxiuzhen.system.xiulian.XiulianWorldData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 经真实共享命令检查修炼数值、灵根阴阳、五行统计和本人同步，检查后恢复原值。 */
final class CultivationCommandCheck extends SystemCheck {
    private static final String[] VALUE_WORDS = {"experience", "experiencestand", "rate", "hinder", "skillpoints",
            "vigorous", "merits", "lifetime", "liftlock"};
    private static final CultivationValue[] VALUES = {CultivationValue.EXPERIENCE, CultivationValue.EXPERIENCE_STAND,
            CultivationValue.EXPERIENCE_RATE, CultivationValue.EXPERIENCE_HINDER, CultivationValue.SKILL_POINTS,
            CultivationValue.VIGOROUS, CultivationValue.MERITS, CultivationValue.LIFETIME, CultivationValue.LIFTLOCK};
    private static final String[] ROOT_WORDS = {"bodygold", "bodywater", "bodytree", "bodyfire", "bodysoil"};
    private static final CultivationValue[] ROOTS = {CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER,
            CultivationValue.ROOT_TREE, CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL};
    private static final String[] STATISTICS = {"killentitygold", "killentitypointsgold", "killentitywater",
            "killentitypointswater", "killentitytree", "killentitypointstree", "killentityfire",
            "killentitypointsfire", "killentitysoil", "killentitypointssoil"};
    private final Map<CultivationValue, Double> original = new EnumMap<>(CultivationValue.class);
    private CompoundTag originalTizhi;
    private double originalDailyExperience;
    private int originalRootLevel;
    private boolean originalBirthSelection;
    private boolean originalDaylight;

    CultivationCommandCheck(List<String> report, String levelId) {
        super("修炼与五行管理命令", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::exercise);
            return true;
        }
        if (local < 8) return false;
        try {
            expect("层数同步本人", 2, Cultivation.get(minecraft.player, CultivationValue.LEVEL_NUMBER));
            expect("层数命令门槛同步本人", 500, Cultivation.get(minecraft.player, CultivationValue.EXPERIENCE_MAX));
            for (int index = 0; index < VALUES.length; index++) {
                expect(VALUE_WORDS[index] + "同步本人", 8.25 + index, Cultivation.get(minecraft.player, VALUES[index]));
            }
            for (int index = 0; index < ROOTS.length; index++) {
                expect(ROOT_WORDS[index] + "同步本人", 6.25, Cultivation.get(minecraft.player, ROOTS[index]));
            }
            expect("阴阳差额同步本人", 5, Cultivation.get(minecraft.player, CultivationValue.YINYANG));
            for (int index = 0; index < STATISTICS.length; index++) {
                expect(STATISTICS[index] + "同步本人", 8.25 + index, Cultivation.tizhiDouble(minecraft.player, STATISTICS[index]));
            }
        } finally {
            onServer(this::restore);
        }
        finish();
        return true;
    }

    private void exercise(ServerPlayer player) {
        for (CultivationValue value : VALUES) original.put(value, Cultivation.get(player, value));
        for (CultivationValue value : ROOTS) original.put(value, Cultivation.get(player, value));
        for (CultivationValue value : List.of(CultivationValue.LEVEL_NUMBER, CultivationValue.EXPERIENCE_MAX, CultivationValue.YINYANG)) {
            original.put(value, Cultivation.get(player, value));
        }
        GameRules rules = player.serverLevel().getGameRules();
        originalDaylight = rules.getBoolean(GameRules.RULE_DAYLIGHT);
        originalRootLevel = rules.getInt(XiulianRules.ROOT_LEVEL);
        originalBirthSelection = rules.getBoolean(XiulianRules.BIRTH_SELECTION);
        originalDailyExperience = XiulianWorldData.get(player.server).dailyExperience();
        // 每秒修炼以 dayTime 计时；暂停推进并对齐计时键，等待同步时保持命令结果。
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, player.server);
        Cultivation.editTizhi(player, tag -> {
            originalTizhi = tag.copy();
            tag.putDouble("jishiqi", player.serverLevel().getDayTime());
        });

        String realm = Cultivation.get(player, CultivationText.LEVEL_NAME);
        boolean thunder = Cultivation.thunderOpen(player);
        command(player, "levelup level set invalid");
        expect("无效层数按来源取零", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect("无效层数重算门槛", 100, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
        command(player, "levelup level set 2.5 true");
        expect("层数保留小数", 2.5, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect("小数层数立方门槛", 881.25, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
        command(player, "levelup level other -0.5");
        expect("层数非set累加负数", 2, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect("累加层数重算门槛", 500, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
        expect("层数命令保留境界名", realm, Cultivation.get(player, CultivationText.LEVEL_NAME));
        expect("层数命令不发放升级属性点", original.get(CultivationValue.SKILL_POINTS).doubleValue(),
                Cultivation.get(player, CultivationValue.SKILL_POINTS));
        expectTrue("层数命令保留雷劫开关", thunder == Cultivation.thunderOpen(player));

        command(player, "experienceup experience set invalid");
        expect("无效修为参数按来源取零", 0, Cultivation.get(player, CultivationValue.EXPERIENCE));
        for (int index = 0; index < VALUES.length; index++) {
            command(player, "experienceup " + VALUE_WORDS[index] + " set " + (10.5 + index));
            expect(VALUE_WORDS[index] + "设置小数", 10.5 + index, Cultivation.get(player, VALUES[index]));
            command(player, "experienceup " + VALUE_WORDS[index] + " minus -2.25");
            expect(VALUE_WORDS[index] + "非set累加负小数", 8.25 + index, Cultivation.get(player, VALUES[index]));
        }
        exerciseRoots(player);
        for (int index = 0; index < STATISTICS.length; index++) {
            command(player, "experienceup " + STATISTICS[index] + " set " + (10.5 + index));
            expect(STATISTICS[index] + "设置指定键", 10.5 + index, Cultivation.tizhiDouble(player, STATISTICS[index]));
            command(player, "experienceup " + STATISTICS[index] + " minus -2.25");
            expect(STATISTICS[index] + "非set累加负小数", 8.25 + index, Cultivation.tizhiDouble(player, STATISTICS[index]));
        }
        command(player, "experienceup dailyexperience set 10.5");
        expect("每日修为上限设置", 10.5, XiulianWorldData.get(player.server).dailyExperience());
        command(player, "experienceup dailyexperience add -2.25");
        expect("每日修为上限累加负小数", 8.25, XiulianWorldData.get(player.server).dailyExperience());
        command(player, "experienceup linggenlevel set 9.75");
        expect("灵根随机规则设置截断", 9, rules.getInt(XiulianRules.ROOT_LEVEL));
        command(player, "experienceup linggenlevel other -1.5");
        expect("灵根随机规则先累加再截断", 7, rules.getInt(XiulianRules.ROOT_LEVEL));
        command(player, "lock rebirthgui true");
        expectTrue("来源出身规则关闭分支", !rules.getBoolean(XiulianRules.BIRTH_SELECTION));
        command(player, "unlock rebirthgui true");
        expectTrue("来源出身规则开启分支", rules.getBoolean(XiulianRules.BIRTH_SELECTION));
    }

    private void exerciseRoots(ServerPlayer player) {
        for (CultivationValue root : ROOTS) Cultivation.set(player, root, 3);
        Cultivation.set(player, CultivationValue.YINYANG, 5);
        double[] setYinYang = {10.5, 2.75, -0.5, 7.25, 5};
        double[] addedYinYang = {8.25, 5, 1.75, 5, 5};
        for (int index = 0; index < ROOTS.length; index++) {
            command(player, "experienceup " + ROOT_WORDS[index] + " set 8.5");
            expect(ROOT_WORDS[index] + "设置小数", 8.5, Cultivation.get(player, ROOTS[index]));
            expect(ROOT_WORDS[index] + "设置按旧根值调整阴阳", setYinYang[index], Cultivation.get(player, CultivationValue.YINYANG));
            command(player, "experienceup " + ROOT_WORDS[index] + " add -2.25");
            expect(ROOT_WORDS[index] + "累加负小数", 6.25, Cultivation.get(player, ROOTS[index]));
            expect(ROOT_WORDS[index] + "累加按增量调整阴阳", addedYinYang[index], Cultivation.get(player, CultivationValue.YINYANG));
        }
    }

    private void restore(ServerPlayer player) {
        original.forEach((value, amount) -> Cultivation.set(player, value, amount));
        Cultivation.editTizhi(player, tag -> {
            restoreTag(tag, "jishiqi");
            for (String key : STATISTICS) restoreTag(tag, key);
        });
        XiulianWorldData.get(player.server).setDailyExperience(originalDailyExperience);
        GameRules rules = player.serverLevel().getGameRules();
        rules.getRule(XiulianRules.ROOT_LEVEL).set(originalRootLevel, player.server);
        rules.getRule(XiulianRules.BIRTH_SELECTION).set(originalBirthSelection, player.server);
        rules.getRule(GameRules.RULE_DAYLIGHT).set(originalDaylight, player.server);
    }

    private void restoreTag(CompoundTag tag, String key) {
        if (originalTizhi.contains(key)) {
            tag.put(key, originalTizhi.get(key).copy());
        } else {
            tag.remove(key);
        }
    }

    private static void command(ServerPlayer player, String arguments) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),
                "yvanchu " + player.getDisplayName().getString() + " " + arguments);
    }
}
