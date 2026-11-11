package net.yvanchuxiuzhen.system.world.terrain;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源维度的独立种子、float地形样条与单元噪声类型。 */
public final class WorldTerrainTypes {
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, ModId.VALUE);
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, ModId.VALUE);

    static {
        DENSITY_TYPES.register("legacy_terrain_spline", () -> LegacyTerrainSpline.CODEC.codec());
        DENSITY_TYPES.register("legacy_blended_noise", () -> LegacyBlendedNoise.CODEC.codec());
        GENERATORS.register("legacy_dimension", () -> LegacyDimensionChunkGenerator.CODEC);
    }

    private WorldTerrainTypes() {
    }

    public static void register(IEventBus modBus) {
        DENSITY_TYPES.register(modBus);
        GENERATORS.register(modBus);
    }
}
