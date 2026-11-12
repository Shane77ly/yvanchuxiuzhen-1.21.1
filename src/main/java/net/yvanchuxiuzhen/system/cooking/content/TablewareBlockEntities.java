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

/** 三个来源方块实体注册身份分别绑定餐具库存能力。 */
public final class TablewareBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<TablewareBlockEntity>> BOWL = register("foodwan");
    public static final Supplier<BlockEntityType<TablewareBlockEntity>> PLATE = register("foodcipan");
    public static final Supplier<BlockEntityType<TablewareBlockEntity>> LARGE_BOWL = register("fooddawan");

    private TablewareBlockEntities() {
    }

    private static Supplier<BlockEntityType<TablewareBlockEntity>> register(String path) {
        return TYPES.register(path, () -> BlockEntityType.Builder.of(TablewareBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path))).build(null));
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BOWL.get(), TablewareBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PLATE.get(), TablewareBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, LARGE_BOWL.get(), TablewareBlockEntity::handler);
        });
    }

    public static BlockEntityType<TablewareBlockEntity> type(BlockState state) {
        return switch (((TablewareBlock) state.getBlock()).kind()) {
            case BOWL -> BOWL.get();
            case PLATE -> PLATE.get();
            case LARGE_BOWL -> LARGE_BOWL.get();
        };
    }
}
