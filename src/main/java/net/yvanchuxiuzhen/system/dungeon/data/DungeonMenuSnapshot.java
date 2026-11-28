package net.yvanchuxiuzhen.system.dungeon.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 副本菜单向客户端发送的强类型界面只读快照。 */
public record DungeonMenuSnapshot(
        double preview,
        String dungeonName,
        String templateId,
        String description,
        String text01,
        String text02,
        String text03,
        String text04,
        String rewardSummary,
        double minLevel,
        double maxLevel,
        double maxPeople,
        int sideLength,
        String markerId
) {
    public static final DungeonMenuSnapshot EMPTY = new DungeonMenuSnapshot(
            0, "", "", "", "", "", "", "", "", 0, 0, 0, 0, "");

    public static final StreamCodec<ByteBuf, DungeonMenuSnapshot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DungeonMenuSnapshot decode(ByteBuf buf) {
            return new DungeonMenuSnapshot(
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            );
        }

        @Override
        public void encode(ByteBuf buf, DungeonMenuSnapshot snapshot) {
            ByteBufCodecs.DOUBLE.encode(buf, snapshot.preview());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.dungeonName());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.templateId());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.description());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.text01());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.text02());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.text03());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.text04());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.rewardSummary());
            ByteBufCodecs.DOUBLE.encode(buf, snapshot.minLevel());
            ByteBufCodecs.DOUBLE.encode(buf, snapshot.maxLevel());
            ByteBufCodecs.DOUBLE.encode(buf, snapshot.maxPeople());
            ByteBufCodecs.VAR_INT.encode(buf, snapshot.sideLength());
            ByteBufCodecs.STRING_UTF8.encode(buf, snapshot.markerId());
        }
    };
}
