package net.yvanchuxiuzhen.system.npc;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 修士系统管理工具使用的物品数据组件。 */
public final class NpcData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<NpcToolProfile>> TOOL_PROFILE = COMPONENTS.registerComponentType("npc_tool_profile",
            builder -> builder.persistent(NpcToolProfile.CODEC).networkSynchronized(NpcToolProfile.STREAM_CODEC));

    private NpcData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
