package net.yvanchuxiuzhen.system.npc.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.npc.NpcManagementData;
import net.yvanchuxiuzhen.system.npc.NpcManagementMenu;

/** 设置面板的文本字段提交由服务端写入手持工具的自定义数据。 */
public record NpcManagementPayload(List<String> values, boolean fixed) implements CustomPacketPayload {
    public static final Type<NpcManagementPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "npc_management"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NpcManagementPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), NpcManagementPayload::values,
            ByteBufCodecs.BOOL, NpcManagementPayload::fixed, NpcManagementPayload::new);

    @Override
    public Type<NpcManagementPayload> type() { return TYPE; }

    public static void handle(NpcManagementPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !(player.containerMenu instanceof NpcManagementMenu menu)
                || !menu.hasBoundTool() || payload.values().size() != NpcManagementData.KEYS.size()) return;
        double level;
        try {
            String value = payload.values().get(NpcManagementData.KEYS.indexOf("npclevel"));
            level = value.isEmpty() ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return;
        }
        if (!Double.isFinite(level)) return;
        menu.saveEquipment(player);
        menu.saveProfile(payload.values(), level, payload.fixed());
        player.closeContainer();
    }
}
