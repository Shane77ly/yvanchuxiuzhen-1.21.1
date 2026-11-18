package net.yvanchuxiuzhen.system.creature.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 棺材方块、零槽容器能力与动画方块物品的系统内装配。 */
public final class CoffinContent {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<CoffinBlockEntity>> TYPE = TYPES.register("guancai_01", () ->
            BlockEntityType.Builder.of(CoffinBlockEntity::new, block()).build(null));

    private CoffinContent() { }

    private static Block block() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guancai_01"));
    }

    public static void register(ContentTables content, IEventBus modBus) {
        content.blockFactory("creature:coffin", traits -> ContentBlock.create(traits, CoffinBlock::new));
        content.itemFactory("creature:coffin", (properties, tooltip) -> new CoffinBlockItem(block(), properties, tooltip));
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, TYPE.get(), CoffinBlockEntity::handler));
    }
}
