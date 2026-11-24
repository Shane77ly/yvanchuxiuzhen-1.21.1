package net.yvanchuxiuzhen.collection.festival;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 节庆合集使用的物品组件，记录手持烟花状态与财神衣累计刻。 */
public final class FestivalData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<HandheldFireworkState>> FIREWORK_STATE =
            COMPONENTS.registerComponentType("firework_state",
                    builder -> builder.persistent(HandheldFireworkState.CODEC).networkSynchronized(HandheldFireworkState.STREAM_CODEC));
    public static final Supplier<DataComponentType<Double>> COSTUME_TICK =
            COMPONENTS.registerComponentType("costume_tick",
                    builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    private FestivalData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
