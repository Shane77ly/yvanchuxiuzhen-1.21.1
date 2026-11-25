package net.yvanchuxiuzhen.system.xiulian.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 修炼系统的实体附件。玩家修炼在所有克隆事件中复制，只同步给本人；非玩家神识计数随实体保存。
 */
public final class XiulianAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<CultivationData>> CULTIVATION = TYPES.register("cultivation", () -> AttachmentType
            .builder(CultivationData::new)
            .serialize(CultivationData.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, CultivationData.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<Double>> ENTITY_SOUL = TYPES.register("entity_soul", () -> AttachmentType
            .builder(() -> 0.0)
            .serialize(Codec.DOUBLE)
            .build());

    private XiulianAttachments() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
