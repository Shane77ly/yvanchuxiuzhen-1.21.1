package net.yvanchuxiuzhen.collection.decor;

import java.util.HashMap;
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

/** 文字装饰登记十八个实体、三个来源菜单和零槽物品能力。 */
public final class InscribedDecorContent {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    private static final Map<String, Supplier<BlockEntityType<InscribedDecorBlockEntity>>> TYPES = new HashMap<>();
    private static final Map<InscribedDecorMenu.Kind, Supplier<MenuType<InscribedDecorMenu>>> MENU_TYPES = new HashMap<>();

    static {
        for (InscribedDecorVariant variant : InscribedDecorVariant.VALUES) {
            TYPES.put(variant.id(), BLOCK_ENTITIES.register(variant.id(), () -> BlockEntityType.Builder.of(
                    (pos, state) -> new InscribedDecorBlockEntity(variant, pos, state), block(variant.id())).build(null)));
        }
        for (InscribedDecorMenu.Kind kind : InscribedDecorMenu.Kind.values()) {
            MENU_TYPES.put(kind, MENUS.register(kind.id(), () -> IMenuTypeExtension.create(
                    (id, inventory, buffer) -> new InscribedDecorMenu(kind, id, inventory, buffer.readBlockPos()))));
        }
    }

    private InscribedDecorContent() { }
    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        bus.addListener(InscribedDecorContent::capabilities);
        bus.addListener(InscribedDecorPayload::register);
    }
    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (InscribedDecorVariant variant : InscribedDecorVariant.VALUES) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type(variant), InscribedDecorBlockEntity::handler);
        }
    }
    public static BlockEntityType<InscribedDecorBlockEntity> type(InscribedDecorVariant variant) { return TYPES.get(variant.id()).get(); }
    public static MenuType<InscribedDecorMenu> menu(InscribedDecorMenu.Kind kind) { return MENU_TYPES.get(kind).get(); }
    public static Block block(String id) { return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)); }
}
