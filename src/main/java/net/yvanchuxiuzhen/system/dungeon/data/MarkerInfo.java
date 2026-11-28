package net.yvanchuxiuzhen.system.dungeon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 副本占位符位置、朝向与攻击标记，保存在控制器与占位符方块实体中。 */
public record MarkerInfo(String id, int x, int y, int z, int facing, boolean attack) {
    public static final MarkerInfo EMPTY = new MarkerInfo("", 0, 0, 0, 0, false);

    public static final Codec<MarkerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(MarkerInfo::id),
            Codec.INT.fieldOf("x").forGetter(MarkerInfo::x),
            Codec.INT.fieldOf("y").forGetter(MarkerInfo::y),
            Codec.INT.fieldOf("z").forGetter(MarkerInfo::z),
            Codec.INT.fieldOf("facing").forGetter(MarkerInfo::facing),
            Codec.BOOL.optionalFieldOf("attack", false).forGetter(MarkerInfo::attack)
    ).apply(instance, MarkerInfo::new));
}
