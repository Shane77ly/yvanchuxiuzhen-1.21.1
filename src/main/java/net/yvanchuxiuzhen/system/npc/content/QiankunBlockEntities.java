package net.yvanchuxiuzhen.system.npc.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 乾坤道人与门徒方块实体注册及门徒单槽自动化能力。 */
public final class QiankunBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<QiankundaorenBlockEntity>> DAO = TYPES.register("qiankundaoren", () ->
            BlockEntityType.Builder.of(QiankundaorenBlockEntity::new, block("qiankundaoren")).build(null));
    public static final Supplier<BlockEntityType<QiankunmentuBlockEntity>> DISCIPLE = TYPES.register("qiankunmentu", () ->
            BlockEntityType.Builder.of(QiankunmentuBlockEntity::new, block("qiankunmentu")).build(null));

    private QiankunBlockEntities() { }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, DAO.get(), QiankundaorenBlockEntity::handler));
        bus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, DISCIPLE.get(), QiankunmentuBlockEntity::handler));
    }
}
