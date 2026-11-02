package net.yvanchuxiuzhen.family.decor;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.Namespaces;

/**
 * 装饰内容族的启动装配，只在 Mod 构造时调用一次。装饰内容来自原初系统，使用其命名空间。
 */
public final class DecorSetup {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Namespaces.XITONG);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Namespaces.XITONG);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Namespaces.XITONG);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Namespaces.XITONG);

    private DecorSetup() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
    }
}
