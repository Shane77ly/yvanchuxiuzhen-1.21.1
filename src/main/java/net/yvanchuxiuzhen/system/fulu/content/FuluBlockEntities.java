package net.yvanchuxiuzhen.system.fulu.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 制符装置保持来源方块实体身份和各自的库存方向规则。 */
public final class FuluBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<DrawingTableBlockEntity>> DRAWING_TABLE = TYPES.register("huafutai",
            () -> BlockEntityType.Builder.of(DrawingTableBlockEntity::new, block("huafutai")).build(null));
    public static final Supplier<BlockEntityType<AshBurnerBlockEntity>> ASH_BURNER = TYPES.register("huafushaohui",
            () -> BlockEntityType.Builder.of(AshBurnerBlockEntity::new, block("huafushaohui")).build(null));

    private FuluBlockEntities() {
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(FuluBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DRAWING_TABLE.get(), DrawingTableBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ASH_BURNER.get(), AshBurnerBlockEntity::handler);
    }
}
