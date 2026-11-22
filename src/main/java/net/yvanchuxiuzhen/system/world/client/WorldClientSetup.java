package net.yvanchuxiuzhen.system.world.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.world.WorldSetup;
import net.yvanchuxiuzhen.system.world.content.RiftItem;
import net.yvanchuxiuzhen.system.world.content.SpiritCompassItem;
import net.yvanchuxiuzhen.system.world.content.WorldBlockEntities;
import net.yvanchuxiuzhen.system.world.entity.GuidanceHalo;

/** 世界探索的客户端装配登记光环、裂隙和寻灵盘的来源动画显示。 */
public final class WorldClientSetup {
    private WorldClientSetup() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(WorldSetup.<GuidanceHalo>entityType("guanghuan").get(), GuidanceHaloRenderer::new);
            event.registerBlockEntityRenderer(WorldBlockEntities.RIFT.get(), RiftRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof SpiritCompassItem compass) compass.setRenderProvider(new SpiritCompassRenderer.Provider());
                if (item instanceof RiftItem rift) rift.setRenderProvider(new RiftItemRenderer.Provider());
            }
        }));
    }
}
