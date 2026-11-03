package net.yvanchuxiuzhen.system.relation.data;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 关系系统的附件。玩家数据在全部克隆事件中复制（{@code clone_policy.csv}：team、friendlist 为 all_clone_events），只同步给本人；
 * 其他实体的阵营存档但不同步，与来源实体 NBT 一致。
 */
public final class RelationAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<PlayerRelation>> PLAYER = TYPES.register("relation", () -> AttachmentType
            .builder(PlayerRelation::new)
            .serialize(PlayerRelation.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, PlayerRelation.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<Allegiance>> ENTITY = TYPES.register("allegiance", () -> AttachmentType
            .builder(Allegiance::new)
            .serialize(Allegiance.CODEC)
            .build());

    private RelationAttachments() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
