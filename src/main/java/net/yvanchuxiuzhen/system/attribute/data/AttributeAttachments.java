package net.yvanchuxiuzhen.system.attribute.data;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 属性系统的玩家附件。来源中这些字段在全部克隆事件中复制（{@code clone_policy.csv}：all_clone_events），
 * 因此死亡后保留；只同步给玩家本人，其他客户端不持有该数据。
 */
public final class AttributeAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<AttributeData>> ATTRIBUTES = TYPES.register("attribute", () -> AttachmentType
            .builder(() -> AttributeData.EMPTY)
            .serialize(AttributeData.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, AttributeData.STREAM_CODEC)
            .build());

    private AttributeAttachments() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
