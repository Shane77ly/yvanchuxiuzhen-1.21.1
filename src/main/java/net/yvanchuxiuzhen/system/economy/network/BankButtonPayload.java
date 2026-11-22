package net.yvanchuxiuzhen.system.economy.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.content.BankLoginMenu;
import net.yvanchuxiuzhen.system.economy.content.BankMenu;

/** 银行请求只携带当前容器ID和按钮号；货币、卡与输入库存均现读服务端。 */
public record BankButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<BankButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "bank_button"));
    public static final StreamCodec<ByteBuf, BankButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BankButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, BankButtonPayload::button,
            BankButtonPayload::new);

    @Override
    public Type<BankButtonPayload> type() {
        return TYPE;
    }

    public static void handle(BankButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        AbstractContainerMenu menu = player.containerMenu;
        if ((menu instanceof BankMenu || menu instanceof BankLoginMenu) && menu.containerId == payload.containerId() && menu.stillValid(player)) {
            player.resetLastActionTime();
            menu.clickMenuButton(player, payload.button());
        }
    }
}
