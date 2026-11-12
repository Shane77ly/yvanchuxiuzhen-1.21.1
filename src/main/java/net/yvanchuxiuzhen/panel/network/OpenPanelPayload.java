package net.yvanchuxiuzhen.panel.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.platform.ModId;

/** K 键只请求打开，分页由服务端本人附件决定。 */
public record OpenPanelPayload() implements CustomPacketPayload {
    public static final Type<OpenPanelPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "open_player_panel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPanelPayload> STREAM_CODEC = StreamCodec.unit(new OpenPanelPayload());

    @Override
    public Type<OpenPanelPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) PanelNavigation.open(player);
        });
    }
}
