package net.yvanchuxiuzhen.system.world.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.yvanchuxiuzhen.system.world.terrain.mixin.ChunkGeneratorStructureStateMixin;

/** 保存来源独立地形种子；世界装饰、随机散布结构和Carver仍沿用外部世界种子。 */
public final class LegacyDimensionChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<LegacyDimensionChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(LegacyDimensionChunkGenerator::getBiomeSource),
            Codec.LONG.fieldOf("seed").forGetter(LegacyDimensionChunkGenerator::terrainSeed),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(LegacyDimensionChunkGenerator::generatorSettings)
    ).apply(instance, LegacyDimensionChunkGenerator::new));

    private final long terrainSeed;

    public LegacyDimensionChunkGenerator(BiomeSource biomeSource, long terrainSeed, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.terrainSeed = terrainSeed;
    }

    public long terrainSeed() {
        return terrainSeed;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long worldSeed) {
        ChunkGeneratorStructureState state = super.createState(structureSets, randomState, worldSeed);
        ((ChunkGeneratorStructureStateMixin) state).yvanchuxiuzhen$setConcentricRingsSeed(terrainSeed);
        return state;
    }
}
