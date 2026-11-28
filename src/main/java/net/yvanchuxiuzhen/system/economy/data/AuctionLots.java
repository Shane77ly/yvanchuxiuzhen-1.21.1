package net.yvanchuxiuzhen.system.economy.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 拍卖拍品元数据列表（{@code yvanchuxiuzhen:auction_lots}）。 */
public record AuctionLots(List<Entry> entries) {
    public static final AuctionLots EMPTY = new AuctionLots(List.of());
    public static final Codec<AuctionLots> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(AuctionLots::entries)
    ).apply(instance, AuctionLots::new));
    public static final StreamCodec<ByteBuf, AuctionLots> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public AuctionLots {
        entries = List.copyOf(entries);
    }

    public Entry lot(int oneBasedIndex) {
        int index = oneBasedIndex - 1;
        return index >= 0 && index < entries.size() ? entries.get(index) : Entry.EMPTY;
    }

    public record Entry(String type, int startingPrice, int level) {
        public static final Entry EMPTY = new Entry("", 0, 0);
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("type", "").forGetter(Entry::type),
                Codec.INT.optionalFieldOf("starting_price", 0).forGetter(Entry::startingPrice),
                Codec.INT.optionalFieldOf("level", 0).forGetter(Entry::level)
        ).apply(instance, Entry::new));
    }
}
