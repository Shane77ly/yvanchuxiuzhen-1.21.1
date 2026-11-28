package net.yvanchuxiuzhen.system.npc;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 修士系统管理工具使用的物品数据组件与实体附件。 */
public final class NpcData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<DataComponentType<NpcToolProfile>> TOOL_PROFILE = COMPONENTS.registerComponentType("npc_tool_profile",
            builder -> builder.persistent(NpcToolProfile.CODEC).networkSynchronized(NpcToolProfile.STREAM_CODEC));
    public static final Supplier<AttachmentType<Boolean>> INTERACTIVE_NPC = ATTACHMENTS.register("interactive_npc",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
    public static final Supplier<AttachmentType<NpcFollowState>> FOLLOW = ATTACHMENTS.register("npc_follow",
            () -> AttachmentType.builder(() -> NpcFollowState.EMPTY).serialize(NpcFollowState.CODEC).build());
    public static final Supplier<AttachmentType<NpcDialogueProfile>> PROFILE = ATTACHMENTS.register("npc_profile",
            () -> AttachmentType.builder(() -> NpcDialogueProfile.EMPTY).serialize(NpcDialogueProfile.CODEC).build());

    private NpcData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
