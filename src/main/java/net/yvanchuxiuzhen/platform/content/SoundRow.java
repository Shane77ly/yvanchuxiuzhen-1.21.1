package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 声音事件内容表的一行（{@code content/<单位>/sounds.json}）：只有事件 ID；声音文件、类别与字幕由
 * {@code src/main/sounds/<单位>.json} 片段声明。
 */
public record SoundRow(String id) {
    public static final Codec<SoundRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(SoundRow::id)
    ).apply(instance, SoundRow::new));
}
