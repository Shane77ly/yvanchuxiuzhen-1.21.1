package net.yvanchuxiuzhen.system.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

/** 来源贴图粒子的构造参数与帧推进；颜色、透明度、阻尼和生命周期由原生基类承担。 */
public record ParticleRow(String id, float width, float height, float scale, int lifetime, int lifetimeJitter,
                          float gravity, boolean physics, double speed, float angularVelocity, float angularAcceleration,
                          Layer render, boolean fullBright, int frames, int frameTicks, boolean alwaysShow) {
    public static final Codec<ParticleRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(ParticleRow::id),
            Codec.FLOAT.fieldOf("width").forGetter(ParticleRow::width),
            Codec.FLOAT.fieldOf("height").forGetter(ParticleRow::height),
            Codec.FLOAT.fieldOf("scale").forGetter(ParticleRow::scale),
            Codec.INT.fieldOf("lifetime").forGetter(ParticleRow::lifetime),
            Codec.INT.fieldOf("lifetime_jitter").forGetter(ParticleRow::lifetimeJitter),
            Codec.FLOAT.fieldOf("gravity").forGetter(ParticleRow::gravity),
            Codec.BOOL.fieldOf("physics").forGetter(ParticleRow::physics),
            Codec.DOUBLE.fieldOf("speed").forGetter(ParticleRow::speed),
            Codec.FLOAT.fieldOf("angular_velocity").forGetter(ParticleRow::angularVelocity),
            Codec.FLOAT.fieldOf("angular_acceleration").forGetter(ParticleRow::angularAcceleration),
            Layer.CODEC.fieldOf("render").forGetter(ParticleRow::render),
            Codec.BOOL.fieldOf("full_bright").forGetter(ParticleRow::fullBright),
            Codec.INT.fieldOf("frames").forGetter(ParticleRow::frames),
            Codec.INT.fieldOf("frame_ticks").forGetter(ParticleRow::frameTicks),
            Codec.BOOL.fieldOf("always_show").forGetter(ParticleRow::alwaysShow)
    ).apply(instance, ParticleRow::new));

    public enum Layer implements StringRepresentable {
        OPAQUE("opaque"), LIT("lit"), TRANSLUCENT("translucent");

        public static final Codec<Layer> CODEC = StringRepresentable.fromEnum(Layer::values);
        private final String name;

        Layer(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
