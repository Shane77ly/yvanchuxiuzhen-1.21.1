package net.yvanchuxiuzhen.system.craft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelMenu;

/** 陶轮按钮携带当前菜单编号，由服务端菜单确定机器位置与合法操作。 */
public record PotteryWheelButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<PotteryWheelButtonPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pottery_wheel_button"));
    public static final StreamCodec<ByteBuf, PotteryWheelButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PotteryWheelButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, PotteryWheelButtonPayload::button,
            PotteryWheelButtonPayload::new);

    @Override
    public Type<PotteryWheelButtonPayload> type() {
        return TYPE;
    }

    public static void handle(PotteryWheelButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof PotteryWheelMenu menu && menu.containerId == payload.containerId()) {
            menu.press(player, payload.button());
        }
    }
}
