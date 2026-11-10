package net.yvanchuxiuzhen.system.craft.content;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 工艺设备分别保留方块实体注册身份，提供无方向库存与六方向自动化库存。 */
public final class CraftBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<String, Supplier<BlockEntityType<StoneMillBlockEntity>>> MILLS = Map.of(
            "stonemill", registerType("stonemill"),
            "stoneroller", registerType("stoneroller"));
    public static final Supplier<BlockEntityType<LoomBlockEntity>> LOOM = TYPES.register("zhibuji",
            () -> BlockEntityType.Builder.of(LoomBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zhibuji"))).build(null));

    public static final Supplier<BlockEntityType<CarpentryBlockEntity>> CARPENTRY = TYPES.register("mugongtai",
            () -> BlockEntityType.Builder.of(CarpentryBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "mugongtai"))).build(null));

    public static final Supplier<BlockEntityType<SilkReelBlockEntity>> SILK_REEL = TYPES.register("saosilun",
            () -> BlockEntityType.Builder.of(SilkReelBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "saosilun"))).build(null));

    public static final Supplier<BlockEntityType<PotteryWheelBlockEntity>> POTTERY_WHEEL = TYPES.register("taolun",
            () -> BlockEntityType.Builder.of(PotteryWheelBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taolun"))).build(null));

    public static final Supplier<BlockEntityType<PotteryBlankBlockEntity>> POTTERY_BLANK = TYPES.register("taopi",
            () -> BlockEntityType.Builder.of(PotteryBlankBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taopi"))).build(null));

    public static final Supplier<BlockEntityType<SilkwormBoxBlockEntity>> SILKWORM_BOX = TYPES.register("canhe",
            () -> BlockEntityType.Builder.of(SilkwormBoxBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "canhe"))).build(null));

    private CraftBlockEntities() {
    }

    private static Supplier<BlockEntityType<StoneMillBlockEntity>> registerType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(StoneMillBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<StoneMillBlockEntity> type(String id) {
        return MILLS.get(id).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(CraftBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<StoneMillBlockEntity>> type : MILLS.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), StoneMillBlockEntity::handler);
        }
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CARPENTRY.get(), CarpentryBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SILK_REEL.get(), SilkReelBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, LOOM.get(), LoomBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, POTTERY_WHEEL.get(), PotteryWheelBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, POTTERY_BLANK.get(), PotteryBlankBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SILKWORM_BOX.get(), SilkwormBoxBlockEntity::handler);
    }
}
