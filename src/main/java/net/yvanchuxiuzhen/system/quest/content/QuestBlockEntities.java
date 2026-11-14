package net.yvanchuxiuzhen.system.quest.content;

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

/** 任务伪装方块的方块实体身份及所有方向的单槽物品能力。 */
public final class QuestBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModId.VALUE);
    public static final Supplier<BlockEntityType<DisguisedQuestBlockEntity>> DISGUISED = TYPES.register("renwuweizhuangfangkuai", () ->
            BlockEntityType.Builder.of(DisguisedQuestBlockEntity::new,
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "renwuweizhuangfangkuai"))).build(null));

    private QuestBlockEntities() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                DISGUISED.get(), DisguisedQuestBlockEntity::handler));
    }
}
