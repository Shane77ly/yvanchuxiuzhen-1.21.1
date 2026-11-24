package net.yvanchuxiuzhen.system.yihuo.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.yihuo.api.DanFireData;

/** 异火与丹火使用的结构化物品组件，由异火系统注册。 */
public final class YihuoData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<DanFireData>> DAN_FIRE = COMPONENTS.registerComponentType("dan_fire",
            builder -> builder.persistent(DanFireData.CODEC).networkSynchronized(DanFireData.STREAM_CODEC));

    private YihuoData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
