package net.yvanchuxiuzhen.system.guchong.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.content.GuchongReleaseMenu;

/** 蛊笛按钮只提交当前容器号与按钮号，蛊罐与页码均读取服务端状态。 */
public record GuchongReleaseButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<GuchongReleaseButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong_release_button"));
    public static final StreamCodec<ByteBuf, GuchongReleaseButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GuchongReleaseButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, GuchongReleaseButtonPayload::button,
            GuchongReleaseButtonPayload::new);

    @Override
    public Type<GuchongReleaseButtonPayload> type() {
        return TYPE;
    }

    public static void handle(GuchongReleaseButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof GuchongReleaseMenu menu && menu.containerId == payload.containerId()
                && payload.button() >= 0 && payload.button() <= 7 && menu.stillValid(player)
                && player.level().hasChunkAt(menu.position())) {
            player.resetLastActionTime();
            menu.clickMenuButton(player, payload.button());
        }
    }
}
