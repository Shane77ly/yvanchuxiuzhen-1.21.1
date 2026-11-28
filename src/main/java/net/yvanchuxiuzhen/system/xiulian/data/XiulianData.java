package net.yvanchuxiuzhen.system.xiulian.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.SpiritDiscData;

/** 修炼系统拥有的物品实例组件，包含传承/家族补给计数与出身石盘状态。 */
public final class XiulianData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<SpiritDiscData>> SPIRIT_DISC = COMPONENTS.registerComponentType("spirit_disc",
            builder -> builder.persistent(SpiritDiscData.CODEC).networkSynchronized(SpiritDiscData.STREAM_CODEC));
    public static final Supplier<DataComponentType<Double>> SUPPLY_COUNT = COMPONENTS.registerComponentType("supply_count",
            builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    private XiulianData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
