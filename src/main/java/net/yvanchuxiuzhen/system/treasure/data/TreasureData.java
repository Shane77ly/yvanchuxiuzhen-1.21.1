package net.yvanchuxiuzhen.system.treasure.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.api.CasketCommand;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;

/** 宝箱系统的强类型物品数据组件。 */
public final class TreasureData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<TreasureContainerConfig>> CONTAINER = COMPONENTS.registerComponentType(
            "treasure_container", builder -> builder.persistent(TreasureContainerConfig.CODEC)
                    .networkSynchronized(TreasureContainerConfig.STREAM_CODEC));
    public static final Supplier<DataComponentType<CasketCommand>> CASKET_COMMAND = COMPONENTS.registerComponentType(
            "casket_command", builder -> builder.persistent(CasketCommand.CODEC)
                    .networkSynchronized(CasketCommand.STREAM_CODEC));

    private TreasureData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
