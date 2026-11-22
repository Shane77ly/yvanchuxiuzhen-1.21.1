package net.yvanchuxiuzhen.system.material.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 已辨识效果的只读视图，对应来源 yaoxiao、mingmingkongjian、open 与管理解锁写入的介绍。 */
public record MaterialRevealedEntry(String namespace, String name, boolean open, String description) {
    public static final MaterialRevealedEntry EMPTY = new MaterialRevealedEntry("", "", false, "");
    public static final Codec<MaterialRevealedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("namespace").forGetter(MaterialRevealedEntry::namespace),
            Codec.STRING.fieldOf("name").forGetter(MaterialRevealedEntry::name),
            Codec.BOOL.fieldOf("open").forGetter(MaterialRevealedEntry::open),
            Codec.STRING.optionalFieldOf("description", "").forGetter(MaterialRevealedEntry::description)
    ).apply(instance, MaterialRevealedEntry::new));
}
