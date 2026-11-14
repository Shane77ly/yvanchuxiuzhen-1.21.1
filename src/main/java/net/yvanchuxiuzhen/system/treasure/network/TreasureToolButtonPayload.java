package net.yvanchuxiuzhen.system.treasure.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.content.TreasureToolMenu;

/** 设置请求只携带当前菜单与按钮编号，五项配置仍由服务端读取当前主手。 */
public record TreasureToolButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<TreasureToolButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "treasure_tool_button"));
    public static final StreamCodec<ByteBuf, TreasureToolButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TreasureToolButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, TreasureToolButtonPayload::button,
            TreasureToolButtonPayload::new);

    @Override
    public Type<TreasureToolButtonPayload> type() {
        return TYPE;
    }

    public static void handle(TreasureToolButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof TreasureToolMenu menu && menu.containerId == payload.containerId()) {
            menu.clickMenuButton(player, payload.button());
        }
    }
}
