package net.yvanchuxiuzhen.system.cooking.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.SteamerMenu;

/** 蒸锅两个按钮绑定当前菜单编号和同一设备，不接受客户端指定库存坐标。 */
public record SteamerButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<SteamerButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "steamer_button"));
    public static final StreamCodec<ByteBuf, SteamerButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SteamerButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, SteamerButtonPayload::button, SteamerButtonPayload::new);

    @Override
    public Type<SteamerButtonPayload> type() { return TYPE; }

    public static void handle(SteamerButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof SteamerMenu menu && player.containerMenu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 1) menu.press(player, payload.button);
    }
}
