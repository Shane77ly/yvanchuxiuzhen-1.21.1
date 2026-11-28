package net.yvanchuxiuzhen.system.cooking.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.api.CookingRecipeData;
import net.yvanchuxiuzhen.system.cooking.api.WineData;

/** 烹饪系统的强类型物品数据组件注册。 */
public final class CookingData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<WineData>> WINE = COMPONENTS.registerComponentType("wine",
            builder -> builder.persistent(WineData.CODEC).networkSynchronized(WineData.STREAM_CODEC));
    public static final Supplier<DataComponentType<CookingRecipeData>> RECIPE = COMPONENTS.registerComponentType("cooking_recipe",
            builder -> builder.persistent(CookingRecipeData.CODEC).networkSynchronized(CookingRecipeData.STREAM_CODEC));

    private CookingData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
