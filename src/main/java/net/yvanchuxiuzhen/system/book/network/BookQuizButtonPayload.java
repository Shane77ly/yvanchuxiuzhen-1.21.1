package net.yvanchuxiuzhen.system.book.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.content.BookQuizMenu;

/** 答题请求只提交选项，题目与累计数始终读取服务端真值。 */
public record BookQuizButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<BookQuizButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_quiz_button"));
    public static final StreamCodec<ByteBuf, BookQuizButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BookQuizButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, BookQuizButtonPayload::button,
            BookQuizButtonPayload::new);

    @Override
    public Type<BookQuizButtonPayload> type() {
        return TYPE;
    }

    public static void handle(BookQuizButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof BookQuizMenu menu && menu.containerId == payload.containerId() && menu.stillValid(player)) {
            player.resetLastActionTime();
            menu.clickMenuButton(player, payload.button());
        }
    }
}
