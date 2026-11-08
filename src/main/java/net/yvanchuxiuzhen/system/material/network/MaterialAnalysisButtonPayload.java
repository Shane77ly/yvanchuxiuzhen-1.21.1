package net.yvanchuxiuzhen.system.material.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;

/** 千材录按钮绑定当前容器，保留来源允许旁观者操作菜单的入口。 */
public record MaterialAnalysisButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<MaterialAnalysisButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "material_analysis_button"));
    public static final StreamCodec<ByteBuf, MaterialAnalysisButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaterialAnalysisButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, MaterialAnalysisButtonPayload::button,
            MaterialAnalysisButtonPayload::new);

    @Override
    public Type<MaterialAnalysisButtonPayload> type() {
        return TYPE;
    }

    public static void handle(MaterialAnalysisButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof MaterialAnalysisMenu menu && menu.containerId == payload.containerId() && menu.stillValid(player)) {
            player.resetLastActionTime();
            if (menu.clickMenuButton(player, payload.button())) menu.broadcastChanges();
        }
    }
}
