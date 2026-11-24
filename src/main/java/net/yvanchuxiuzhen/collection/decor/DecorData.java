package net.yvanchuxiuzhen.collection.decor;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 装饰合集使用的结构化物品组件。 */
public final class DecorData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<PlaqueInscription>> PLAQUE_INSCRIPTION =
            COMPONENTS.registerComponentType("plaque_inscription",
                    builder -> builder.persistent(PlaqueInscription.CODEC).networkSynchronized(PlaqueInscription.STREAM_CODEC));
    public static final Supplier<DataComponentType<CritterCageData>> CRITTER_CAGE =
            COMPONENTS.registerComponentType("critter_cage",
                    builder -> builder.persistent(CritterCageData.CODEC).networkSynchronized(CritterCageData.STREAM_CODEC));
    public static final Supplier<DataComponentType<RoofBrushConfig>> ROOF_BRUSH =
            COMPONENTS.registerComponentType("roof_brush",
                    builder -> builder.persistent(RoofBrushConfig.CODEC).networkSynchronized(RoofBrushConfig.STREAM_CODEC));

    private DecorData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
