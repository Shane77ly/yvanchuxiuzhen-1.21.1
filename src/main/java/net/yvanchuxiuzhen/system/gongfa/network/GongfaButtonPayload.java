package net.yvanchuxiuzhen.system.gongfa.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.gongfa.Comprehension;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionMenu;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionTableBlockEntity;

/**
 * 功法系统界面的按钮（客户端 → 服务端）：参悟界面（来源 {@code CanwutaiButtonMessage}，0–2）。
 * 按发送者当前打开的菜单分派，编号越界时不处理。
 */
public record GongfaButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<GongfaButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "gongfa_button"));
    public static final StreamCodec<ByteBuf, GongfaButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(GongfaButtonPayload::new, GongfaButtonPayload::button);

    @Override
    public Type<GongfaButtonPayload> type() {
        return TYPE;
    }

    public static void handle(GongfaButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        int button = payload.button();
        if (player.containerMenu instanceof ComprehensionMenu menu && button >= 0 && button < Comprehension.BUTTONS
                && player.level().getBlockEntity(menu.pos()) instanceof ComprehensionTableBlockEntity table) {
            Comprehension.press(player, table, button);
        }
    }
}
