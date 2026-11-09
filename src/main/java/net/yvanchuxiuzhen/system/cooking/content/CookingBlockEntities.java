package net.yvanchuxiuzhen.system.cooking.content;

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

/** 两锅分别保留方块实体注册身份，并向全部方向提供十四槽库存。 */
public final class CookingBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<CookingPotBlockEntity>> IRON = registerType("fooddaguo");
    public static final Supplier<BlockEntityType<CookingPotBlockEntity>> CLAY = registerType("fooddunguo");

    private CookingBlockEntities() {
    }

    private static Supplier<BlockEntityType<CookingPotBlockEntity>> registerType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(CookingPotBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<CookingPotBlockEntity> type(BlockState state) {
        return ((CookingPotBlock) state.getBlock()).iron() ? IRON.get() : CLAY.get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(CookingBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IRON.get(), CookingPotBlockEntity::handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CLAY.get(), CookingPotBlockEntity::handler);
    }
}
