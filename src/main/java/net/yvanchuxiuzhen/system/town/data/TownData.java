package net.yvanchuxiuzhen.system.town.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 城镇系统使用的结构化物品组件。 */
public final class TownData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<TownBuildPlan>> BUILD_PLAN = COMPONENTS.registerComponentType("town_build_plan",
            builder -> builder.persistent(TownBuildPlan.CODEC).networkSynchronized(TownBuildPlan.STREAM_CODEC));

    private TownData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
