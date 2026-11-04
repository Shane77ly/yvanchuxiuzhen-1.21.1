package net.yvanchuxiuzhen.system.combat.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.content.DummyMenu;

/**
 * 木人桩设置界面的按钮（客户端 → 服务端，来源 {@code MurenzhuangGuiButtonMessage}）：0、1、2 把对应输入框的数字设为
 * 木人桩的物理、法术、神识防御基值（解析失败为 0），3 是“拿走木人桩”。按发送者当前打开的菜单分派。
 */
public record DummyButtonPayload(int button, String text) implements CustomPacketPayload {
    public static final Type<DummyButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "combat_dummy_button"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DummyButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DummyButtonPayload::button, ByteBufCodecs.STRING_UTF8, DummyButtonPayload::text, DummyButtonPayload::new);

    @Override
    public Type<DummyButtonPayload> type() {
        return TYPE;
    }

    public static void handle(DummyButtonPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DummyMenu menu) {
            menu.press(player, payload.button(), payload.text());
        }
    }
}
