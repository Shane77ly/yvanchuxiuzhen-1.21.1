package net.yvanchuxiuzhen.system.fulu.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 传承之光复用来源几何和半透明纹理，循环动画由方块状态提供。 */
public final class InheritanceRenderer extends GeoBlockRenderer<InheritanceBlockEntity> {
    public InheritanceRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(InheritanceBlockEntity table, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<InheritanceBlockEntity> {
        @Override
        public ResourceLocation getModelResource(InheritanceBlockEntity table) {
            return resource("geo/chuancheng.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(InheritanceBlockEntity table) {
            return resource("textures/block/chuancheng.png");
        }

        @Override
        public ResourceLocation getAnimationResource(InheritanceBlockEntity table) {
            return resource("animations/chuancheng.animation.json");
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
