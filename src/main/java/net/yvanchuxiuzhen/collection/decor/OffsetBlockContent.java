package net.yvanchuxiuzhen.collection.decor;

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

/** 注册偏移方块实体及其物品处理器能力。 */
public final class OffsetBlockContent {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<OffsetBlockEntity>> TYPE = BLOCK_ENTITIES.register("pianyifangkuai",
            () -> BlockEntityType.Builder.of(OffsetBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pianyifangkuai"))).build(null));

    private OffsetBlockContent() {
    }

    static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(OffsetBlockContent::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TYPE.get(), OffsetBlockEntity::handler);
    }
}
