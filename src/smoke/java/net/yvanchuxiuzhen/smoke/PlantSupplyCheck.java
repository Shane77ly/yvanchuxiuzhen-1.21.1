package net.yvanchuxiuzhen.smoke;

import com.mojang.datafixers.util.Pair;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.terrain.LegacyDimensionChunkGenerator;

/** 用人间道新生成的少量区块定位桑叶、桃叶和成熟水稻的真实采收坐标。 */
final class PlantSupplyCheck extends SystemCheck {
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian"));
    private static final List<Supply> SUPPLIES = List.of(
            new Supply("biomewind_02", "mulberryleaves", false),
            new Supply("biomepeach_01", "peachleaves", false),
            new Supply("biomeautumn_01", "shicai_001_plant_08", true));
    private final List<String> report;
    private int sampled;

    PlantSupplyCheck(List<String> report, String levelId) {
        super("人间自然植物供给", report, levelId);
        this.report = report;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(player -> {
                if (player.server.getLevel(RENJIAN) == null || player.level().dimension().equals(RENJIAN)
                        || Files.exists(player.server.getWorldPath(LevelResource.ROOT)
                                .resolve("dimensions/yvanchuxiuzhen/dimensionrenjian/region"))) {
                    fail("本场景需要从未生成过人间道区块的新档");
                    finish();
                }
            });
        } else if (step <= SUPPLIES.size()) {
            onServer(player -> locate(player, SUPPLIES.get(step - 1)));
        } else {
            expectTrue("至少取得一个人间道自然 FULL 区块", sampled > 0);
            finish();
        }
        return true;
    }

    private void locate(ServerPlayer player, Supply supply) {
        ServerLevel level = player.server.getLevel(RENJIAN);
        LegacyDimensionChunkGenerator generator = (LegacyDimensionChunkGenerator) level.getChunkSource().getGenerator();
        ResourceLocation biome = id(supply.biome());
        Pair<BlockPos, Holder<Biome>> target = level.findClosestBiome3d(candidate -> candidate.is(biome),
                new BlockPos(4096, 72, 4096), 4096, 32, 32);
        if (target == null) {
            report.add("INFO 人间自然植物供给 未覆盖：有界气候搜索未找到 " + biome);
            return;
        }
        Block block = BuiltInRegistries.BLOCK.get(id(supply.block()));
        ChunkPos center = new ChunkPos(target.getFirst());
        for (ChunkPos pos : List.of(center, new ChunkPos(center.x + 1, center.z),
                new ChunkPos(center.x, center.z + 1), new ChunkPos(center.x - 1, center.z))) {
            if (level.getChunkSource().getChunkNow(pos.x, pos.z) != null) continue;
            LevelChunk chunk = level.getChunk(pos.x, pos.z);
            sampled++;
            for (BlockPos point : BlockPos.betweenClosed(pos.getMinBlockX(), level.getMinBuildHeight(), pos.getMinBlockZ(),
                    pos.getMaxBlockX(), level.getMaxBuildHeight() - 1, pos.getMaxBlockZ())) {
                BlockState state = chunk.getBlockState(point);
                if (!state.is(block) || !level.getBiome(point).is(biome)) continue;
                if (supply.doublePlant() && state.getValue(DoublePlantBlock.HALF) != DoubleBlockHalf.LOWER) continue;
                expectTrue("自然生成状态可存活 " + supply.block(), state.canSurvive(level, point));
                if (supply.doublePlant()) {
                    BlockState upper = chunk.getBlockState(point.above());
                    expectTrue("成熟水稻有原生上半格",
                            upper.is(block) && upper.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER);
                }
                report.add("INFO 人间自然植物供给 世界种子=" + level.getSeed() + " 地形种子=" + generator.terrainSeed() + " 群系=" + biome
                        + " 区块=" + pos + " 方块=" + supply.block() + " 生存采收坐标=" + point.toShortString());
                return;
            }
            report.add("INFO 人间自然植物供给 种子=" + level.getSeed() + " 区块=" + pos + " 未命中 " + supply.block());
        }
        report.add("INFO 人间自然植物供给 未覆盖：在有限新区块中未采到 " + supply.block());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private record Supply(String biome, String block, boolean doublePlant) {
    }
}
