package net.yvanchuxiuzhen.system.gongfa.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 功法系统的方块实体与菜单类型：参悟台 {@code canwutai_block} 与参悟界面 {@code canwutai}（来源注册名）。 */
public final class GongfaBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<BlockEntityType<ComprehensionTableBlockEntity>> COMPREHENSION_TABLE = TYPES.register("canwutai_block",
            () -> BlockEntityType.Builder.of(ComprehensionTableBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "canwutai_block"))).build(null));
    public static final Supplier<MenuType<ComprehensionMenu>> COMPREHENSION_MENU = MENUS.register("canwutai",
            () -> IMenuTypeExtension.create(ComprehensionMenu::new));

    private GongfaBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        MENUS.register(modBus);
    }
}
