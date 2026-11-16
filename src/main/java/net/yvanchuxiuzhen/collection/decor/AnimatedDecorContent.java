package net.yvanchuxiuzhen.collection.decor;

import java.util.EnumMap;
import java.util.Map;
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

/** 两种动画装饰方块分别登记来源方块实体和六面库存能力。 */
public final class AnimatedDecorContent {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<AnimatedDecorVariant, Supplier<BlockEntityType<AnimatedDecorBlockEntity>>> TYPES = new EnumMap<>(AnimatedDecorVariant.class);

    static {
        for (AnimatedDecorVariant variant : AnimatedDecorVariant.values()) {
            TYPES.put(variant, BLOCK_ENTITIES.register(variant.id(), () -> BlockEntityType.Builder.of(
                    (pos, state) -> new AnimatedDecorBlockEntity(variant, pos, state), block(variant)).build(null)));
        }
    }

    private AnimatedDecorContent() { }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            for (AnimatedDecorVariant variant : AnimatedDecorVariant.values()) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type(variant), AnimatedDecorBlockEntity::handler);
            }
        });
    }

    public static BlockEntityType<AnimatedDecorBlockEntity> type(AnimatedDecorVariant variant) { return TYPES.get(variant).get(); }
    public static Block block(AnimatedDecorVariant variant) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, variant.id()));
    }
}
