package net.yvanchuxiuzhen.system.quest.data;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.quest.api.BankDialogueSession;

/** 来源隐藏会话存盘并在全部克隆中复制；登录另按来源主动清空，仅同步本人。 */
public final class BankDialogueData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<BankDialogueSession>> SESSION = TYPES.register("quest_bank_dialogue", () -> AttachmentType
            .builder(() -> BankDialogueSession.EMPTY)
            .serialize(BankDialogueSession.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, BankDialogueSession.STREAM_CODEC)
            .build());

    private BankDialogueData() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
