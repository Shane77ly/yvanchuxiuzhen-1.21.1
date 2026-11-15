package net.yvanchuxiuzhen.system.material.network;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.MaterialRevealedEntry;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;

/** 同步千材录本次刷新的显示快照；管理解锁只更新永久辨识附件，不反写此缓存。 */
public record MaterialAnalysisViewPayload(int containerId, String itemId, String name, int progress, int maximum,
                                          List<MaterialRevealedEntry> entries) implements CustomPacketPayload {
    public static final Type<MaterialAnalysisViewPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "material_analysis_view"));
    public static final StreamCodec<ByteBuf, MaterialAnalysisViewPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaterialAnalysisViewPayload::containerId,
            ByteBufCodecs.STRING_UTF8, MaterialAnalysisViewPayload::itemId,
            ByteBufCodecs.STRING_UTF8, MaterialAnalysisViewPayload::name,
            ByteBufCodecs.VAR_INT, MaterialAnalysisViewPayload::progress,
            ByteBufCodecs.VAR_INT, MaterialAnalysisViewPayload::maximum,
            ByteBufCodecs.fromCodec(MaterialRevealedEntry.CODEC.listOf()), MaterialAnalysisViewPayload::entries,
            MaterialAnalysisViewPayload::new);

    @Override
    public Type<MaterialAnalysisViewPayload> type() {
        return TYPE;
    }

    public static void handle(MaterialAnalysisViewPayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof MaterialAnalysisMenu menu && menu.containerId == payload.containerId()) {
            menu.setView(payload.itemId(), payload.name(), payload.progress(), payload.maximum(), payload.entries());
        }
    }
}
