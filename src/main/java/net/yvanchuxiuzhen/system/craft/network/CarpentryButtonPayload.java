package net.yvanchuxiuzhen.system.craft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.CarpentryMenu;

/** 木匠台按钮携带当前菜单编号，由服务端菜单确定机器位置与合法操作。 */
public record CarpentryButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<CarpentryButtonPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "carpentry_button"));
    public static final StreamCodec<ByteBuf, CarpentryButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CarpentryButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, CarpentryButtonPayload::button,
            CarpentryButtonPayload::new);

    @Override
    public Type<CarpentryButtonPayload> type() {
        return TYPE;
    }

    public static void handle(CarpentryButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof CarpentryMenu menu && menu.containerId == payload.containerId()) {
            menu.press(player, payload.button());
        }
    }
}
