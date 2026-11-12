package net.yvanchuxiuzhen.system.world.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.yvanchuxiuzhen.system.world.terrain.mixin.BlendedNoiseAccessor;

/** 保留来源BlendedNoise的单元采样与乘除次序；内层八度噪声由RandomState注入种子。 */
public record LegacyBlendedNoise(DensityFunction argument, int cellWidth, int cellHeight, double sourceYScale)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<LegacyBlendedNoise> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(LegacyBlendedNoise::argument),
            Codec.intRange(1, 16).fieldOf("cell_width").forGetter(LegacyBlendedNoise::cellWidth),
            Codec.intRange(1, 16).fieldOf("cell_height").forGetter(LegacyBlendedNoise::cellHeight),
            Codec.doubleRange(0.001, 1000.0).fieldOf("source_y_scale").forGetter(LegacyBlendedNoise::sourceYScale)
    ).apply(instance, LegacyBlendedNoise::new)));

    @Override
    public double compute(FunctionContext context) {
        DensityFunction input = argument;
        while (input instanceof DensityFunctions.HolderHolder holder) {
            input = holder.function().value();
        }
        BlendedNoiseAccessor noise = (BlendedNoiseAccessor) input;
        int x = Math.floorDiv(context.blockX(), cellWidth);
        int y = Math.floorDiv(context.blockY(), cellHeight);
        int z = Math.floorDiv(context.blockZ(), cellWidth);
        double xzScale = 684.412 * (noise.yvanchuxiuzhen$xzScale() * cellWidth);
        double yScale = 684.412 * sourceYScale;
        double xzMainScale = xzScale / noise.yvanchuxiuzhen$xzFactor();
        double yMainScale = yScale / noise.yvanchuxiuzhen$yFactor();
        double minimum = 0.0;
        double maximum = 0.0;
        double main = 0.0;
        double frequency = 1.0;
        for (int octave = 0; octave < 8; octave++) {
            ImprovedNoise sample = noise.yvanchuxiuzhen$mainNoise().getOctaveNoise(octave);
            if (sample != null) {
                main += sample.noise(PerlinNoise.wrap((double) x * xzMainScale * frequency),
                        PerlinNoise.wrap((double) y * yMainScale * frequency),
                        PerlinNoise.wrap((double) z * xzMainScale * frequency),
                        yMainScale * frequency, (double) y * yMainScale * frequency) / frequency;
            }
            frequency /= 2.0;
        }
        double blend = (main / 10.0 + 1.0) / 2.0;
        boolean skipMinimum = blend >= 1.0;
        boolean skipMaximum = blend <= 0.0;
        frequency = 1.0;
        for (int octave = 0; octave < 16; octave++) {
            double sampleX = PerlinNoise.wrap((double) x * xzScale * frequency);
            double sampleY = PerlinNoise.wrap((double) y * yScale * frequency);
            double sampleZ = PerlinNoise.wrap((double) z * xzScale * frequency);
            double smear = yScale * frequency;
            if (!skipMinimum) {
                ImprovedNoise sample = noise.yvanchuxiuzhen$minLimitNoise().getOctaveNoise(octave);
                if (sample != null) {
                    minimum += sample.noise(sampleX, sampleY, sampleZ, smear, (double) y * smear) / frequency;
                }
            }
            if (!skipMaximum) {
                ImprovedNoise sample = noise.yvanchuxiuzhen$maxLimitNoise().getOctaveNoise(octave);
                if (sample != null) {
                    maximum += sample.noise(sampleX, sampleY, sampleZ, smear, (double) y * smear) / frequency;
                }
            }
            frequency /= 2.0;
        }
        return Mth.clampedLerp(minimum / 512.0, maximum / 512.0, blend) / 128.0;
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new LegacyBlendedNoise(argument.mapAll(visitor), cellWidth, cellHeight, sourceYScale));
    }

    @Override
    public double minValue() {
        return -maxValue();
    }

    @Override
    public double maxValue() {
        // 来源16个全幅八度的PerlinNoise.edgeValue，保持逐项累加顺序。
        double multiplier = 684.412 * sourceYScale + 2.0;
        double factor = Math.pow(2.0, 15.0) / (Math.pow(2.0, 16.0) - 1.0);
        double maximum = 0.0;
        for (int octave = 0; octave < 16; octave++) {
            maximum += multiplier * factor;
            factor /= 2.0;
        }
        return maximum;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
