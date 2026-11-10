package net.yvanchuxiuzhen.system.cooking.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.WoodenStillMenu;

/** 木甑按钮绑定玩家当前菜单及其真实方块实体，保留旁观模式的自定义按钮入口。 */
public record WineButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<WineButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "wine_button"));
    public static final StreamCodec<ByteBuf, WineButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WineButtonPayload::containerId, ByteBufCodecs.VAR_INT, WineButtonPayload::button, WineButtonPayload::new);

    @Override
    public Type<WineButtonPayload> type() {
        return TYPE;
    }

    public static void handle(WineButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof WoodenStillMenu menu && menu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 1) menu.press(player, payload.button);
    }
}
