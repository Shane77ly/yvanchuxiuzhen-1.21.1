package net.yvanchuxiuzhen.collection.tiandao;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 客户端提交来源格式的输入与八类型选项，服务端按实际打开的编辑器菜单处理。 */
public record NbtEditorPayload(int containerId, String state) implements CustomPacketPayload {
    public static final Type<NbtEditorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandao_item_nbt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NbtEditorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, NbtEditorPayload::containerId,
            ByteBufCodecs.STRING_UTF8, NbtEditorPayload::state, NbtEditorPayload::new);

    @Override public Type<NbtEditorPayload> type() { return TYPE; }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof NbtEditorMenu menu
                    && menu.containerId == payload.containerId()) menu.apply(player, payload.state());
        });
    }
}
