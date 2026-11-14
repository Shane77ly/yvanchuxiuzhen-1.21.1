package net.yvanchuxiuzhen.system.zhenfa.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationDisplayItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 阵旗与罗盘的块物品按自身注册路径加载对应来源模型。 */
final class FormationDisplayItemRenderer extends GeoItemRenderer<FormationDisplayItem> {
    FormationDisplayItemRenderer() { super(new Model()); }
    @Override public RenderType getRenderType(FormationDisplayItem item, ResourceLocation texture,
            MultiBufferSource buffers, float partialTick) { return RenderType.entityCutout(texture); }

    static final class Provider implements GeoRenderProvider {
        private FormationDisplayItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new FormationDisplayItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<FormationDisplayItem> {
        private String base(FormationDisplayItem item) {
            return switch (item.modelId()) {
                case "flagmother" -> "flag_mother";
                case "flagson" -> "flag_son";
                default -> "luopan";
            };
        }
        @Override public ResourceLocation getModelResource(FormationDisplayItem item) { return resource("geo/" + base(item) + ".geo.json"); }
        @Override public ResourceLocation getTextureResource(FormationDisplayItem item) { return resource("textures/block/" + base(item) + ".png"); }
        @Override public ResourceLocation getAnimationResource(FormationDisplayItem item) { return resource("animations/" + base(item) + ".animation.json"); }
        private ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
    }
}
