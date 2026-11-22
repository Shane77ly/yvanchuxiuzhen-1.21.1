package net.yvanchuxiuzhen.collection.decor;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 地淌云的源、流动态、液体方块及地淌云桶。 */
public final class WaterCloud {
    private static final String ID = "watercloud";
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ModId.VALUE);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ModId.VALUE);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModId.VALUE);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModId.VALUE);

    public static final Supplier<FluidType> TYPE = TYPES.register(ID, () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid." + ModId.VALUE + "." + ID).density(-5).viscosity(0).temperature(200)));
    public static final Supplier<FlowingFluid> SOURCE = FLUIDS.register(ID, WaterCloudFluid.Source::new);
    public static final Supplier<FlowingFluid> FLOWING = FLUIDS.register("flowing_watercloud", WaterCloudFluid.Flowing::new);
    private static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register(ID, () -> new LiquidBlock(SOURCE.get(),
            BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable().noCollission().strength(10F)
                    .pushReaction(PushReaction.DESTROY).liquid()));
    private static final DeferredItem<BucketItem> BUCKET = ITEMS.register("watercloud_bucket", () -> new BucketItem(SOURCE.get(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private WaterCloud() {
    }

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING)
                .explosionResistance(10F).tickRate(15).bucket(BUCKET).block(BLOCK);
    }

    static void register(IEventBus modBus) {
        TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(WaterCloud::addToTab);
    }

    private static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(BUCKET.get());
        }
    }

    private abstract static class WaterCloudFluid extends BaseFlowingFluid {
        private WaterCloudFluid() {
            super(properties());
        }

        @Override
        public ParticleOptions getDripParticle() {
            return ParticleTypes.CLOUD;
        }

        @Override
        public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
            return Vec3.ZERO;
        }

        private static final class Source extends WaterCloudFluid {
            @Override
            public int getAmount(FluidState state) {
                return 8;
            }

            @Override
            public boolean isSource(FluidState state) {
                return true;
            }
        }

        private static final class Flowing extends WaterCloudFluid {
            @Override
            protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
                super.createFluidStateDefinition(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getAmount(FluidState state) {
                return state.getValue(LEVEL);
            }

            @Override
            public boolean isSource(FluidState state) {
                return false;
            }
        }
    }
}
