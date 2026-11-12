package net.yvanchuxiuzhen.system.guchong.content;

import java.util.Map;
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

/** 三种蛊罐各自注册方块实体类型，共用容量随方块编号变化的实体实现。 */
public final class GuchongBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<Integer, Supplier<BlockEntityType<TaoguanBlockEntity>>> TYPES_BY_GRADE = Map.of(
            1, register(1), 2, register(2), 3, register(3));

    private GuchongBlockEntities() { }

    private static Supplier<BlockEntityType<TaoguanBlockEntity>> register(int grade) {
        String id = String.format("taoguan_%02d", grade);
        return TYPES.register(id, () -> BlockEntityType.Builder.of(TaoguanBlockEntity::new, block(id)).build(null));
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    static BlockEntityType<TaoguanBlockEntity> type(int grade) {
        return TYPES_BY_GRADE.get(grade).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(GuchongBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<TaoguanBlockEntity>> type : TYPES_BY_GRADE.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(), TaoguanBlockEntity::handler);
        }
    }
}
