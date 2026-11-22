package net.yvanchuxiuzhen.collection.decor;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 三种余族家具独立登记方块实体，复用来源展示菜单并提供方向库存能力。 */
public final class UtilityFurnitureContent {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<UtilityFurnitureVariant, Supplier<BlockEntityType<UtilityFurnitureBlockEntity>>> TYPES = new EnumMap<>(UtilityFurnitureVariant.class);

    static {
        for (UtilityFurnitureVariant variant : UtilityFurnitureVariant.values()) {
            TYPES.put(variant, BLOCK_ENTITIES.register(variant.id(), () -> BlockEntityType.Builder.of(
                    (pos, state) -> new UtilityFurnitureBlockEntity(variant, pos, state), block(variant)).build(null)));
        }
    }

    private UtilityFurnitureContent() { }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(UtilityFurnitureContent::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (UtilityFurnitureVariant variant : UtilityFurnitureVariant.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type(variant), UtilityFurnitureBlockEntity::handler);
        }
    }

    public static BlockEntityType<UtilityFurnitureBlockEntity> type(UtilityFurnitureVariant variant) { return TYPES.get(variant).get(); }
    public static Block block(UtilityFurnitureVariant variant) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, variant.id()));
    }
    public static Item cup() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zisha_02"));
    }
}
