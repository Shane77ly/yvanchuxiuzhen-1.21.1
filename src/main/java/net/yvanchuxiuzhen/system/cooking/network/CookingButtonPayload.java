package net.yvanchuxiuzhen.system.cooking.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotMenu;

/** 烹饪按钮只提交编号，操作坐标与真实库存由服务端当前菜单提供。 */
public record CookingButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<CookingButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cooking_button"));
    public static final StreamCodec<ByteBuf, CookingButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(CookingButtonPayload::new, CookingButtonPayload::button);

    @Override
    public Type<CookingButtonPayload> type() {
        return TYPE;
    }

    public static void handle(CookingButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof CookingPotMenu menu && payload.button >= 0 && payload.button <= 2) menu.press(player, payload.button);
    }
}
