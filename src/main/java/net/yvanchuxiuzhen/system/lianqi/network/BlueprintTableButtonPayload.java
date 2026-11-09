package net.yvanchuxiuzhen.system.lianqi.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.BlueprintTableMenu;

/** 图纸设计台按钮只携带菜单编号与操作编号，保留来源不传输入框文本的边界。 */
public record BlueprintTableButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<BlueprintTableButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "blueprint_table"));
    public static final StreamCodec<ByteBuf, BlueprintTableButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BlueprintTableButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, BlueprintTableButtonPayload::button, BlueprintTableButtonPayload::new);

    @Override
    public Type<BlueprintTableButtonPayload> type() {
        return TYPE;
    }

    public static void handle(BlueprintTableButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof BlueprintTableMenu menu && menu.containerId == payload.containerId
                && payload.button >= 0 && payload.button <= 1) menu.press(player, payload.button);
    }
}
