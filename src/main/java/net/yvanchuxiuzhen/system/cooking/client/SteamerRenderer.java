package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 蒸锅与笼屉复用各自来源模型、半透明纹理和数字编号动画。 */
public final class SteamerRenderer extends GeoBlockRenderer<SteamerBlockEntity> {
    public SteamerRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(SteamerBlockEntity pot, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<SteamerBlockEntity> {
        @Override
        public ResourceLocation getModelResource(SteamerBlockEntity pot) {
            return resource("geo/" + pot.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SteamerBlockEntity pot) {
            return resource("textures/block/foodzhengguo.png");
        }

        @Override
        public ResourceLocation getAnimationResource(SteamerBlockEntity pot) {
            return resource("animations/" + pot.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
