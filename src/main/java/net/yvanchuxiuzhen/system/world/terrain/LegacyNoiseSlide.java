package net.yvanchuxiuzhen.system.world.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** 两端滑移共用同一输入密度图；整数高度和中间插值沿用来源运算顺序。 */
public record LegacyNoiseSlide(DensityFunction input, int cellHeight, int minCell, int cells, Slider top, Slider bottom)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<LegacyNoiseSlide> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(i -> i.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(LegacyNoiseSlide::input),
            Codec.intRange(1, 256).fieldOf("cell_height").forGetter(LegacyNoiseSlide::cellHeight),
            Codec.INT.fieldOf("min_cell").forGetter(LegacyNoiseSlide::minCell), Codec.INT.fieldOf("cells").forGetter(LegacyNoiseSlide::cells),
            Slider.CODEC.fieldOf("top").forGetter(LegacyNoiseSlide::top), Slider.CODEC.fieldOf("bottom").forGetter(LegacyNoiseSlide::bottom)
    ).apply(i, LegacyNoiseSlide::new)));

    public record Slider(double target, int size, int offset) {
        private static final Codec<Slider> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.fieldOf("target").forGetter(Slider::target), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("size").forGetter(Slider::size),
                Codec.INT.fieldOf("offset").forGetter(Slider::offset)).apply(i, Slider::new));

        private double weight(int index) { return size == 0 ? 1.0 : (index - (double) offset) / size; }
        private double apply(double weight, double value) { return weight >= 1 ? value : target + weight * (value - target); }
    }

    @Override public double compute(FunctionContext context) {
        int cell = context.blockY() / cellHeight - minCell;
        double low = bottom.weight(cell);
        if (low <= 0) return bottom.target();
        double high = top.weight(cells - cell);
        return bottom.apply(low, high <= 0 ? top.target() : top.apply(high, input.compute(context)));
    }

    @Override public void fillArray(double[] array, ContextProvider context) { context.fillAllDirectly(array, this); }
    @Override public double minValue() { return Math.min(input.minValue(), Math.min(top.target(), bottom.target())); }
    @Override public double maxValue() { return Math.max(input.maxValue(), Math.max(top.target(), bottom.target())); }
    @Override public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new LegacyNoiseSlide(input.mapAll(visitor), cellHeight, minCell, cells, top, bottom));
    }
    @Override public KeyDispatchDataCodec<? extends DensityFunction> codec() { return CODEC; }
}
