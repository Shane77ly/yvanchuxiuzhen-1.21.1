package net.yvanchuxiuzhen.system.craft.content;

import java.util.Map;
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

/** 石磨与石碾分别保留方块实体注册身份，提供无方向库存与六方向自动化库存。 */
public final class CraftBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<String, Supplier<BlockEntityType<StoneMillBlockEntity>>> MILLS = Map.of(
            "stonemill", registerType("stonemill"),
            "stoneroller", registerType("stoneroller"));

    private CraftBlockEntities() {
    }

    private static Supplier<BlockEntityType<StoneMillBlockEntity>> registerType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(StoneMillBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<StoneMillBlockEntity> type(String id) {
        return MILLS.get(id).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(CraftBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<StoneMillBlockEntity>> type : MILLS.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), StoneMillBlockEntity::handler);
        }
    }
}
