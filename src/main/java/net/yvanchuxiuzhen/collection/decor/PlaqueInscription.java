package net.yvanchuxiuzhen.collection.decor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 牌位物品带字收回时保存的铭文、朝向与逐字偏移（{@code yvanchuxiuzhen:plaque_inscription}）。 */
public record PlaqueInscription(String text, String direction, List<Glyph> glyphs) {
    public static final PlaqueInscription EMPTY = new PlaqueInscription("", "", List.of());
    public static final Codec<PlaqueInscription> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("text", "").forGetter(PlaqueInscription::text),
            Codec.STRING.optionalFieldOf("direction", "").forGetter(PlaqueInscription::direction),
            Glyph.CODEC.listOf().optionalFieldOf("glyphs", List.of()).forGetter(PlaqueInscription::glyphs)
    ).apply(instance, PlaqueInscription::new));
    public static final StreamCodec<ByteBuf, PlaqueInscription> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public PlaqueInscription {
        glyphs = List.copyOf(glyphs);
    }

    public record Glyph(String glyph, double offset) {
        public static final Codec<Glyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("glyph", "").forGetter(Glyph::glyph),
                Codec.DOUBLE.optionalFieldOf("offset", 0.0).forGetter(Glyph::offset)
        ).apply(instance, Glyph::new));
    }
}
