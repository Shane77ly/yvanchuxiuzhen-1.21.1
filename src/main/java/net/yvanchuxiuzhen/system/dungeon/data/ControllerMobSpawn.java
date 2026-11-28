package net.yvanchuxiuzhen.system.dungeon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/** 控制器展开波次后待生成的单个妖兽实例条目。 */
public record ControllerMobSpawn(
        ResourceLocation entityId,
        int level,
        int x,
        int y,
        int z,
        int facing,
        boolean attack
) {
    public static final Codec<ControllerMobSpawn> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity_id").forGetter(ControllerMobSpawn::entityId),
            Codec.INT.fieldOf("level").forGetter(ControllerMobSpawn::level),
            Codec.INT.fieldOf("x").forGetter(ControllerMobSpawn::x),
            Codec.INT.fieldOf("y").forGetter(ControllerMobSpawn::y),
            Codec.INT.fieldOf("z").forGetter(ControllerMobSpawn::z),
            Codec.INT.fieldOf("facing").forGetter(ControllerMobSpawn::facing),
            Codec.BOOL.optionalFieldOf("attack", false).forGetter(ControllerMobSpawn::attack)
    ).apply(instance, ControllerMobSpawn::new));
}
