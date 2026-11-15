package net.yvanchuxiuzhen.collection.tiandao;

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

/** 多结构生成器的方块实体身份和六面及无方向零槽库存能力。 */
public final class MultiStructureContent {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<MultiStructureBlockEntity>> TYPE = TYPES.register("tiandaoduojiegou", () ->
            BlockEntityType.Builder.of(MultiStructureBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandaoduojiegou"))).build(null));

    private MultiStructureContent() {
    }

    static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(MultiStructureContent::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TYPE.get(), MultiStructureBlockEntity::handler);
    }
}
