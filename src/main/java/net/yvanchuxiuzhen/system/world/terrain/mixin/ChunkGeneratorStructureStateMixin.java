package net.yvanchuxiuzhen.system.world.terrain.mixin;

import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 同心环位置沿用来源生成器种子，levelSeed继续由世界提供。 */
@Mixin(ChunkGeneratorStructureState.class)
public interface ChunkGeneratorStructureStateMixin {
    @Mutable
    @Accessor("concentricRingsSeed")
    void yvanchuxiuzhen$setConcentricRingsSeed(long seed);
}
