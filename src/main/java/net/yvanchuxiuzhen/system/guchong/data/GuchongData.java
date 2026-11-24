package net.yvanchuxiuzhen.system.guchong.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.api.GuchongState;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTrackingData;

/** 蛊虫系统的强类型物品数据组件。 */
public final class GuchongData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<GuchongTrackingData>> TRACKING = COMPONENTS.registerComponentType(
            "guchong_tracking", builder -> builder.persistent(GuchongTrackingData.CODEC)
                    .networkSynchronized(GuchongTrackingData.STREAM_CODEC));
    public static final Supplier<DataComponentType<GuchongState>> STATE = COMPONENTS.registerComponentType(
            "guchong_state", builder -> builder.persistent(GuchongState.CODEC)
                    .networkSynchronized(GuchongState.STREAM_CODEC));

    private GuchongData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
