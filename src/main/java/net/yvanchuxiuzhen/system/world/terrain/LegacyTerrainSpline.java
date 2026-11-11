package net.yvanchuxiuzhen.system.world.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;

/** 按1.18.2地形采样顺序执行float坐标、山脊折叠与偏移，再转换为double密度。 */
public record LegacyTerrainSpline(DensityFunction continentalness, DensityFunction erosion, DensityFunction weirdness,
                                  CubicSpline<Point, Coordinate> spline, Kind kind, double minValue, double maxValue)
        implements DensityFunction {
    private static final Codec<CubicSpline<Point, Coordinate>> SPLINE_CODEC = CubicSpline.codec(Coordinate.CODEC);
    public static final KeyDispatchDataCodec<LegacyTerrainSpline> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("continentalness").forGetter(LegacyTerrainSpline::continentalness),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("erosion").forGetter(LegacyTerrainSpline::erosion),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("weirdness").forGetter(LegacyTerrainSpline::weirdness),
            SPLINE_CODEC.fieldOf("spline").forGetter(LegacyTerrainSpline::spline),
            Kind.CODEC.fieldOf("kind").forGetter(LegacyTerrainSpline::kind),
            Codec.DOUBLE.fieldOf("min_value").forGetter(LegacyTerrainSpline::minValue),
            Codec.DOUBLE.fieldOf("max_value").forGetter(LegacyTerrainSpline::maxValue)
    ).apply(instance, LegacyTerrainSpline::new)));

    @Override
    public double compute(FunctionContext context) {
        float continentsValue = (float) continentalness.compute(context);
        float erosionValue = (float) erosion.compute(context);
        float weirdnessValue = (float) weirdness.compute(context);
        float ridgesValue = -(Math.abs(Math.abs(weirdnessValue) - 0.6666667F) - 0.33333334F) * 3.0F;
        float value = spline.apply(new Point(continentsValue, erosionValue, ridgesValue, weirdnessValue));
        if (kind == Kind.OFFSET) {
            value += -0.50375F;
        }
        return Mth.clamp((double) value, minValue, maxValue);
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new LegacyTerrainSpline(continentalness.mapAll(visitor), erosion.mapAll(visitor),
                weirdness.mapAll(visitor), spline, kind, minValue, maxValue));
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    public record Point(float continents, float erosion, float ridges, float weirdness) {
    }

    public enum Kind implements StringRepresentable {
        OFFSET("offset"), FACTOR("factor"), JAGGEDNESS("jaggedness");

        private static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Coordinate implements StringRepresentable, ToFloatFunction<Point> {
        CONTINENTS("continents"), EROSION("erosion"), RIDGES("ridges"), WEIRDNESS("weirdness");

        private static final Codec<Coordinate> CODEC = StringRepresentable.fromEnum(Coordinate::values);
        private final String name;

        Coordinate(String name) {
            this.name = name;
        }

        @Override
        public float apply(Point point) {
            return switch (this) {
                case CONTINENTS -> point.continents();
                case EROSION -> point.erosion();
                case RIDGES -> point.ridges();
                case WEIRDNESS -> point.weirdness();
            };
        }

        @Override
        public float minValue() {
            return Float.NEGATIVE_INFINITY;
        }

        @Override
        public float maxValue() {
            return Float.POSITIVE_INFINITY;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
