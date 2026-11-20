package net.yvanchuxiuzhen.system.gongfa.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;

/**
 * 功法系统的数据登记：书上的功法实例组件 {@code technique}，以及玩家的功法栏 {@code technique_slot}
 * （来源 {@code gongfa} 字段，全部克隆事件复制，只同步给本人）；上次功法比较槽仅服务端保存。
 * 克隆保留两个来源物品引用及其已有别名关系，存档仍分别序列化两个槽。
 */
public final class GongfaData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);

    public static final Supplier<DataComponentType<TechniqueBook>> TECHNIQUE = COMPONENTS.registerComponentType("technique",
            builder -> builder.persistent(TechniqueBook.CODEC).networkSynchronized(TechniqueBook.STREAM_CODEC.cast()));
    public static final Supplier<AttachmentType<ItemStack>> SLOT = ATTACHMENTS.register("technique_slot", () -> AttachmentType
            .builder(() -> ItemStack.EMPTY)
            .serialize(ItemStack.OPTIONAL_CODEC)
            .copyOnDeath()
            .copyHandler((stack, holder, provider) -> stack)
            .sync((holder, player) -> holder == player, ItemStack.OPTIONAL_STREAM_CODEC)
            .build());
    /** 来源 gongfalinshi：每秒末尾比较功法种类与名称；随存档、克隆保留，不发送给客户端。 */
    public static final Supplier<AttachmentType<ItemStack>> PREVIOUS_SLOT = ATTACHMENTS.register("previous_technique_slot", () -> AttachmentType
            .builder(() -> ItemStack.EMPTY)
            .serialize(ItemStack.OPTIONAL_CODEC)
            .copyOnDeath()
            .copyHandler((stack, holder, provider) -> stack)
            .build());

    private GongfaData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
