package net.yvanchuxiuzhen.system.quest.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.quest.QuestJournalRuntime;

/** U 键请求仅打开本人任务列表，不携带会话或任务目录。 */
public record OpenQuestJournalPayload() implements CustomPacketPayload {
    public static final Type<OpenQuestJournalPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "open_quest_journal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenQuestJournalPayload> STREAM_CODEC = StreamCodec.unit(new OpenQuestJournalPayload());

    @Override
    public Type<OpenQuestJournalPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player && player.serverLevel().hasChunkAt(player.blockPosition())) QuestJournalRuntime.open(player);
        });
    }
}
