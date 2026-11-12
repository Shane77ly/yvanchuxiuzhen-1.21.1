package net.yvanchuxiuzhen.system.world.terrain.mixin;

import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 只读复用RandomState已播种的八度采样器，保持来源密度运算次序。 */
@Mixin(BlendedNoise.class)
public interface BlendedNoiseAccessor {
    @Accessor("minLimitNoise")
    PerlinNoise yvanchuxiuzhen$minLimitNoise();

    @Accessor("maxLimitNoise")
    PerlinNoise yvanchuxiuzhen$maxLimitNoise();

    @Accessor("mainNoise")
    PerlinNoise yvanchuxiuzhen$mainNoise();

    @Accessor("xzScale")
    double yvanchuxiuzhen$xzScale();

    @Accessor("xzFactor")
    double yvanchuxiuzhen$xzFactor();

    @Accessor("yFactor")
    double yvanchuxiuzhen$yFactor();
}
