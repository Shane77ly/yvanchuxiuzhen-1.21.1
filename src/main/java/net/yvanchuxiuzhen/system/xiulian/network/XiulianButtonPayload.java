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
import net.yvanchuxiuzhen.system.xiulian.content.LevelUpMenu;

/**
 * 修炼系统界面的按钮（客户端 → 服务端）：出身界面（来源 {@code PlayerJoinGUIButtonMessage}，0–7）与升级界面
 * （来源 {@code PlayershengjixianshiButtonMessage}，0 为关闭）。按发送者当前打开的菜单分派，编号越界时不处理。
 */
public record XiulianButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<XiulianButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xiulian_button"));
    public static final StreamCodec<ByteBuf, XiulianButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(XiulianButtonPayload::new, XiulianButtonPayload::button);

    @Override
    public Type<XiulianButtonPayload> type() {
        return TYPE;
    }

    public static void handle(XiulianButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        int button = payload.button();
        if (player.containerMenu instanceof BirthMenu && button >= 0 && button < BirthSelection.BUTTONS) {
            BirthSelection.press(player, button);
        } else if (player.containerMenu instanceof LevelUpMenu && button == 0) {
            player.closeContainer();
        }
    }
}
