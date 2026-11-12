package net.yvanchuxiuzhen.system.lianqi.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.SewingBasketMenu;

/** 翻页与制作仅作用于发送者当前的缝纫菜单。 */
public record SewingBasketPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<SewingBasketPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "sewing_basket"));
    public static final StreamCodec<ByteBuf, SewingBasketPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SewingBasketPayload::containerId,
            ByteBufCodecs.VAR_INT, SewingBasketPayload::button, SewingBasketPayload::new);

    @Override
    public Type<SewingBasketPayload> type() {
        return TYPE;
    }

    public static void handle(SewingBasketPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof SewingBasketMenu menu && menu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 2) menu.press(player, payload.button);
    }
}
