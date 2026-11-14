package net.yvanchuxiuzhen.system.treasure.content;

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

/** 两种宝箱保留独立注册身份与单槽、六个侧面的来源物品能力。 */
public final class TreasureBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<TreasureChestBlockEntity>> CHEST_03 = register("treasure_chest_03");
    public static final Supplier<BlockEntityType<TreasureChestBlockEntity>> CHEST_11 = register("treasure_chest_11");

    private TreasureBlockEntities() {
    }

    private static Supplier<BlockEntityType<TreasureChestBlockEntity>> register(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(TreasureChestBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CHEST_03.get(), TreasureChestBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CHEST_11.get(), TreasureChestBlockEntity::handler);
        });
    }
}
