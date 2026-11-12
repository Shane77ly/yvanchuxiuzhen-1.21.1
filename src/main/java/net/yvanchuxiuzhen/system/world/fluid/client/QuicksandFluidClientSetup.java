package net.yvanchuxiuzhen.system.world.fluid.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.fluid.QuicksandFluids;

/** 流沙保留来源原色贴图与半透明流体渲染层。 */
public final class QuicksandFluidClientSetup {
    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/quicksand01");
    private static final ResourceLocation FLOWING = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/quicksand02");

    private QuicksandFluidClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(QuicksandFluidClientSetup::extensions);
        modBus.addListener(QuicksandFluidClientSetup::setup);
    }

    private static void extensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;
            }
        }, QuicksandFluids.TYPE.get());
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(QuicksandFluids.SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(QuicksandFluids.FLOWING.get(), RenderType.translucent());
        });
    }
}
