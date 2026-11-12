package net.yvanchuxiuzhen.system.liandan.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.MedicineSeparationMenu;

/** 操作绑定发送时的菜单编号，切页后到达的旧按钮包不作用于新页面。 */
public record MedicineSeparationPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<MedicineSeparationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "medicine_separation"));
    public static final StreamCodec<ByteBuf, MedicineSeparationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MedicineSeparationPayload::containerId,
            ByteBufCodecs.VAR_INT, MedicineSeparationPayload::button, MedicineSeparationPayload::new);

    @Override
    public Type<MedicineSeparationPayload> type() {
        return TYPE;
    }

    public static void handle(MedicineSeparationPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof MedicineSeparationMenu menu && menu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 1) menu.press(player, payload.button);
    }
}
