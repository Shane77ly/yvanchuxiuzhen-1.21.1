package net.yvanchuxiuzhen.collection.festival;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/** 方块烟花各自的方块实体身份和零槽库存能力。 */
public final class BlockFireworks {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<BlockFireworkKind, Supplier<BlockEntityType<BlockFireworkBlockEntity>>> TYPES = new EnumMap<>(BlockFireworkKind.class);

    static {
        for (BlockFireworkKind kind : BlockFireworkKind.values()) TYPES.put(kind, BLOCK_ENTITIES.register(kind.id(),
                () -> BlockEntityType.Builder.of((pos, state) -> new BlockFireworkBlockEntity(kind, pos, state),
                        BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, kind.id()))).build(null)));
    }

    private BlockFireworks() { }

    public static void register(ContentTables content, IEventBus modBus) {
        content.blockFactory("festival:pao_10", AttachedFirecrackerBlock::new);
        content.blockFactory("festival:pao_11", traits -> new BlockFirework(traits, BlockFireworkKind.FOUNTAIN));
        content.blockFactory("festival:pao_12", traits -> new BlockFirework(traits, BlockFireworkKind.BATTERY));
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(BlockFireworks::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (BlockFireworkKind kind : BlockFireworkKind.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type(kind), BlockFireworkBlockEntity::handler);
        }
    }

    public static BlockEntityType<BlockFireworkBlockEntity> type(BlockFireworkKind kind) { return TYPES.get(kind).get(); }
}
