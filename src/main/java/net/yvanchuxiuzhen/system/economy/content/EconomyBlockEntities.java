package net.yvanchuxiuzhen.system.economy.content;

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

/** 注册银行、玩家商铺和拍卖台库存，并保留各自的来源方向能力边界。 */
public final class EconomyBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<EconomyBlockEntity>> SHOP = type("shopblock_01");
    public static final Supplier<BlockEntityType<EconomyBlockEntity>> AUCTION = type("paimaiblock");
    public static final Supplier<BlockEntityType<BankBlockEntity>> BANK = TYPES.register("tiandiyinhang", () ->
            BlockEntityType.Builder.of(BankBlockEntity::new, BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandiyinhang"))).build(null));
    private EconomyBlockEntities() { }
    private static Supplier<BlockEntityType<EconomyBlockEntity>> type(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(EconomyBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }
    static BlockEntityType<EconomyBlockEntity> type(BlockState state) { return ((EconomyBlock) state.getBlock()).auction() ? AUCTION.get() : SHOP.get(); }
    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SHOP.get(), EconomyBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AUCTION.get(), EconomyBlockEntity::handler);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BANK.get(), BankBlockEntity::handler);
        });
    }
}
