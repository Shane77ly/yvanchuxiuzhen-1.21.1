package net.yvanchuxiuzhen.system.fulu.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerMenu;

/** 帮助请求绑定当前烧灰菜单，内容由服务端发送。 */
public record AshBurnerHelpPayload(int containerId) implements CustomPacketPayload {
    public static final Type<AshBurnerHelpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ash_burner_help"));
    public static final StreamCodec<ByteBuf, AshBurnerHelpPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(AshBurnerHelpPayload::new, AshBurnerHelpPayload::containerId);

    @Override
    public Type<AshBurnerHelpPayload> type() {
        return TYPE;
    }

    public static void handle(AshBurnerHelpPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof AshBurnerMenu menu && menu.containerId == payload.containerId) menu.help(player);
    }
}
