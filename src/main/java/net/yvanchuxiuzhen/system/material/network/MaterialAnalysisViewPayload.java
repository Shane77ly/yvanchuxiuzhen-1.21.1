package net.yvanchuxiuzhen.system.material.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;

/** 只同步千材录当前刷新选中的材料和显示名；永久辨识数据使用本人附件同步。 */
public record MaterialAnalysisViewPayload(int containerId, String itemId, String name) implements CustomPacketPayload {
    public static final Type<MaterialAnalysisViewPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "material_analysis_view"));
    public static final StreamCodec<ByteBuf, MaterialAnalysisViewPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaterialAnalysisViewPayload::containerId,
            ByteBufCodecs.STRING_UTF8, MaterialAnalysisViewPayload::itemId,
            ByteBufCodecs.STRING_UTF8, MaterialAnalysisViewPayload::name,
            MaterialAnalysisViewPayload::new);

    @Override
    public Type<MaterialAnalysisViewPayload> type() {
        return TYPE;
    }

    public static void handle(MaterialAnalysisViewPayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof MaterialAnalysisMenu menu && menu.containerId == payload.containerId()) {
            menu.setView(payload.itemId(), payload.name());
        }
    }
}
