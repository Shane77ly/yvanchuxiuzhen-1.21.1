package net.yvanchuxiuzhen.system.fulu.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableMenu;

/** 画符按钮只传当前菜单和操作编号，配方与玩家材料在服务端读取。 */
public record DrawingTableButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<DrawingTableButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "drawing_table_button"));
    public static final StreamCodec<ByteBuf, DrawingTableButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DrawingTableButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, DrawingTableButtonPayload::button,
            DrawingTableButtonPayload::new);

    @Override
    public Type<DrawingTableButtonPayload> type() {
        return TYPE;
    }

    public static void handle(DrawingTableButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof DrawingTableMenu menu && menu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 8) menu.press(player, payload.button);
    }
}
