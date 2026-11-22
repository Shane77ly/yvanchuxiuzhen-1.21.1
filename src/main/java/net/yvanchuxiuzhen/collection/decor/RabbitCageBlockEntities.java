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

/** 只有开笼具有诱饵容器，闭笼内动物沿用世界实体保存。 */
public final class RabbitCageBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<RabbitCageBlockEntity>> OPEN = TYPES.register("zhuangshi_tulong_02", () ->
            BlockEntityType.Builder.of(RabbitCageBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zhuangshi_tulong_02"))).build(null));

    private RabbitCageBlockEntities() { }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, OPEN.get(), RabbitCageBlockEntity::handler));
    }
}
