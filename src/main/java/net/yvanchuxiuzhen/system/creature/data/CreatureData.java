package net.yvanchuxiuzhen.system.creature.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBagData;

/** 生物系统的强类型物品数据组件。 */
public final class CreatureData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<SpiritBeastBagData>> SPIRIT_BEAST_BAG = COMPONENTS.registerComponentType(
            "spirit_beast_bag", builder -> builder.persistent(SpiritBeastBagData.CODEC)
                    .networkSynchronized(SpiritBeastBagData.STREAM_CODEC));
    public static final Supplier<DataComponentType<Double>> TAME_VALUE = COMPONENTS.registerComponentType(
            "tame_value", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    private CreatureData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
