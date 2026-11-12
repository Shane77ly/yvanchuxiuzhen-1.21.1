package net.yvanchuxiuzhen.system.skill.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.content.SkillMenu;

/** 技能菜单按钮请求，绑定发送者当前容器；按来源允许出窍旁观状态操作归窍按钮。 */
public record SkillButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<SkillButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_button"));
    public static final StreamCodec<ByteBuf, SkillButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SkillButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, SkillButtonPayload::button,
            SkillButtonPayload::new);

    @Override
    public Type<SkillButtonPayload> type() {
        return TYPE;
    }

    public static void handle(SkillButtonPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof SkillMenu menu && menu.containerId == payload.containerId() && menu.stillValid(player)) {
            player.resetLastActionTime();
            if (menu.clickMenuButton(player, payload.button())) {
                menu.broadcastChanges();
            }
        }
    }
}
