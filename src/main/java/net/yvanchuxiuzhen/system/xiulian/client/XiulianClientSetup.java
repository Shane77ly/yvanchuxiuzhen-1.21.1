package net.yvanchuxiuzhen.system.xiulian.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.xiulian.XiulianSetup;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianBlockEntities;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianFluids;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianMenus;
import net.yvanchuxiuzhen.system.xiulian.tribulation.TribulationEntity;
import net.yvanchuxiuzhen.system.xiulian.tribulation.Tribulations;

/** 修炼系统的客户端装配：出身、升级与丹田界面，血池与血流渲染，以及劫云和天雷实体的模型、动画与发光层。 */
public final class XiulianClientSetup {
    private static final ResourceLocation BLOOD_STILL = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/xveliustill");
    private static final ResourceLocation BLOOD_FLOWING = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/xveliu");

    private XiulianClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(XiulianClientSetup::registerScreens);
        modBus.addListener(XiulianClientSetup::registerRenderers);
        modBus.addListener(XiulianClientSetup::registerExtensions);
        modBus.addListener(XiulianClientSetup::setup);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(XiulianMenus.BIRTH.get(), BirthScreen::new);
        event.register(XiulianMenus.LEVEL_UP.get(), LevelUpScreen::new);
        event.register(XiulianMenus.DANTIAN.get(), DantianScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(XiulianBlockEntities.BLOOD_STONE.get(), BloodStoneRenderer::new);
        for (EntityModelRow row : Tribulations.models().values()) {
            event.registerEntityRenderer(XiulianSetup.<TribulationEntity>entityType(row.id()).get(), context -> new AnimatedEntityRenderer<>(context, row));
        }
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return BLOOD_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return BLOOD_FLOWING;
            }
        }, XiulianFluids.BLOOD_TYPE.get());
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(XiulianFluids.BLOOD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(XiulianFluids.FLOWING_BLOOD.get(), RenderType.translucent());
        });
    }
}
