package net.yvanchuxiuzhen.system.liandan.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 八档丹炉按注册身份读取来源几何、动画与贴图，使用来源半透明渲染层。 */
public final class AlchemyRenderer extends GeoBlockRenderer<AlchemyBlockEntity> {
    public AlchemyRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(AlchemyBlockEntity furnace, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<AlchemyBlockEntity> {
        @Override
        public ResourceLocation getModelResource(AlchemyBlockEntity furnace) {
            return resource("geo/alchemy_0" + furnace.tier() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(AlchemyBlockEntity furnace) {
            return resource("textures/block/alchemy_0" + furnace.tier() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(AlchemyBlockEntity furnace) {
            return resource("animations/alchemy_0" + furnace.tier() + ".animation.json");
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
