package net.yvanchuxiuzhen.system.liandan.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.HerbAnalysisMenu;

/** 百草鉴按钮绑定发送者当前菜单，药草样本与解析真值均由服务端读取。 */
public record HerbAnalysisPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<HerbAnalysisPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "herb_analysis"));
    public static final StreamCodec<ByteBuf, HerbAnalysisPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HerbAnalysisPayload::containerId,
            ByteBufCodecs.VAR_INT, HerbAnalysisPayload::button, HerbAnalysisPayload::new);

    @Override
    public Type<HerbAnalysisPayload> type() {
        return TYPE;
    }

    public static void handle(HerbAnalysisPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof HerbAnalysisMenu menu && menu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 1) menu.clickMenuButton(player, payload.button);
    }
}
