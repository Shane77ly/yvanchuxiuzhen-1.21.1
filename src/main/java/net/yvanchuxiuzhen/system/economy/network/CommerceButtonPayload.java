package net.yvanchuxiuzhen.system.economy.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.content.CommerceMenu;

/** 按钮只传当前容器和操作；商品、数量、价格及账户均在服务端现读。 */
public record CommerceButtonPayload(int containerId, int operation) implements CustomPacketPayload {
    public static final Type<CommerceButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "commerce_button"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CommerceButtonPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> { buffer.writeVarInt(value.containerId()); buffer.writeVarInt(value.operation()); },
            buffer -> new CommerceButtonPayload(buffer.readVarInt(), buffer.readVarInt()));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CommerceButtonPayload value, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.containerMenu.containerId == value.containerId()
                && player.containerMenu instanceof CommerceMenu menu && menu.stillValid(player)) menu.button(player, value.operation());
    }
}
