package net.yvanchuxiuzhen.system.quest.data;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源任务会话、任务目录和剧情扩展数据各自保存，写入统一由任务操作处理。 */
public final class QuestData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<CompoundTag>> SESSION = register("quest_dialogue", true);
    public static final Supplier<AttachmentType<CompoundTag>> JOURNAL = register("quest_journal", true);
    public static final Supplier<AttachmentType<CompoundTag>> FLAGS = register("quest_story_flags", false);

    private QuestData() {
    }

    private static Supplier<AttachmentType<CompoundTag>> register(String id, boolean copyOnDeath) {
        return TYPES.register(id, () -> {
            AttachmentType.Builder<CompoundTag> builder = AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC);
            if (copyOnDeath) builder.copyOnDeath();
            return builder.sync((holder, player) -> holder == player, ByteBufCodecs.COMPOUND_TAG).build();
        });
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
