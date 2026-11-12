package net.yvanchuxiuzhen.system.plant;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 葫芦果实保留来源方块实体身份与无方向、六方向库存能力。 */
public final class GourdBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<GourdBlockEntity>> GOURD = TYPES.register("shicai_039_plant_04",
            () -> BlockEntityType.Builder.of(GourdBlockEntity::new, VineCrop.SHICAI_039.block(4)).build(null));

    private GourdBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(GourdBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GOURD.get(), GourdBlockEntity::handler);
    }
}
