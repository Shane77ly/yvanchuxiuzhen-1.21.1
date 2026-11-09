package net.yvanchuxiuzhen.system.fulu.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceMenu;

/** 切换传承只传按钮编号，目标位置和当前传承由服务端菜单决定。 */
public record InheritanceButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<InheritanceButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "inheritance_button"));
    public static final StreamCodec<ByteBuf, InheritanceButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(InheritanceButtonPayload::new, InheritanceButtonPayload::button);

    @Override
    public Type<InheritanceButtonPayload> type() {
        return TYPE;
    }

    public static void handle(InheritanceButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (payload.button == 0 && player.containerMenu instanceof InheritanceMenu menu) menu.press(player);
    }
}
