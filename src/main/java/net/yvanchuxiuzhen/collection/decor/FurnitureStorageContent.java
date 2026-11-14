package net.yvanchuxiuzhen.collection.decor;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.collection.decor.FurnitureStorageVariant.Layout;
import net.yvanchuxiuzhen.platform.ModId;

/** 储物家具的独立方块实体身份、物品能力和三种菜单注册。 */
public final class FurnitureStorageContent {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    private static final Map<FurnitureStorageVariant, Supplier<BlockEntityType<FurnitureStorageBlockEntity>>> TYPES = new EnumMap<>(FurnitureStorageVariant.class);
    private static final Map<Layout, Supplier<MenuType<FurnitureStorageMenu>>> MENU_TYPES = new EnumMap<>(Layout.class);

    static {
        for (FurnitureStorageVariant variant : FurnitureStorageVariant.values()) {
            TYPES.put(variant, BLOCK_ENTITIES.register(variant.id(), () -> BlockEntityType.Builder.of(
                    (pos, state) -> new FurnitureStorageBlockEntity(variant, pos, state),
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, variant.id()))).build(null)));
        }
        for (Layout layout : Layout.values()) {
            MENU_TYPES.put(layout, MENUS.register(layout.menuId(), () -> IMenuTypeExtension.create(
                    (id, inventory, buffer) -> new FurnitureStorageMenu(layout, id, inventory, buffer))));
        }
    }

    private FurnitureStorageContent() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(FurnitureStorageContent::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (FurnitureStorageVariant variant : FurnitureStorageVariant.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, blockEntityType(variant), FurnitureStorageBlockEntity::handler);
        }
    }

    public static BlockEntityType<FurnitureStorageBlockEntity> blockEntityType(FurnitureStorageVariant variant) {
        return TYPES.get(variant).get();
    }

    public static MenuType<FurnitureStorageMenu> menuType(Layout layout) {
        return MENU_TYPES.get(layout).get();
    }
}
