package net.yvanchuxiuzhen.system.world.fluid.client;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.fluid.SpringFluids;

/** 温泉的静止与流动贴图、物品颜色、群系水色和半透明渲染层。 */
public final class SpringFluidClientSetup {
    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/wenqvanstill");
    private static final ResourceLocation FLOWING = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/wenqvanflow");

    private SpringFluidClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(SpringFluidClientSetup::extensions);
        modBus.addListener(SpringFluidClientSetup::setup);
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
                return -13083194;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                return BiomeColors.getAverageWaterColor(level, pos) | 0xFF000000;
            }
        }, SpringFluids.TYPE.get());
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(SpringFluids.SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(SpringFluids.FLOWING.get(), RenderType.translucent());
        });
    }
}
