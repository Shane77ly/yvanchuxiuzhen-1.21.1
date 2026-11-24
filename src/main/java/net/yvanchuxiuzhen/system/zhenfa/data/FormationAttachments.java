package net.yvanchuxiuzhen.system.zhenfa.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 阳二写在目标方块实体上的罡气及阵旗物品绑定组件，由阵法系统统一拥有。 */
public final class FormationAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<AttachmentType<Integer>> GANGQI = TYPES.register("formation_gangqi", () ->
            AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<DataComponentType<FormationFlagData>> FLAG = COMPONENTS.registerComponentType("formation_flag",
            builder -> builder.persistent(FormationFlagData.CODEC).networkSynchronized(FormationFlagData.STREAM_CODEC));

    private FormationAttachments() { }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        COMPONENTS.register(modBus);
    }
}
