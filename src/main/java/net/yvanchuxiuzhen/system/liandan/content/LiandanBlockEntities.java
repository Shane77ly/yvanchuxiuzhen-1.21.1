package net.yvanchuxiuzhen.system.liandan.content;

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

/** 八档丹炉分别保留来源方块实体身份，全部方向及无方向查询均暴露十二槽库存。 */
public final class LiandanBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final List<Supplier<BlockEntityType<AlchemyBlockEntity>>> ALCHEMY = IntStream.rangeClosed(1, 8)
            .mapToObj(tier -> registerType("alchemy_0" + tier)).toList();

    private LiandanBlockEntities() {
    }

    private static Supplier<BlockEntityType<AlchemyBlockEntity>> registerType(String id) {
        return TYPES.register(id, () -> BlockEntityType.Builder.of(AlchemyBlockEntity::new,
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null));
    }

    public static BlockEntityType<AlchemyBlockEntity> type(int tier) {
        return ALCHEMY.get(tier - 1).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(LiandanBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<AlchemyBlockEntity>> type : ALCHEMY) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), AlchemyBlockEntity::handler);
        }
    }
}
