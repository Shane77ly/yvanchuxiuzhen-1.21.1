package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 匾额与牌位沿来源逗号字段格式提交输入，服务端从当前菜单取得目标位置。 */
public record InscribedDecorPayload(int containerId, int button, String state) implements CustomPacketPayload {
    public static final Type<InscribedDecorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "inscribed_decor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InscribedDecorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, InscribedDecorPayload::containerId,
            ByteBufCodecs.VAR_INT, InscribedDecorPayload::button,
            ByteBufCodecs.STRING_UTF8, InscribedDecorPayload::state, InscribedDecorPayload::new);
    @Override public Type<InscribedDecorPayload> type() { return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof InscribedDecorMenu menu
                    && menu.containerId == payload.containerId()) menu.apply(player, payload.button(), payload.state());
        });
    }
}
