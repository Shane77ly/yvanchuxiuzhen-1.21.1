package net.yvanchuxiuzhen.system.liandan.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyMenu;

/** 炼丹按钮只传操作编号，真实装置与输入库存由发送者当前菜单确定。 */
public record AlchemyButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<AlchemyButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "alchemy_button"));
    public static final StreamCodec<ByteBuf, AlchemyButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(AlchemyButtonPayload::new, AlchemyButtonPayload::button);

    @Override
    public Type<AlchemyButtonPayload> type() {
        return TYPE;
    }

    public static void handle(AlchemyButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof AlchemyMenu menu && payload.button >= 0 && payload.button <= 5) {
            menu.press(player, payload.button);
        }
    }
}
