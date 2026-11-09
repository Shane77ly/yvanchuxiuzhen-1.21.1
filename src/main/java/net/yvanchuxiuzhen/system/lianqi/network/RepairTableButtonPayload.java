package net.yvanchuxiuzhen.system.lianqi.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableMenu;

/** 修理按钮只传操作编号，修理物品与坐标由当前服务端菜单提供。 */
public record RepairTableButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<RepairTableButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "repair_table_button"));
    public static final StreamCodec<ByteBuf, RepairTableButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(RepairTableButtonPayload::new, RepairTableButtonPayload::button);

    @Override
    public Type<RepairTableButtonPayload> type() {
        return TYPE;
    }

    public static void handle(RepairTableButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof RepairTableMenu menu && payload.button >= 0 && payload.button <= 1) menu.press(player, payload.button);
    }
}
