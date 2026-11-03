package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 血流（来源 {@code XveliuFluid}、{@code XveliuBlock}、{@code XveliuItem}）：源与流动流体 {@code xveliu}、{@code flowing_xveliu}，
 * 液体方块 {@code xveliu} 与桶 {@code xveliu_bucket}。密度 2000、黏度 5000、爆炸抗性 100、每格衰减 2、坡度搜索 2。
 * 来源把血流列入 {@code minecraft:water} 流体标签，实体在其中按水处理；1.21.1 的实体按流体类型判断，
 * 因此流体类型取水的行为：可游泳、可溺水、灭火、清除摔落距离、可载船、可湿润耕地，桶的装取声音同水。
 */
public final class XiulianFluids {
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ModId.VALUE);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ModId.VALUE);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModId.VALUE);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModId.VALUE);

    public static final Supplier<FluidType> BLOOD_TYPE = TYPES.register("xveliu", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid." + ModId.VALUE + ".xveliu")
            .density(2000).viscosity(5000)
            .canExtinguish(true).fallDistanceModifier(0F).supportsBoating(true).canHydrate(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));
    public static final Supplier<FlowingFluid> BLOOD = FLUIDS.register("xveliu", () -> new BaseFlowingFluid.Source(properties()));
    public static final Supplier<FlowingFluid> FLOWING_BLOOD = FLUIDS.register("flowing_xveliu", () -> new BaseFlowingFluid.Flowing(properties()));
    public static final DeferredBlock<LiquidBlock> BLOOD_BLOCK = BLOCKS.register("xveliu", () -> new LiquidBlock(BLOOD.get(),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).replaceable().noCollission().strength(100F)
                    .pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));
    public static final DeferredItem<BucketItem> BLOOD_BUCKET = ITEMS.register("xveliu_bucket", () -> new BucketItem(BLOOD.get(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private XiulianFluids() {
    }

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(BLOOD_TYPE, BLOOD, FLOWING_BLOOD)
                .explosionResistance(100F).levelDecreasePerBlock(2).slopeFindDistance(2)
                .bucket(BLOOD_BUCKET).block(BLOOD_BLOCK);
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(XiulianFluids::addToTab);
    }

    /** 来源的桶在原版“杂项”栏，1.21.1 对应“工具与实用物品”栏（与内容表的栏位换算一致）。 */
    private static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BLOOD_BUCKET.get());
        }
    }
}
