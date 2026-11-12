package net.yvanchuxiuzhen.system.cooking.content;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 木甑及八个酒坛保留九个方块实体注册身份。 */
public final class WineBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<String, Supplier<BlockEntityType<WineVesselBlockEntity>>> VESSELS = new LinkedHashMap<>();

    static {
        registerType("foodmuzeng");
        for (int index = 1; index <= 8; index++) registerType("jiuniang_%03d01".formatted(index));
    }

    private WineBlockEntities() {
    }

    private static void registerType(String id) {
        VESSELS.put(id, TYPES.register(id, () -> BlockEntityType.Builder.of(WineVesselBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null)));
    }

    public static BlockEntityType<WineVesselBlockEntity> type(BlockState state) {
        return VESSELS.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(WineBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<WineVesselBlockEntity>> type : VESSELS.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), WineVesselBlockEntity::handler);
        }
    }
}
