package net.yvanchuxiuzhen.system.economy.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 拍卖令牌记录的拍卖场标识与已放置设施坐标（索引 0 为拍卖台，后续为座位）。 */
public record AuctionTokenData(String auctionUuid, int placedCount, List<BlockPos> seats) {
    public static final AuctionTokenData EMPTY = new AuctionTokenData("", 0, List.of());
    public static final Codec<AuctionTokenData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("auction_uuid", "").forGetter(AuctionTokenData::auctionUuid),
            Codec.INT.optionalFieldOf("placed_count", 0).forGetter(AuctionTokenData::placedCount),
            BlockPos.CODEC.listOf().optionalFieldOf("seats", List.of()).forGetter(AuctionTokenData::seats)
    ).apply(instance, AuctionTokenData::new));
    public static final StreamCodec<ByteBuf, AuctionTokenData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public AuctionTokenData {
        seats = List.copyOf(seats);
    }

    public BlockPos seat(int oneBasedIndex) {
        int index = oneBasedIndex - 1;
        return index >= 0 && index < seats.size() ? seats.get(index) : BlockPos.ZERO;
    }

    public AuctionTokenData withUuid(String nextUuid) {
        return new AuctionTokenData(nextUuid, placedCount, seats);
    }

    public AuctionTokenData addSeat(int nextPlacedCount, BlockPos pos) {
        List<BlockPos> next = new ArrayList<>(seats);
        while (next.size() < nextPlacedCount) next.add(BlockPos.ZERO);
        next.set(nextPlacedCount - 1, pos.immutable());
        return new AuctionTokenData(auctionUuid, nextPlacedCount, next);
    }

    public AuctionTokenData resetPlacements() {
        List<BlockPos> next = seats.isEmpty() ? List.of() : List.of(seats.getFirst());
        return new AuctionTokenData(auctionUuid, 0, next);
    }

    public AuctionTokenData removeChair(BlockPos pos) {
        int count = placedCount;
        List<BlockPos> next = new ArrayList<>(seats);
        for (int index = 2; index < count + 2; index++) {
            if (seat(index).equals(pos)) {
                BlockPos replacement = seat(count);
                while (next.size() < index) next.add(BlockPos.ZERO);
                next.set(index - 1, replacement);
                if (count - 1 >= 0 && count - 1 < next.size()) next.set(count - 1, BlockPos.ZERO);
                break;
            }
        }
        return new AuctionTokenData(auctionUuid, count - 1, next);
    }
}
