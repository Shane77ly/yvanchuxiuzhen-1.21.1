package net.yvanchuxiuzhen.system.equipment.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.content.JewelryTableMenu;

/** 饰品台按钮请求只携带操作编号，目标位置由发送者当前菜单决定。 */
public record JewelryTableButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<JewelryTableButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "jewelry_table_button"));
    public static final StreamCodec<ByteBuf, JewelryTableButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(JewelryTableButtonPayload::new, JewelryTableButtonPayload::button);

    @Override
    public Type<JewelryTableButtonPayload> type() {
        return TYPE;
    }

    public static void handle(JewelryTableButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof JewelryTableMenu menu && payload.button >= 0 && payload.button < 7) {
            menu.press(player, payload.button);
        }
    }
}
