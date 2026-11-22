package net.yvanchuxiuzhen.system.plant;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 高草动画方块保留来源方块实体身份和六个方向的九槽库存能力。 */
public final class AnimatedBigGrassBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<AnimatedBigGrassBlockEntity>> GRASS = TYPES.register("biggrassyincang",
            () -> BlockEntityType.Builder.of(AnimatedBigGrassBlockEntity::new, AnimatedBigGrassBlock.block()).build(null));

    private AnimatedBigGrassBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(AnimatedBigGrassBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GRASS.get(), AnimatedBigGrassBlockEntity::handler);
    }
}
