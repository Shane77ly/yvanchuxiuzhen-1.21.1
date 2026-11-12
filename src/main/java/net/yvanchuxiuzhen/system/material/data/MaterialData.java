package net.yvanchuxiuzhen.system.material.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;

/** 材料物品组件与玩家辨识附件；二者均由材料系统唯一写入。 */
public final class MaterialData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);

    public static final Supplier<DataComponentType<MaterialInstance>> INSTANCE = COMPONENTS.registerComponentType("material_instance",
            builder -> builder.persistent(MaterialInstance.CODEC).networkSynchronized(MaterialInstance.STREAM_CODEC));
    public static final Supplier<AttachmentType<MaterialAnalysis>> ANALYSIS = ATTACHMENTS.register("material_analysis", () -> AttachmentType
            .builder(() -> MaterialAnalysis.EMPTY)
            .serialize(MaterialAnalysis.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodec(MaterialAnalysis.CODEC))
            .build());

    private MaterialData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
