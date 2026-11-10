package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 石磨和石碾方块按注册身份选择来源几何、纹理与数字动画。 */
public final class StoneMillRenderer extends GeoBlockRenderer<StoneMillBlockEntity> {
    public StoneMillRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(StoneMillBlockEntity mill, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<StoneMillBlockEntity> {
        @Override
        public ResourceLocation getModelResource(StoneMillBlockEntity mill) {
            return resource("geo/" + mill.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(StoneMillBlockEntity mill) {
            return resource("textures/block/" + mill.id() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(StoneMillBlockEntity mill) {
            return resource("animations/" + mill.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
