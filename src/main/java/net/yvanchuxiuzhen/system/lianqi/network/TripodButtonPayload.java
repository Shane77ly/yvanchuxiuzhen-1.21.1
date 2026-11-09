package net.yvanchuxiuzhen.system.lianqi.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.TripodMenu;

/** 炼器按钮只传操作编号，装置与输入取自发送者当前菜单。 */
public record TripodButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<TripodButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tripod_button"));
    public static final StreamCodec<ByteBuf, TripodButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(TripodButtonPayload::new, TripodButtonPayload::button);

    @Override
    public Type<TripodButtonPayload> type() {
        return TYPE;
    }

    public static void handle(TripodButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof TripodMenu menu && payload.button >= 0 && payload.button <= 8) menu.press(player, payload.button);
    }
}
