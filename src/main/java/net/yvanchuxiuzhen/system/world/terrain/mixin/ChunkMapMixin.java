package net.yvanchuxiuzhen.system.world.terrain.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.yvanchuxiuzhen.system.world.DifuStructureNotifications;
import net.yvanchuxiuzhen.system.world.terrain.DifuProtoChunk;
import net.yvanchuxiuzhen.system.world.terrain.LegacyDimensionChunkGenerator;
import net.yvanchuxiuzhen.system.world.diagnostic.WorldStopRequeueDiagnostics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @WrapOperation(method = "createEmptyChunk", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/ProtoChunk"))
    private ProtoChunk yvanchuxiuzhen$emptyDifuChunk(ChunkPos pos, UpgradeData upgrade, LevelHeightAccessor height,
            Registry<Biome> biomes, BlendingData blending, Operation<ProtoChunk> original) {
        if (((DifuStructureNotifications) level).yvanchuxiuzhen$difuPlacementActive()) {
            return new DifuProtoChunk(pos, upgrade, height, biomes, blending);
        }
        return original.call(pos, upgrade, height, biomes, blending);
    }

    @Inject(method = "lambda$scheduleUnload$12", at = @At("HEAD"), require = 1)
    private void yvanchuxiuzhen$observeScheduleUnload(ChunkHolder holder, long chunkPos, CallbackInfo callback) {
        WorldStopRequeueDiagnostics.observe((ChunkMap) (Object) this, holder, chunkPos);
    }

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
