package net.yvanchuxiuzhen.system.lianqi.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 修理台复用来源几何和半透明纹理，朝向由方块状态提供。 */
public final class RepairTableRenderer extends GeoBlockRenderer<RepairTableBlockEntity> {
    public RepairTableRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(RepairTableBlockEntity table, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<RepairTableBlockEntity> {
        @Override
        public ResourceLocation getModelResource(RepairTableBlockEntity table) {
            return resource("geo/xiulitai.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(RepairTableBlockEntity table) {
            return resource("textures/block/xiulitai.png");
        }

        @Override
        public ResourceLocation getAnimationResource(RepairTableBlockEntity table) {
            return resource("animations/xiulitai.animation.json");
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
