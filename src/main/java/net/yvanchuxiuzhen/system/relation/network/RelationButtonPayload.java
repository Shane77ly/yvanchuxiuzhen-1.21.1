package net.yvanchuxiuzhen.system.relation.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.FriendList;
import net.yvanchuxiuzhen.system.relation.TeamRequest;
import net.yvanchuxiuzhen.system.relation.content.FriendMenu;
import net.yvanchuxiuzhen.system.relation.content.TeamRequestMenu;

/**
 * 好友界面与好友申请界面的按钮（客户端 → 服务端，来源 {@code FriendguiButtonMessage}、{@code ZuduiteamGuiButtonMessage}）。
 * 服务端按发送者当前打开的菜单分派；菜单不符或编号超出该界面的按钮范围时不处理。处理在服务端主线程执行。
 */
public record RelationButtonPayload(int button) implements CustomPacketPayload {
    public static final Type<RelationButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "relation_button"));
    public static final StreamCodec<ByteBuf, RelationButtonPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(RelationButtonPayload::new, RelationButtonPayload::button);

    @Override
    public Type<RelationButtonPayload> type() {
        return TYPE;
    }

    public static void handle(RelationButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        int button = payload.button();
        if (button < 0) {
            return;
        }
        if (player.containerMenu instanceof FriendMenu && button < FriendList.BUTTONS) {
            FriendList.press(player, button);
        } else if (player.containerMenu instanceof TeamRequestMenu && button < TeamRequest.BUTTONS) {
            TeamRequest.press(player, button);
        }
    }
}
