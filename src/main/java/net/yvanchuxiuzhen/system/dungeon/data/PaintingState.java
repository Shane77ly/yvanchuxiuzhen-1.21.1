package net.yvanchuxiuzhen.system.dungeon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 画里乾坤法宝物品保存的主人、出入坐标、维度与状态标志（{@code yvanchuxiuzhen:painting_state}）。 */
public record PaintingState(String ownerUuid,
                            double rukouX, double rukouY, double rukouZ,
                            double chukouX, double chukouY, double chukouZ,
                            String returnDimension, double paintingNumber,
                            boolean activated, boolean placed, boolean entering, boolean returning) {
    public static final PaintingState EMPTY = new PaintingState("", 0, 0, 0, 0, 0, 0, "", 0, false, false, false, false);
    public static final Codec<PaintingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(PaintingState::ownerUuid),
            Codec.DOUBLE.optionalFieldOf("rukou_x", 0.0).forGetter(PaintingState::rukouX),
            Codec.DOUBLE.optionalFieldOf("rukou_y", 0.0).forGetter(PaintingState::rukouY),
            Codec.DOUBLE.optionalFieldOf("rukou_z", 0.0).forGetter(PaintingState::rukouZ),
            Codec.DOUBLE.optionalFieldOf("chukou_x", 0.0).forGetter(PaintingState::chukouX),
            Codec.DOUBLE.optionalFieldOf("chukou_y", 0.0).forGetter(PaintingState::chukouY),
            Codec.DOUBLE.optionalFieldOf("chukou_z", 0.0).forGetter(PaintingState::chukouZ),
            Codec.STRING.optionalFieldOf("return_dimension", "").forGetter(PaintingState::returnDimension),
            Codec.DOUBLE.optionalFieldOf("painting_number", 0.0).forGetter(PaintingState::paintingNumber),
            Codec.BOOL.optionalFieldOf("activated", false).forGetter(PaintingState::activated),
            Codec.BOOL.optionalFieldOf("placed", false).forGetter(PaintingState::placed),
            Codec.BOOL.optionalFieldOf("entering", false).forGetter(PaintingState::entering),
            Codec.BOOL.optionalFieldOf("returning", false).forGetter(PaintingState::returning)
    ).apply(instance, PaintingState::new));
    public static final StreamCodec<ByteBuf, PaintingState> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public BlockPos rukouPos() {
        return BlockPos.containing(rukouX, rukouY, rukouZ);
    }

    public BlockPos chukouPos() {
        return BlockPos.containing(chukouX, chukouY, chukouZ);
    }

    public PaintingState withPlacement(boolean insidePainting, BlockPos pos, String nextOwnerUuid) {
        return insidePainting
                ? new PaintingState(nextOwnerUuid, rukouX, rukouY, rukouZ, pos.getX(), pos.getY(), pos.getZ(),
                returnDimension, paintingNumber, activated, placed, entering, returning)
                : new PaintingState(nextOwnerUuid, pos.getX(), pos.getY(), pos.getZ(), chukouX, chukouY, chukouZ,
                returnDimension, paintingNumber, activated, placed, entering, returning);
    }

    public PaintingState withReturning(boolean value) {
        return new PaintingState(ownerUuid, rukouX, rukouY, rukouZ, chukouX, chukouY, chukouZ,
                returnDimension, paintingNumber, activated, placed, entering, value);
    }

    public PaintingState withEntering(boolean value) {
        return new PaintingState(ownerUuid, rukouX, rukouY, rukouZ, chukouX, chukouY, chukouZ,
                returnDimension, paintingNumber, activated, placed, value, returning);
    }

    public PaintingState enter(String dimension, boolean firstActivation, double number) {
        double nextNumber = firstActivation ? number : paintingNumber;
        double nextChukouX = firstActivation ? 48 * number : chukouX;
        double nextChukouY = firstActivation ? 100 : chukouY;
        double nextChukouZ = firstActivation ? 48 : chukouZ;
        return new PaintingState(ownerUuid, rukouX, rukouY, rukouZ, nextChukouX, nextChukouY, nextChukouZ,
                dimension, nextNumber, true, !firstActivation, true, returning);
    }

    public PaintingState withPlacedExitRaised() {
        return new PaintingState(ownerUuid, rukouX, rukouY, rukouZ, chukouX, chukouY + 2, chukouZ,
                returnDimension, paintingNumber, activated, true, false, returning);
    }
}
