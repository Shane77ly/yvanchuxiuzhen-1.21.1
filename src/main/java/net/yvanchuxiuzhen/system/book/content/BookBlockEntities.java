package net.yvanchuxiuzhen.system.book.content;

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

/** 装订台方块实体沿用来源 makebook 身份，并公开原生物品能力。 */
public final class BookBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<BookBindingBlockEntity>> BINDING = TYPES.register("makebook",
            () -> BlockEntityType.Builder.of(BookBindingBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "makebook"))).build(null));

    private BookBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) ->
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BINDING.get(), BookBindingBlockEntity::handler));
    }
}
