package net.yvanchuxiuzhen.system.fulu.data;

import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.api.FuluKnowledge;

/** 符箓知识由本系统写入，随玩家存盘并在全部克隆事件保留，只同步给本人。 */
public final class FuluData {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<FuluKnowledge>> KNOWLEDGE = ATTACHMENTS.register("fulu_knowledge", () -> AttachmentType
            .builder(() -> FuluKnowledge.INITIAL)
            .serialize(FuluKnowledge.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodec(FuluKnowledge.CODEC))
            .build());

    private FuluData() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
