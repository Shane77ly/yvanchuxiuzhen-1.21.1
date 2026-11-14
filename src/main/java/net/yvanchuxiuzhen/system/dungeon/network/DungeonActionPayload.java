package net.yvanchuxiuzhen.system.dungeon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonMenu;

/** 按钮请求绑定当前菜单，客户端只提交配置输入和攻击复选框。 */
public record DungeonActionPayload(int containerId, String value, boolean attack) implements CustomPacketPayload {
    public static final Type<DungeonActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dungeon_action"));
    public static final StreamCodec<ByteBuf, DungeonActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DungeonActionPayload::containerId,
            ByteBufCodecs.STRING_UTF8, DungeonActionPayload::value,
            ByteBufCodecs.BOOL, DungeonActionPayload::attack,
            DungeonActionPayload::new);

    @Override public Type<DungeonActionPayload> type() { return TYPE; }

    public static void handle(DungeonActionPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof DungeonMenu menu && menu.containerId == payload.containerId() && menu.stillValid(player)) {
            player.resetLastActionTime();
            menu.action(player, payload.value(), payload.attack());
        }
    }
}
