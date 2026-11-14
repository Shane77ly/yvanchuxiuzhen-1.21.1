package net.yvanchuxiuzhen.system.town.content;

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

/** 规划核心十八槽、中心案台一槽保持各自来源身份。 */
public final class TownBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<TownBlockEntity>> PLANNING = register("chengzhen_buildblock", true);
    public static final Supplier<BlockEntityType<TownBlockEntity>> CENTER = register("chengzhen_antai", false);

    private TownBlockEntities() {
    }

    private static Supplier<BlockEntityType<TownBlockEntity>> register(String id, boolean planning) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of((pos, state) -> new TownBlockEntity(pos, state, planning),
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PLANNING.get(), TownBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CENTER.get(), TownBlockEntity::handler);
        });
    }
}
