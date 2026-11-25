package net.yvanchuxiuzhen.system.skill.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 身法按键双击窗口与共享冷却。 */
public record SkillMovementState(
        double wWindow,
        double sWindow,
        double aWindow,
        double dWindow,
        double jumpWindow,
        double cooldownTicks
) {
    public static final SkillMovementState EMPTY = new SkillMovementState(0, 0, 0, 0, 0, 0);

    public static final Codec<SkillMovementState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("w_window", 0.0).forGetter(SkillMovementState::wWindow),
            Codec.DOUBLE.optionalFieldOf("s_window", 0.0).forGetter(SkillMovementState::sWindow),
            Codec.DOUBLE.optionalFieldOf("a_window", 0.0).forGetter(SkillMovementState::aWindow),
            Codec.DOUBLE.optionalFieldOf("d_window", 0.0).forGetter(SkillMovementState::dWindow),
            Codec.DOUBLE.optionalFieldOf("jump_window", 0.0).forGetter(SkillMovementState::jumpWindow),
            Codec.DOUBLE.optionalFieldOf("cooldown_ticks", 0.0).forGetter(SkillMovementState::cooldownTicks)
    ).apply(instance, SkillMovementState::new));

    public SkillMovementState withWindow(String key, double value) {
        return switch (key) {
            case "W" -> new SkillMovementState(value, sWindow, aWindow, dWindow, jumpWindow, cooldownTicks);
            case "S" -> new SkillMovementState(wWindow, value, aWindow, dWindow, jumpWindow, cooldownTicks);
            case "A" -> new SkillMovementState(wWindow, sWindow, value, dWindow, jumpWindow, cooldownTicks);
            case "D" -> new SkillMovementState(wWindow, sWindow, aWindow, value, jumpWindow, cooldownTicks);
            case "JUMP" -> new SkillMovementState(wWindow, sWindow, aWindow, dWindow, value, cooldownTicks);
            default -> throw new IllegalArgumentException("未知身法窗口：" + key);
        };
    }

    public double window(String key) {
        return switch (key) {
            case "W" -> wWindow;
            case "S" -> sWindow;
            case "A" -> aWindow;
            case "D" -> dWindow;
            case "JUMP" -> jumpWindow;
            default -> throw new IllegalArgumentException("未知身法窗口：" + key);
        };
    }

    public SkillMovementState withCooldownTicks(double value) {
        return new SkillMovementState(wWindow, sWindow, aWindow, dWindow, jumpWindow, value);
    }
}
