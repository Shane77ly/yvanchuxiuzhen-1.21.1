package net.yvanchuxiuzhen.system.world.terrain.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.yvanchuxiuzhen.system.world.terrain.LegacyDimensionChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 在结构状态首次读取前替换专属生成器的共享RandomState，使全部查询使用同一种子。 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    @Mutable
    private RandomState randomState;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/chunk/ChunkGenerator;createState(Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/level/levelgen/RandomState;J)Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;"), require = 1)
    private ChunkGeneratorStructureState yvanchuxiuzhen$createSeededState(ChunkGenerator generator,
            HolderLookup<StructureSet> structureSets, RandomState original, long worldSeed) {
        if (generator instanceof LegacyDimensionChunkGenerator legacy) {
            randomState = RandomState.create(legacy.generatorSettings().value(),
                    level.registryAccess().lookupOrThrow(Registries.NOISE), legacy.terrainSeed());
            return generator.createState(structureSets, randomState, worldSeed);
        }
        return generator.createState(structureSets, original, worldSeed);
    }
}
