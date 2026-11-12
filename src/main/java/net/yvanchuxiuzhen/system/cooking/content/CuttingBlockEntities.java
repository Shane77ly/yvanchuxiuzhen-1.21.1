package net.yvanchuxiuzhen.system.cooking.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 案板和菜刀保持两个来源方块实体身份及独立库存能力。 */
public final class CuttingBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<CuttingBlockEntity>> BOARD = register("anban");
    public static final Supplier<BlockEntityType<CuttingBlockEntity>> CLEAVER = register("caidaoblock");

    private CuttingBlockEntities() {
    }

    private static Supplier<BlockEntityType<CuttingBlockEntity>> register(String path) {
        return TYPES.register(path, () -> BlockEntityType.Builder.of(CuttingBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path))).build(null));
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BOARD.get(), CuttingBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CLEAVER.get(), CuttingBlockEntity::handler);
        });
    }

    public static BlockEntityType<CuttingBlockEntity> type(BlockState state) {
        return state.getBlock() instanceof CuttingBoardBlock ? BOARD.get() : CLEAVER.get();
    }
}
