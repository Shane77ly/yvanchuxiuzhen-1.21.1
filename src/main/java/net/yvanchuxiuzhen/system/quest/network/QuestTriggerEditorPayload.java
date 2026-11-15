package net.yvanchuxiuzhen.system.quest.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.quest.content.QuestTriggerEditorMenu;

/** 只提交当前任务方块菜单的编辑内容，不接收任意方块坐标。 */
public record QuestTriggerEditorPayload(int containerId, String state) implements CustomPacketPayload {
    public static final Type<QuestTriggerEditorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "quest_trigger_editor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestTriggerEditorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, QuestTriggerEditorPayload::containerId,
            ByteBufCodecs.STRING_UTF8, QuestTriggerEditorPayload::state, QuestTriggerEditorPayload::new);

    @Override
    public Type<QuestTriggerEditorPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof QuestTriggerEditorMenu menu
                    && menu.containerId == payload.containerId()) menu.apply(player, payload.state());
        });
    }
}
