package net.yvanchuxiuzhen.system.dungeon.content;

import java.util.List;
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

/** 四类副本方块实体保留独立身份与来源库存容量。 */
public final class DungeonBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<DungeonBlockEntity>> ENTRANCE = type("tiandaofuben");
    public static final Supplier<BlockEntityType<DungeonBlockEntity>> CONTROLLER = type("fubenblock");
    public static final Supplier<BlockEntityType<DungeonBlockEntity>> MARKER = type("fubenentity");
    public static final Supplier<BlockEntityType<DungeonBlockEntity>> PAINTING = type("fabao_009");

    private DungeonBlockEntities() {
    }

    private static Supplier<BlockEntityType<DungeonBlockEntity>> type(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(DungeonBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<DungeonBlockEntity> typeFor(String id) {
        return switch (id) {
            case "tiandaofuben" -> ENTRANCE.get();
            case "fubenblock" -> CONTROLLER.get();
            case "fubenentity" -> MARKER.get();
            case "fabao_009" -> PAINTING.get();
            default -> throw new IllegalArgumentException(id);
        };
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            for (Supplier<BlockEntityType<DungeonBlockEntity>> type : List.of(ENTRANCE, CONTROLLER, MARKER, PAINTING)) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), DungeonBlockEntity::handler);
            }
        });
    }
}
