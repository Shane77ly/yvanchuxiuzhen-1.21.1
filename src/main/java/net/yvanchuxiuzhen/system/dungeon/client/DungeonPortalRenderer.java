package net.yvanchuxiuzhen.system.dungeon.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 天道入口与私人画卷各自保留来源几何、动画和透明纹理。 */
public final class DungeonPortalRenderer extends GeoBlockRenderer<DungeonBlockEntity> {
    public DungeonPortalRenderer(BlockEntityRendererProvider.Context context) { super(new Model()); }
    @Override public RenderType getRenderType(DungeonBlockEntity block, ResourceLocation texture, MultiBufferSource buffers, float partialTick) { return RenderType.entityTranslucent(texture); }

    private static final class Model extends GeoModel<DungeonBlockEntity> {
        @Override public ResourceLocation getModelResource(DungeonBlockEntity block) { return resource("geo/" + name(block) + ".geo.json"); }
        @Override public ResourceLocation getAnimationResource(DungeonBlockEntity block) { return resource("animations/" + name(block) + ".animation.json"); }
        @Override public ResourceLocation getTextureResource(DungeonBlockEntity block) { return resource("textures/block/" + (block.kind().equals("fabao_009") ? "fabao00901" : "tiandaofuben") + ".png"); }
        private static String name(DungeonBlockEntity block) { return block.kind().equals("fabao_009") ? "fabao009" : "tiandaofuben"; }
    }

    static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
}
