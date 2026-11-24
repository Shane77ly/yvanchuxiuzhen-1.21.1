package net.yvanchuxiuzhen.system.craft.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 工艺系统陶坯成品映射等物品数据组件。 */
public final class CraftData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<String>> POTTERY_PRODUCT = COMPONENTS.registerComponentType("pottery_product",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private CraftData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
