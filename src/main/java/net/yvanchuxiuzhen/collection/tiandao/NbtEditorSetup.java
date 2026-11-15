package net.yvanchuxiuzhen.collection.tiandao;

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
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 通用物品NBT编辑器的专属方块工厂、单槽实体、菜单与服务端编辑消息。 */
public final class NbtEditorSetup {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<BlockEntityType<NbtEditorBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register("tiandaoitemnbt", () -> BlockEntityType.Builder.of(
            NbtEditorBlockEntity::new, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandaoitemnbt"))).build(null));
    public static final Supplier<MenuType<NbtEditorMenu>> MENU = MENUS.register("tiandaoitemnbt_gui", () -> IMenuTypeExtension.create(NbtEditorMenu::new));

    private NbtEditorSetup() {
    }

    public static ContentTables configure(ContentTables content) {
        return content.blockFactory("tiandao:nbt_editor", traits -> ContentBlock.create(traits, NbtEditorBlock::new));
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(NbtEditorSetup::capabilities);
        modBus.addListener(NbtEditorPayload::register);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BLOCK_ENTITY.get(), NbtEditorBlockEntity::handler);
    }
}
