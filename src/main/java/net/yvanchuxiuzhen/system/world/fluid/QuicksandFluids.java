package net.yvanchuxiuzhen.system.world.fluid;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 流沙源、流动态与液体方块的独立注册及来源流动参数。 */
public final class QuicksandFluids {
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ModId.VALUE);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ModId.VALUE);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModId.VALUE);

    public static final Supplier<FluidType> TYPE = TYPES.register("quicksand", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block." + ModId.VALUE + ".quicksand").density(1650).viscosity(1000).temperature(300)
            .canConvertToSource(false).canExtinguish(true).fallDistanceModifier(0F).supportsBoating(true).canHydrate(true)));
    public static final Supplier<FlowingFluid> SOURCE = FLUIDS.register("quicksand", QuicksandFluid.Source::new);
    public static final Supplier<FlowingFluid> FLOWING = FLUIDS.register("flowing_quicksand", QuicksandFluid.Flowing::new);
    public static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register("quicksand", () -> new LiquidBlock(SOURCE.get(),
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).replaceable().noCollission()
                    .strength(100F).pushReaction(PushReaction.DESTROY).liquid()));

    private QuicksandFluids() {
    }

    static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING).explosionResistance(100F)
                .slopeFindDistance(2).levelDecreasePerBlock(2).tickRate(5).block(BLOCK);
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
    }
}
