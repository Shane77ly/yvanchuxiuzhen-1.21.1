package net.yvanchuxiuzhen.system.yihuo;

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

/** 每种火块保留独立的来源方块实体类型 ID，共用运行代码。 */
public final class YihuoBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    private static final Map<String, Supplier<BlockEntityType<FireBlockEntity>>> FIRES = new LinkedHashMap<>();

    static {
        for (int family : new int[] {220, 230}) {
            for (int index = 1; index <= 9; index++) {
                String id = "cailiao_" + (family + index) + "fireblock";
                FIRES.put(id, TYPES.register(id, () -> BlockEntityType.Builder.of(FireBlockEntity::new,
                        BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id))).build(null)));
            }
        }
    }

    private YihuoBlockEntities() {
    }

    static BlockEntityType<FireBlockEntity> type(BlockState state) {
        return FIRES.get(((FireBlock) state.getBlock()).traits().row().id()).get();
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(YihuoBlockEntities::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<BlockEntityType<FireBlockEntity>> type : FIRES.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(),
                    (entity, direction) -> direction == null ? null : entity.handler(direction));
        }
    }
}
