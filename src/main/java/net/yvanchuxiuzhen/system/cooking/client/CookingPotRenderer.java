package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 两锅复用各自来源模型、半透明纹理和数字编号动画。 */
public final class CookingPotRenderer extends GeoBlockRenderer<CookingPotBlockEntity> {
    public CookingPotRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(CookingPotBlockEntity pot, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<CookingPotBlockEntity> {
        @Override
        public ResourceLocation getModelResource(CookingPotBlockEntity pot) {
            return resource("geo/" + pot.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(CookingPotBlockEntity pot) {
            return resource("textures/block/" + pot.id() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(CookingPotBlockEntity pot) {
            return resource("animations/" + pot.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
