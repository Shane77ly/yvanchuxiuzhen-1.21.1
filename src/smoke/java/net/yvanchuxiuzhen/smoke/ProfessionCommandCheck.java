package net.yvanchuxiuzhen.smoke;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.world.WorldGeneration;

/** 经真实共享命令分派检查十职业参数、同步和原世界开关，避免命令节点覆盖。 */
final class ProfessionCommandCheck extends SystemCheck {
    private static final String[] WORDS = {"alchemy", "refiner", "rune", "collection", "cooking",
            "puppet", "breed", "ghost", "insect", "blood"};
    private final Map<Profession, double[]> original = new EnumMap<>(Profession.class);
    private boolean structuresAllowed;

    ProfessionCommandCheck(List<String> report, String levelId) {
        super("职业管理命令", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::exercise);
            return true;
        }
        if (local < 8) return false;
        onServer(player -> {
            for (Profession profession : Profession.values()) {
                expectTrue(profession.displayName() + "命令结果同步本人且上限未联动",
                        Professions.level(minecraft.player, profession) == 2
                                && Professions.experience(minecraft.player, profession) == 8.25
                                && Professions.experienceMax(player, profession) == original.get(profession)[2]);
                Professions.setLevel(player, profession, original.get(profession)[0]);
                Professions.setExperience(player, profession, original.get(profession)[1]);
            }
            WorldGeneration.setRandomStructuresAllowed(player.server, structuresAllowed);
        });
        finish();
        return true;
    }

    private void exercise(ServerPlayer player) {
        for (Profession profession : Profession.values()) {
            original.put(profession, new double[]{Professions.level(player, profession),
                    Professions.experience(player, profession), Professions.experienceMax(player, profession)});
        }
        for (int index = 0; index < WORDS.length; index++) {
            Profession profession = Profession.values()[index];
            command(player, "levelup " + WORDS[index] + " set 2.5");
            expect(profession.displayName() + "等级命令保留小数", 2.5, Professions.level(player, profession));
            command(player, "levelup " + WORDS[index] + " other -0.5");
            expect(profession.displayName() + "非set分支累加负数", 2, Professions.level(player, profession));
            command(player, "experienceup " + WORDS[index] + " set invalid");
            expect(profession.displayName() + "无效经验参数按来源取零", 0, Professions.experience(player, profession));
            command(player, "experienceup " + WORDS[index] + " set 10.5");
            command(player, "experienceup " + WORDS[index] + " minus -2.25");
            expect(profession.displayName() + "经验命令累加负小数", 8.25, Professions.experience(player, profession));
        }
        expect("采集等级命令维护来源持久键", 2, player.getPersistentData().getDouble("collection_LEVEL"));
        structuresAllowed = WorldGeneration.randomStructuresAllowed(player.server);
        command(player, "lock jiegoushengcheng true");
        expectTrue("共享分派保留世界结构关闭命令", !WorldGeneration.randomStructuresAllowed(player.server));
        command(player, "unlock jiegoushengcheng true");
        expectTrue("共享分派保留世界结构开启命令", WorldGeneration.randomStructuresAllowed(player.server));
    }

    private static void command(ServerPlayer player, String arguments) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),
                "yvanchu " + player.getDisplayName().getString() + " " + arguments);
    }
}
