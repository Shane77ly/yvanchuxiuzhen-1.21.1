package net.yvanchuxiuzhen.system.world.fluid;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 温泉流体、流动温泉、液体方块及温泉水桶的注册装配。 */
public final class SpringFluids {
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ModId.VALUE);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ModId.VALUE);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModId.VALUE);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModId.VALUE);

    public static final Supplier<FluidType> TYPE = TYPES.register("wenqvan", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid." + ModId.VALUE + ".wenqvan").rarity(Rarity.UNCOMMON)
            .density(1000).viscosity(1000).temperature(300).canConvertToSource(false)
            .canExtinguish(true).fallDistanceModifier(0F).supportsBoating(true).canHydrate(true)));
    public static final Supplier<FlowingFluid> SOURCE = FLUIDS.register("wenqvan", SpringFluid.Source::new);
    public static final Supplier<FlowingFluid> FLOWING = FLUIDS.register("flowing_wenqvan", SpringFluid.Flowing::new);
    public static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register("wenqvan", SpringLiquidBlock::new);
    public static final DeferredItem<BucketItem> BUCKET = ITEMS.register("wenqvan_bucket", () -> new BucketItem(SOURCE.get(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.UNCOMMON)));

    private SpringFluids() {
    }

    static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING).explosionResistance(100F)
                .slopeFindDistance(4).levelDecreasePerBlock(1).tickRate(5).bucket(BUCKET).block(BLOCK);
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(SpringFluids::addToTab);
    }

    private static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().getNamespace().equals(ModId.VALUE)
                && event.getTabKey().location().getPath().equals("tabyvanchu_materials")) {
            event.accept(BUCKET.get());
        }
    }
}
