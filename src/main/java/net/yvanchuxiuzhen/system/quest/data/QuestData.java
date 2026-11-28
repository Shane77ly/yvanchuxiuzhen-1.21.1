package net.yvanchuxiuzhen.system.quest.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.api.QuestJournalState;
import net.yvanchuxiuzhen.system.quest.api.QuestStoryFlags;

/** 来源任务会话、任务目录和剧情扩展数据各自以强类型附件保存，写入统一由任务操作处理。 */
public final class QuestData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<QuestDialogueSession>> SESSION = register(
            "quest_dialogue", () -> QuestDialogueSession.EMPTY, QuestDialogueSession.CODEC, QuestDialogueSession.STREAM_CODEC, true);
    public static final Supplier<AttachmentType<QuestJournalState>> JOURNAL = register(
            "quest_journal", () -> QuestJournalState.EMPTY, QuestJournalState.CODEC, QuestJournalState.STREAM_CODEC, true);
    public static final Supplier<AttachmentType<QuestStoryFlags>> FLAGS = register(
            "quest_story_flags", () -> QuestStoryFlags.EMPTY, QuestStoryFlags.CODEC, QuestStoryFlags.STREAM_CODEC, false);
    public static final Supplier<AttachmentType<BountyTargetData>> BOUNTY_TARGET = TYPES.register("bounty_target",
            () -> AttachmentType.builder(() -> BountyTargetData.EMPTY).serialize(BountyTargetData.CODEC).build());

    private QuestData() {
    }

    private static <T> Supplier<AttachmentType<T>> register(
            String id, Supplier<T> initializer, Codec<T> codec, StreamCodec<ByteBuf, T> streamCodec, boolean copyOnDeath) {
        return TYPES.register(id, () -> {
            AttachmentType.Builder<T> builder = AttachmentType.builder(initializer).serialize(codec);
            if (copyOnDeath) builder.copyOnDeath();
            return builder.sync((holder, player) -> holder == player, streamCodec).build();
        });
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
