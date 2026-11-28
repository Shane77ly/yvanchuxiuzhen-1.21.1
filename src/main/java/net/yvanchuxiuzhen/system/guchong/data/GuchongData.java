package net.yvanchuxiuzhen.system.guchong.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.api.GuchongRuntimeState;
import net.yvanchuxiuzhen.system.guchong.api.GuchongState;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTrackingData;

/** 蛊虫系统的强类型物品数据组件与实体附件。 */
public final class GuchongData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<DataComponentType<GuchongTrackingData>> TRACKING = COMPONENTS.registerComponentType(
            "guchong_tracking", builder -> builder.persistent(GuchongTrackingData.CODEC)
                    .networkSynchronized(GuchongTrackingData.STREAM_CODEC));
    public static final Supplier<DataComponentType<GuchongState>> STATE = COMPONENTS.registerComponentType(
            "guchong_state", builder -> builder.persistent(GuchongState.CODEC)
                    .networkSynchronized(GuchongState.STREAM_CODEC));
    public static final Supplier<AttachmentType<GuchongRuntimeState>> RUNTIME = ATTACHMENTS.register(
            "guchong_runtime", () -> AttachmentType.builder(() -> GuchongRuntimeState.EMPTY)
                    .serialize(GuchongRuntimeState.CODEC).build());
    public static final Supplier<AttachmentType<String>> PROVOKE_TARGET = ATTACHMENTS.register(
            "guchong_provoke_target", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING).build());

    private GuchongData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
