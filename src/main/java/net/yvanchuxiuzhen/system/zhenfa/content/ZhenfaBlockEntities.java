package net.yvanchuxiuzhen.system.zhenfa.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 阵元实体沿用来源身份，六面与无方向能力共用两槽库存。 */
public final class ZhenfaBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<FormationElementBlockEntity>> ELEMENT = TYPES.register("zhenyvan",
            () -> BlockEntityType.Builder.of(FormationElementBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zhenyvan"))).build(null));
    public static final Supplier<BlockEntityType<FormationBaseBlockEntity>> BASE = TYPES.register("zhenji",
            () -> BlockEntityType.Builder.of(FormationBaseBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zhenji"))).build(null));
    public static final Supplier<BlockEntityType<FormationMarkerBlockEntity>> MOTHER = marker("flagmother");
    public static final Supplier<BlockEntityType<FormationMarkerBlockEntity>> SON = marker("flagson");
    public static final Supplier<BlockEntityType<FormationMarkerBlockEntity>> COMPASS = marker("luopan");

    private ZhenfaBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ELEMENT.get(), FormationElementBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BASE.get(), FormationBaseBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MOTHER.get(), FormationMarkerBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SON.get(), FormationMarkerBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COMPASS.get(), FormationMarkerBlockEntity::handler);
        });
    }

    private static Supplier<BlockEntityType<FormationMarkerBlockEntity>> marker(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(FormationMarkerBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<FormationMarkerBlockEntity> markerType(BlockState state) {
        return switch (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()) {
            case "flagmother" -> MOTHER.get();
            case "flagson" -> SON.get();
            default -> COMPASS.get();
        };
    }
}
