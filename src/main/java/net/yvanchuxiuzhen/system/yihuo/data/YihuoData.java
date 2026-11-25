package net.yvanchuxiuzhen.system.yihuo.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.yihuo.api.DanFireData;

/** 异火与丹火使用的结构化物品组件和实体附件，由异火系统注册。 */
public final class YihuoData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<DataComponentType<DanFireData>> DAN_FIRE = COMPONENTS.registerComponentType("dan_fire",
            builder -> builder.persistent(DanFireData.CODEC).networkSynchronized(DanFireData.STREAM_CODEC));
    public static final Supplier<AttachmentType<YihuoBurnState>> BURN_STATE = ATTACHMENTS.register("burn_state",
            () -> AttachmentType.builder(YihuoBurnState::new).serialize(YihuoBurnState.CODEC).build());
    public static final Supplier<AttachmentType<Double>> BLOCK_FIRE_POWER = ATTACHMENTS.register("block_fire_power",
            () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).build());

    private YihuoData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
