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

/** 蒸锅和笼屉保持两个来源方块实体身份及独立库存能力。 */
public final class SteamerBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<SteamerBlockEntity>> POT = register("foodzhengguo");
    public static final Supplier<BlockEntityType<SteamerBlockEntity>> TRAY = register("foodlongti");

    private SteamerBlockEntities() {
    }

    private static Supplier<BlockEntityType<SteamerBlockEntity>> register(String path) {
        return TYPES.register(path, () -> BlockEntityType.Builder.of(SteamerBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path))).build(null));
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, POT.get(), SteamerBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TRAY.get(), SteamerBlockEntity::handler);
        });
    }

    public static BlockEntityType<SteamerBlockEntity> type(BlockState state) {
        return ((SteamerBlock) state.getBlock()).tray() ? TRAY.get() : POT.get();
    }
}
