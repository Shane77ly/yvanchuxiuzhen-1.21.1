package net.yvanchuxiuzhen.system.creature.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.content.SpiritBeastBagDyeMenu;

/** 染色按钮仅提交当前菜单实例与按钮编号。 */
public record SpiritBeastBagDyeButtonPayload(int containerId, int button) implements CustomPacketPayload {
    public static final Type<SpiritBeastBagDyeButtonPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "spirit_beast_bag_dye_button"));
    public static final StreamCodec<ByteBuf, SpiritBeastBagDyeButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SpiritBeastBagDyeButtonPayload::containerId,
            ByteBufCodecs.VAR_INT, SpiritBeastBagDyeButtonPayload::button,
            SpiritBeastBagDyeButtonPayload::new);

    @Override
    public Type<SpiritBeastBagDyeButtonPayload> type() { return TYPE; }

    public static void handle(SpiritBeastBagDyeButtonPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player.containerMenu instanceof SpiritBeastBagDyeMenu menu && menu.containerId == payload.containerId()
                    && payload.button() == 0 && menu.stillValid(player) && player.level().hasChunkAt(menu.position())) {
                menu.clickMenuButton(player, payload.button());
            }
        });
    }
}
