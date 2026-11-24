package net.yvanchuxiuzhen.system.world.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.content.CompassAnchor;

/** 世界系统物品数据组件。 */
public final class WorldData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<CompassAnchor>> COMPASS_ANCHOR = COMPONENTS.registerComponentType("compass_anchor",
            builder -> builder.persistent(CompassAnchor.CODEC).networkSynchronized(CompassAnchor.STREAM_CODEC));

    private WorldData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
