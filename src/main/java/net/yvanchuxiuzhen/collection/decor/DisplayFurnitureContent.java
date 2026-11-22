package net.yvanchuxiuzhen.collection.decor;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 展示家具登记六个方块实体、两个来源菜单及方向库存能力。 */
public final class DisplayFurnitureContent {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    private static final Map<DisplayFurnitureVariant, Supplier<BlockEntityType<DisplayFurnitureBlockEntity>>> TYPES = new EnumMap<>(DisplayFurnitureVariant.class);
    public static final Supplier<MenuType<DisplayFurnitureMenu>> CABINET = MENUS.register("beechfurniture_03_gui", () ->
            IMenuTypeExtension.create((id, inventory, buffer) -> new DisplayFurnitureMenu(true, id, inventory, buffer.readBlockPos())));
    public static final Supplier<MenuType<DisplayFurnitureMenu>> STAND = MENUS.register("beechfurniture_15_gui", () ->
            IMenuTypeExtension.create((id, inventory, buffer) -> new DisplayFurnitureMenu(false, id, inventory, buffer.readBlockPos())));

    static {
        for (DisplayFurnitureVariant variant : DisplayFurnitureVariant.values()) {
            TYPES.put(variant, BLOCK_ENTITIES.register(variant.id(), () -> BlockEntityType.Builder.of(
                    (pos, state) -> new DisplayFurnitureBlockEntity(variant, pos, state), block(variant)).build(null)));
        }
    }

    private DisplayFurnitureContent() { }
    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(DisplayFurnitureContent::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (DisplayFurnitureVariant variant : DisplayFurnitureVariant.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type(variant), DisplayFurnitureBlockEntity::handler);
        }
    }

    public static BlockEntityType<DisplayFurnitureBlockEntity> type(DisplayFurnitureVariant variant) { return TYPES.get(variant).get(); }
    public static Block block(DisplayFurnitureVariant variant) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, variant.id()));
    }
}
