package net.yvanchuxiuzhen.system.xiulian.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;
import net.yvanchuxiuzhen.system.xiulian.content.BirthMenu;

/** 出身界面的按钮（客户端 → 服务端，来源 {@code PlayerJoinGUIButtonMessage}）；发送者未打开出身界面或编号越界时不处理。 */
public record BirthButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<BirthButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "birth_button"));
    public static final StreamCodec<ByteBuf, BirthButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(BirthButtonPayload::new, BirthButtonPayload::button);

    @Override
    public Type<BirthButtonPayload> type() {
        return TYPE;
    }

    public static void handle(BirthButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof BirthMenu && payload.button() >= 0 && payload.button() < BirthSelection.BUTTONS) {
            BirthSelection.press(player, payload.button());
        }
    }
}
