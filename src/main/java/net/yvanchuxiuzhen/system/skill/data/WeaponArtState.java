package net.yvanchuxiuzhen.system.skill.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 武器技连招式数、连击窗口与出手冷却。 */
public record WeaponArtState(
        double step,
        double windowTicks,
        double cooldownTicks
) {
    public static final WeaponArtState EMPTY = new WeaponArtState(0, 0, 0);

    public static final Codec<WeaponArtState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("step", 0.0).forGetter(WeaponArtState::step),
            Codec.DOUBLE.optionalFieldOf("window_ticks", 0.0).forGetter(WeaponArtState::windowTicks),
            Codec.DOUBLE.optionalFieldOf("cooldown_ticks", 0.0).forGetter(WeaponArtState::cooldownTicks)
    ).apply(instance, WeaponArtState::new));
}
