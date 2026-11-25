package net.yvanchuxiuzhen.system.treasure.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.api.CasketCommand;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;

/** 宝箱系统的强类型物品数据组件与方块实体附件。 */
public final class TreasureData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<DataComponentType<TreasureContainerConfig>> CONTAINER = COMPONENTS.registerComponentType(
            "treasure_container", builder -> builder.persistent(TreasureContainerConfig.CODEC)
                    .networkSynchronized(TreasureContainerConfig.STREAM_CODEC));
    public static final Supplier<DataComponentType<CasketCommand>> CASKET_COMMAND = COMPONENTS.registerComponentType(
            "casket_command", builder -> builder.persistent(CasketCommand.CODEC)
                    .networkSynchronized(CasketCommand.STREAM_CODEC));
    public static final Supplier<AttachmentType<TreasureContainerConfig>> BLOCK_CONTAINER = ATTACHMENTS.register(
            "block_treasure_container", () -> AttachmentType.builder(() -> TreasureContainerConfig.EMPTY)
                    .serialize(TreasureContainerConfig.CODEC).build());

    private TreasureData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
