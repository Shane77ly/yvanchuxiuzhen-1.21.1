package net.yvanchuxiuzhen.system.lianqi.content;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 炼器装置分别保留方块实体身份，并提供对应方向的物品库存能力。 */
public final class LianqiBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final List<Supplier<BlockEntityType<TripodBlockEntity>>> TRIPODS = IntStream.rangeClosed(1, 8)
            .mapToObj(tier -> registerType("tripod_0" + tier)).toList();
    public static final Supplier<BlockEntityType<RepairTableBlockEntity>> REPAIR_TABLE = TYPES.register("xiulitai",
            () -> BlockEntityType.Builder.of(RepairTableBlockEntity::new, block("xiulitai")).build(null));
    public static final Supplier<BlockEntityType<SewingBasketBlockEntity>> SEWING_BASKET = TYPES.register("zhenxianpoluo",
            () -> BlockEntityType.Builder.of(SewingBasketBlockEntity::new, block("zhenxianpoluo")).build(null));
    public static final Supplier<BlockEntityType<BlueprintTableBlockEntity>> BLUEPRINT_TABLE = TYPES.register("tiandaotuzhitai",
            () -> BlockEntityType.Builder.of(BlueprintTableBlockEntity::new, block("tiandaotuzhitai")).build(null));
    public static final Supplier<BlockEntityType<PoisonCauldronBlockEntity>> POISON_CAULDRON = TYPES.register("cauldronduyao",
            () -> BlockEntityType.Builder.of(PoisonCauldronBlockEntity::new, block("cauldronduyao")).build(null));

    private LianqiBlockEntities() {
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    private static Supplier<BlockEntityType<TripodBlockEntity>> registerType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(TripodBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<TripodBlockEntity> type(int tier) {
        return TRIPODS.get(tier - 1).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(LianqiBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, REPAIR_TABLE.get(), RepairTableBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SEWING_BASKET.get(), SewingBasketBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BLUEPRINT_TABLE.get(), BlueprintTableBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, POISON_CAULDRON.get(), PoisonCauldronBlockEntity::handler);
        for (Supplier<BlockEntityType<TripodBlockEntity>> type : TRIPODS) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), TripodBlockEntity::handler);
        }
    }
}
