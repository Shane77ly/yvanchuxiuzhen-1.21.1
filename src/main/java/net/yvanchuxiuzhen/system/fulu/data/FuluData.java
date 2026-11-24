package net.yvanchuxiuzhen.system.fulu.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.api.FuluKnowledge;
import net.yvanchuxiuzhen.system.fulu.api.FuluLearningBook;
import net.yvanchuxiuzhen.system.fulu.api.SealedCorpse;
import net.yvanchuxiuzhen.system.fulu.api.TalismanInstance;

/** 符箓物品组件与玩家知识附件注册；由符箓系统统一管理读写。 */
public final class FuluData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);

    public static final Supplier<DataComponentType<TalismanInstance>> TALISMAN = COMPONENTS.registerComponentType("talisman",
            builder -> builder.persistent(TalismanInstance.CODEC).networkSynchronized(TalismanInstance.STREAM_CODEC));
    public static final Supplier<DataComponentType<FuluLearningBook>> LEARNING_BOOK = COMPONENTS.registerComponentType("fulu_learning",
            builder -> builder.persistent(FuluLearningBook.CODEC).networkSynchronized(FuluLearningBook.STREAM_CODEC));
    public static final Supplier<DataComponentType<SealedCorpse>> SEALED_CORPSE = COMPONENTS.registerComponentType("sealed_corpse",
            builder -> builder.persistent(SealedCorpse.CODEC).networkSynchronized(SealedCorpse.STREAM_CODEC));
    public static final Supplier<AttachmentType<FuluKnowledge>> KNOWLEDGE = ATTACHMENTS.register("fulu_knowledge", () -> AttachmentType
            .builder(() -> FuluKnowledge.INITIAL)
            .serialize(FuluKnowledge.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodec(FuluKnowledge.CODEC))
            .build());

    private FuluData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
