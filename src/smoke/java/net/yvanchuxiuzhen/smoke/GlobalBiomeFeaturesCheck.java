package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.GenerationStep;

/** 全群系共享矿物和植物入口及当前维度地物排序的定向检查。 */
final class GlobalBiomeFeaturesCheck extends SystemCheck {
    private static final List<String> ORES = List.of(
            "cailiao_191ore", "cailiao_192ore", "cailiao_193ore", "cailiao_194ore", "cailiao_195ore",
            "cailiao_196ore", "cailiao_197ore", "cailiao_198ore", "cailiao_199ore", "cailiao_20101",
            "cailiao_20301", "cailiao_20501", "cailiao_20701", "cailiao_20801", "cailiao_20901",
            "cailiao_21101", "cailiao_21301", "cailiao_21501", "cailiao_21601", "cailiao_21701",
            "cailiao_21901", "cailiao_16101", "fuluzhushablock", "cailiao_16201", "cailiao_16301",
            "cailiao_16401", "cailiao_16501", "cailiao_16601", "cailiao_16701", "cailiao_16801", "cailiao_16901");
    private boolean executed;

    GlobalBiomeFeaturesCheck(List<String> report, String levelId) {
        super("全群系地物", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (!executed) {
            executed = true;
            onServer(this::check);
        }
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        var biomes = player.server.registryAccess().registryOrThrow(Registries.BIOME);
        int checked = 0;
        for (var biome : biomes.holders().toList()) {
            var features = biome.value().getGenerationSettings().features();
            for (String ore : ORES) {
                var id = ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", ore);
                int stage = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
                long count = stage < features.size() ? features.get(stage).stream().filter(holder -> holder.is(id)).count() : 0;
                if (count != 1) throw new IllegalStateException(biome.unwrapKey() + " 地物 " + id + " 次数=" + count);
            }
            var grass = ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "nichanggrass_06");
            int stage = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
            long count = stage < features.size() ? features.get(stage).stream().filter(holder -> holder.is(grass)).count() : 0;
            if (count != 1) throw new IllegalStateException(biome.unwrapKey() + " 地物 " + grass + " 次数=" + count);
            checked++;
        }
        expectTrue("所有已加载群系在对应阶段各含一次31矿和霓裳草（" + checked + "群系）", checked > 0);
        int dimensions = 0;
        for (ServerLevel level : player.server.getAllLevels()) {
            var source = level.getChunkSource().getGenerator().getBiomeSource();
            FeatureSorter.buildFeaturesPerStep(List.copyOf(source.possibleBiomes()),
                    holder -> holder.value().getGenerationSettings().features(), true);
            dimensions++;
        }
        expectTrue("全部当前维度群系的地物排序无环（" + dimensions + "维度）", dimensions > 0);
    }
}
