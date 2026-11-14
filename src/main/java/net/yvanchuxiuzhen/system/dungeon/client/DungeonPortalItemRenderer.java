package net.yvanchuxiuzhen.system.dungeon.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonPortalItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 天道入口物品与方块共用来源资产，显示变换由物品模型父链提供。 */
public final class DungeonPortalItemRenderer extends GeoItemRenderer<DungeonPortalItem> {
    public DungeonPortalItemRenderer() { super(new Model()); }
    @Override public RenderType getRenderType(DungeonPortalItem item, ResourceLocation texture, MultiBufferSource buffers, float partialTick) { return RenderType.entityTranslucent(texture); }

    public static final class Provider implements GeoRenderProvider {
        private DungeonPortalItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new DungeonPortalItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<DungeonPortalItem> {
        @Override public ResourceLocation getModelResource(DungeonPortalItem item) { return DungeonPortalRenderer.resource("geo/tiandaofuben.geo.json"); }
        @Override public ResourceLocation getTextureResource(DungeonPortalItem item) { return DungeonPortalRenderer.resource("textures/block/tiandaofuben.png"); }
        @Override public ResourceLocation getAnimationResource(DungeonPortalItem item) { return DungeonPortalRenderer.resource("animations/tiandaofuben.animation.json"); }
    }
}
