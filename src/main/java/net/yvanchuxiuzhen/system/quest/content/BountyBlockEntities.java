package net.yvanchuxiuzhen.system.quest.content;

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

/** 六张悬赏展示物品与公共任务状态共用来源告示牌身份。 */
public final class BountyBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<BountyBoardBlockEntity>> BOARD = TYPES.register("xvanshang", () ->
            BlockEntityType.Builder.of(BountyBoardBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xvanshang"))).build(null));

    private BountyBlockEntities() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BOARD.get(), BountyBoardBlockEntity::handler));
    }
}
