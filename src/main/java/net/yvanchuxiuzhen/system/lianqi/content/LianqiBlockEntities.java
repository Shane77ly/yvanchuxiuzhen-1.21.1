package net.yvanchuxiuzhen.system.lianqi.content;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 八鼎分别保留方块实体身份，并提供对应方向的物品库存能力。 */
public final class LianqiBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final List<Supplier<BlockEntityType<TripodBlockEntity>>> TRIPODS = IntStream.rangeClosed(1, 8)
            .mapToObj(tier -> registerType("tripod_0" + tier)).toList();

    private LianqiBlockEntities() {
    }

    private static Supplier<BlockEntityType<TripodBlockEntity>> registerType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(TripodBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<TripodBlockEntity> type(int tier) {
        return TRIPODS.get(tier - 1).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(LianqiBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<TripodBlockEntity>> type : TRIPODS) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), TripodBlockEntity::handler);
        }
    }
}
