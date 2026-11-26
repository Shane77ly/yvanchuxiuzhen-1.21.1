package net.yvanchuxiuzhen.system.world.terrain;

import java.util.Arrays;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.ProtoChunkTicks;

/** 地府首入邻域的新区块。只有结构起点及引用的外围区块不需要先建立全部空分段。 */
public final class DifuProtoChunk extends ProtoChunk {
    private final Registry<Biome> biomeRegistry;
    private volatile boolean materialized;

    public DifuProtoChunk(ChunkPos pos, UpgradeData upgrade, LevelHeightAccessor height, Registry<Biome> biomes, BlendingData blending) {
        super(pos, upgrade, emptySections(height, biomes), new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), height, biomes, blending);
        biomeRegistry = biomes;
    }

    private static LevelChunkSection[] emptySections(LevelHeightAccessor height, Registry<Biome> biomes) {
        var sections = new LevelChunkSection[height.getSectionsCount()];
        Arrays.fill(sections, new LevelChunkSection(biomes));
        return sections;
    }

    @Override
    public LevelChunkSection[] getSections() {
        if (!materialized) materialize();
        return super.getSections();
    }

    // ChunkAccess 在此入口直接访问 sections 数组，其他分段访问均经 getSections。
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z) {
        if (!materialized) materialize();
        return super.getNoiseBiome(x, y, z);
    }

    private synchronized void materialize() {
        if (materialized) return;
        for (int i = 1; i < sections.length; i++) sections[i] = new LevelChunkSection(biomeRegistry);
        materialized = true;
    }
}
