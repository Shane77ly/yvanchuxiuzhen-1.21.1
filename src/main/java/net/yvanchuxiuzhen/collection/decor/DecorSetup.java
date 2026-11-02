package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 装饰内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class DecorSetup {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModId.VALUE);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModId.VALUE);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ModId.VALUE);

    private DecorSetup() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
    }
}
