package net.yvanchuxiuzhen.system.world.portal;

import java.util.EnumMap;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 为六组门户登记独立 POI；门户方块注册完成后由供应器读取其全部状态。 */
public final class DimensionPortalTypes {
    private static final DeferredRegister<PoiType> TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, ModId.VALUE);
    private static final EnumMap<DimensionPortalSpec, DeferredHolder<PoiType, PoiType>> HOLDERS = new EnumMap<>(DimensionPortalSpec.class);

    static {
        for (DimensionPortalSpec spec : DimensionPortalSpec.values()) {
            HOLDERS.put(spec, TYPES.register(spec.portalId(), () -> new PoiType(states(spec), 0, 1)));
        }
    }

    private DimensionPortalTypes() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }

    public static Holder<PoiType> holder(DimensionPortalSpec spec) {
        return HOLDERS.get(spec);
    }

    private static Set<BlockState> states(DimensionPortalSpec spec) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, spec.portalId()));
        return Set.copyOf(block.getStateDefinition().getPossibleStates());
    }
}
