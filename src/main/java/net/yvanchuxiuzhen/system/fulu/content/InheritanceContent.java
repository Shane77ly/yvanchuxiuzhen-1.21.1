package net.yvanchuxiuzhen.system.fulu.content;

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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.network.InheritanceButtonPayload;

/** 传承之光的方块实体、管理菜单和按钮消息装配。 */
public final class InheritanceContent {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<BlockEntityType<InheritanceBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register("chuancheng",
            () -> BlockEntityType.Builder.of(InheritanceBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chuancheng"))).build(null));
    public static final Supplier<MenuType<InheritanceMenu>> MENU = MENUS.register("chuancheng_gui", () -> IMenuTypeExtension.create(InheritanceMenu::new));

    private InheritanceContent() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, BLOCK_ENTITY.get(), InheritanceBlockEntity::handler));
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToServer(
                InheritanceButtonPayload.TYPE, InheritanceButtonPayload.STREAM_CODEC, InheritanceButtonPayload::handle));
    }
}
