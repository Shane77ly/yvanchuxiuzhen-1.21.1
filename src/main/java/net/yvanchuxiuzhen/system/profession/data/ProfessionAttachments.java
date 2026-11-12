package net.yvanchuxiuzhen.system.profession.data;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源三十个职业数值在所有克隆事件中复制；附件持久化并且只同步给本人。 */
public final class ProfessionAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<ProfessionData>> PROGRESS = TYPES.register("profession", () -> AttachmentType
            .builder(ProfessionData::new)
            .serialize(ProfessionData.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, ProfessionData.STREAM_CODEC)
            .build());

    private ProfessionAttachments() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
