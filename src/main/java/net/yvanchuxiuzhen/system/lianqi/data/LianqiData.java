package net.yvanchuxiuzhen.system.lianqi.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;

/** 炼器系统的强类型图纸数据组件注册。 */
public final class LianqiData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<LianqiBlueprint>> BLUEPRINT = COMPONENTS.registerComponentType("lianqi_blueprint",
            builder -> builder.persistent(LianqiBlueprint.CODEC).networkSynchronized(LianqiBlueprint.STREAM_CODEC));

    private LianqiData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
