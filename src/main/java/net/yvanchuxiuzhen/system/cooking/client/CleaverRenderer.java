package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CuttingBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 菜刀使用来源模型、半透明纹理和数字编号动画。 */
public final class CleaverRenderer extends GeoBlockRenderer<CuttingBlockEntity> {
    public CleaverRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(CuttingBlockEntity pot, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<CuttingBlockEntity> {
        @Override
        public ResourceLocation getModelResource(CuttingBlockEntity pot) {
            return resource("geo/" + pot.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(CuttingBlockEntity pot) {
            return resource("textures/block/caidaoblock.png");
        }

        @Override
        public ResourceLocation getAnimationResource(CuttingBlockEntity pot) {
            return resource("animations/" + pot.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
