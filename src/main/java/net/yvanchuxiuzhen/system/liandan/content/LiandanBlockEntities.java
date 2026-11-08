package net.yvanchuxiuzhen.system.liandan.content;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 丹炉与药瓶分别保留来源方块实体身份，全部方向及无方向查询均开放自身库存。 */
public final class LiandanBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final List<Supplier<BlockEntityType<AlchemyBlockEntity>>> ALCHEMY = IntStream.rangeClosed(1, 8)
            .mapToObj(tier -> registerType("alchemy_0" + tier)).toList();
    private static final List<String> MEDICINE_VARIANTS = List.of("01", "02", "03", "04", "0401", "05", "0501", "06");
    public static final List<Supplier<BlockEntityType<MedicineContainerBlockEntity>>> MEDICINE_CONTAINERS = MEDICINE_VARIANTS.stream()
            .map(variant -> registerMedicineType("itemdanyaohulu_" + variant + "block")).toList();

    private LiandanBlockEntities() {
    }

    private static Supplier<BlockEntityType<AlchemyBlockEntity>> registerType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(AlchemyBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<AlchemyBlockEntity> type(int tier) {
        return ALCHEMY.get(tier - 1).get();
    }

    private static Supplier<BlockEntityType<MedicineContainerBlockEntity>> registerMedicineType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(MedicineContainerBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<MedicineContainerBlockEntity> medicineType(String variant) {
        return MEDICINE_CONTAINERS.get(MEDICINE_VARIANTS.indexOf(variant)).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(LiandanBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<AlchemyBlockEntity>> type : ALCHEMY) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), AlchemyBlockEntity::handler);
        }
        for (Supplier<BlockEntityType<MedicineContainerBlockEntity>> type : MEDICINE_CONTAINERS) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), MedicineContainerBlockEntity::handler);
        }
    }
}
