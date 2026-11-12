package net.yvanchuxiuzhen.system.liandan.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.PharmacologyMenu;

/** 药理台按钮只携带菜单编号与操作编号，保留来源不传输入框文本的边界。 */
public record PharmacologyPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<PharmacologyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pharmacology"));
    public static final StreamCodec<ByteBuf, PharmacologyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PharmacologyPayload::containerId,
            ByteBufCodecs.VAR_INT, PharmacologyPayload::button, PharmacologyPayload::new);

    @Override
    public Type<PharmacologyPayload> type() {
        return TYPE;
    }

    public static void handle(PharmacologyPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof PharmacologyMenu menu && menu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 1) menu.press(player, payload.button);
    }
}
