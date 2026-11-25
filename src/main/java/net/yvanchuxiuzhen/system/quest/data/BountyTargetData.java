package net.yvanchuxiuzhen.system.quest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/** 悬赏妖兽关联的告示牌位置、槽位索引与猎人 UUID。 */
public record BountyTargetData(boolean active, int boardX, int boardY, int boardZ, int slotIndex, String hunterUuid) {
    public static final BountyTargetData EMPTY = new BountyTargetData(false, 0, 0, 0, 0, "");
    public static final Codec<BountyTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("active", false).forGetter(BountyTargetData::active),
            Codec.INT.optionalFieldOf("board_x", 0).forGetter(BountyTargetData::boardX),
            Codec.INT.optionalFieldOf("board_y", 0).forGetter(BountyTargetData::boardY),
            Codec.INT.optionalFieldOf("board_z", 0).forGetter(BountyTargetData::boardZ),
            Codec.INT.optionalFieldOf("slot_index", 0).forGetter(BountyTargetData::slotIndex),
            Codec.STRING.optionalFieldOf("hunter_uuid", "").forGetter(BountyTargetData::hunterUuid)
    ).apply(instance, BountyTargetData::new));

    public BlockPos boardPos() {
        return new BlockPos(boardX, boardY, boardZ);
    }
}
