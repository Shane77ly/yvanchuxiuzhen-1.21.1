package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 修炼系统的方块实体类型：吸血石 {@code xixveshi}（来源注册名，方块由内容表登记）。 */
public final class XiulianBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<BloodStoneBlockEntity>> BLOOD_STONE = TYPES.register("xixveshi",
            () -> BlockEntityType.Builder.of(BloodStoneBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xixveshi"))).build(null));

    private XiulianBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
