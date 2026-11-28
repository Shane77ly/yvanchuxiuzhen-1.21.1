package net.yvanchuxiuzhen.collection.decor;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 热油的源、流动态、液体方块及滚油桶。地灵地物按方块 ID 使用源状态。 */
public final class HotOil {
    private static final String ID = "hotoil";
    private static final ResourceLocation BLOCK_TAB = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tabyvanchu_block");
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ModId.VALUE);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ModId.VALUE);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModId.VALUE);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModId.VALUE);

    public static final Supplier<FluidType> TYPE = TYPES.register(ID, () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid." + ModId.VALUE + "." + ID).density(850).temperature(550)
            .canExtinguish(true).fallDistanceModifier(0F).supportsBoating(true).canHydrate(true)));
    public static final Supplier<FlowingFluid> SOURCE = FLUIDS.register(ID, () -> new BaseFlowingFluid.Source(properties()));
    public static final Supplier<FlowingFluid> FLOWING = FLUIDS.register("flowing_hotoil", () -> new BaseFlowingFluid.Flowing(properties()));
    private static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register(ID, OilBlock::new);
    private static final DeferredItem<BucketItem> BUCKET = ITEMS.register("hotoil_bucket", () -> new BucketItem(SOURCE.get(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private HotOil() {
    }

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING).explosionResistance(100F).bucket(BUCKET).block(BLOCK);
    }

    static void register(IEventBus modBus) {
        TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(HotOil::addToTab);
    }

    private static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(BLOCK_TAB)) {
            event.accept(BUCKET.get());
        }
    }

    private static final class OilBlock extends LiquidBlock {
        private OilBlock() {
            super(SOURCE.get(), BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE)
                    .replaceable().noCollission().strength(100F).pushReaction(PushReaction.DESTROY).liquid());
        }

        @Override
        public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
            return 100;
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            if (!(level instanceof ServerLevel server)) return;

            double height = entity.getY() + entity.getBbHeight() / 2;
            double width = entity.getBbWidth() / 2;
            server.sendParticles(ParticleTypes.SMOKE, entity.getX(), height, entity.getZ(), 1, width, 0.5, width, 0.1);
            server.sendParticles(ParticleTypes.CLOUD, entity.getX(), height, entity.getZ(), 1, width, 0.5, width, 0.1);
            server.sendParticles(Particles.type("hotoillizi"), entity.getX(), height, entity.getZ(), 2, width, 0.5, width, 0.1);

            double ticks = entity.getData(DecorData.HOT_OIL_CONTACT_TICKS) + 1;
            if (ticks < 10) {
                entity.setData(DecorData.HOT_OIL_CONTACT_TICKS, ticks);
                return;
            }

            boolean markAttack = true;
            if (entity instanceof ServerPlayer player) {
                server.playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.NEUTRAL, 1, 1);
                GameType mode = player.gameMode.getGameModeForPlayer();
                markAttack = mode == GameType.SURVIVAL || mode == GameType.ADVENTURE;
            }
            if (markAttack) SpellDamage.markHotOilContact(entity);
            entity.setData(DecorData.HOT_OIL_CONTACT_TICKS, 0.0);
        }
    }
}
