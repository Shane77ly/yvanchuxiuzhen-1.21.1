package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.yvanchuxiuzhen.collection.decor.WaterCloud;
import net.yvanchuxiuzhen.platform.ModId;

/** 地淌云源与流动状态使用各自的云水贴图与半透明渲染层。 */
final class WaterCloudClient {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/water_cloud");

    private WaterCloudClient() {
    }

    static void register(IEventBus modBus) {
        modBus.addListener(WaterCloudClient::extensions);
        modBus.addListener(WaterCloudClient::setup);
    }

    private static void extensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return TEXTURE;
            }
        }, WaterCloud.TYPE.get());
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(WaterCloud.SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(WaterCloud.FLOWING.get(), RenderType.translucent());
        });
    }
}
