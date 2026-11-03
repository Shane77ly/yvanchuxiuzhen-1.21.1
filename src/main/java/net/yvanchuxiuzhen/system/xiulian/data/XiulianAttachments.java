package net.yvanchuxiuzhen.system.xiulian.data;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 修炼系统的玩家附件。来源这些字段全部在所有克隆事件中复制（{@code clone_policy.csv}：all_clone_events），只同步给本人。
 */
public final class XiulianAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<CultivationData>> CULTIVATION = TYPES.register("cultivation", () -> AttachmentType
            .builder(CultivationData::new)
            .serialize(CultivationData.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, CultivationData.STREAM_CODEC)
            .build());

    private XiulianAttachments() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
